/*
 * Copyright 2019 HM Revenue & Customs
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
import models.MandationStatus
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{ActionRefiner, Result}
import play.api.mvc.Results.Forbidden
import services.MandationStatusService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import common.MandationStatuses.nonMTDfB
import models.auth.User

import scala.concurrent.{ExecutionContext, Future}

class MandationStatusPredicate @Inject()(mandationStatusService: MandationStatusService,
                                         val errorHandler: ErrorHandler,
                                         val messagesApi: MessagesApi,
                                         implicit val appConfig: AppConfig,
                                         implicit val ec: ExecutionContext) extends ActionRefiner[User, User] with I18nSupport {


  override def refine[A](request: User[A]): Future[Either[Result, User[A]]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
    implicit val req: User[A] = request


    mandationStatusService.getMandationStatus(req.vrn) map {
      case Right(MandationStatus(`nonMTDfB`)) => Right(request)
      case Right(unsupportedMandationStatus) =>
        Logger.debug(s"[MandationStatusPredicate][refine] - User has a non 'non MTDfB' status received. Status returned was: $unsupportedMandationStatus")
        //TODO: Update in future story with correct error content
        Left(Forbidden(views.html.errors.unauthorised_agent()))
      case error =>
        Logger.info(s"[MandationStatusPredicate][refine] - Error has been received $error")
        Logger.warn(s"[MandationStatusPredicate][refine] - Error has been received")
        Left(errorHandler.showInternalServerError)
    }
  }
}
