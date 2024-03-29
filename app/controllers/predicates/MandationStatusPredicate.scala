/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.predicates

import config.{AppConfig, ErrorHandler}

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{ActionRefiner, Result}
import play.api.mvc.Results.{Forbidden, Redirect}
import services.MandationStatusService
import uk.gov.hmrc.http.HeaderCarrier
import common.{MandationStatuses, SessionKeys}
import models.auth.User
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.LoggingUtil
import views.html.errors.MtdMandatedUser

import scala.concurrent.{ExecutionContext, Future}

class MandationStatusPredicate @Inject()(mandationStatusService: MandationStatusService,
                                         val errorHandler: ErrorHandler,
                                         val messagesApi: MessagesApi,
                                         mtdMandatedUser: MtdMandatedUser,
                                         implicit val appConfig: AppConfig,
                                         implicit val executionContext: ExecutionContext) extends ActionRefiner[User, User]
                                         with I18nSupport with LoggingUtil {

  override def refine[A](request: User[A]): Future[Either[Result, User[A]]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    implicit val req: User[A] = request
    val supportedMandationStatuses =
      List(MandationStatuses.nonMTDfB, MandationStatuses.nonDigital, MandationStatuses.MTDfBExempt)

    req.session.get(SessionKeys.mandationStatus) match {
      case Some(status) if supportedMandationStatuses.contains(status) => Future.successful(Right(request))
      case Some(unsupportedMandationStatus) =>
        debug("[MandationStatusPredicate][refine] - User has a non 'non MTDfB' status received. " +
          s"Status returned was: $unsupportedMandationStatus")
        Future.successful(Left(Forbidden(mtdMandatedUser())))
      case None => getMandationStatus
    }
  }

  private def getMandationStatus[A](implicit user: User[A], hc: HeaderCarrier): Future[Left[Result, User[A]]] = {
    mandationStatusService.getMandationStatus(user.vrn) map {
      case Right(status) =>
        Left(Redirect(user.uri).addingToSession(SessionKeys.mandationStatus -> status.mandationStatus))
      case Left(error) =>
        errorLog(s"[MandationStatusPredicate][refine] - Error has been received $error")
        Left(errorHandler.showInternalServerError)
    }
  }
}
