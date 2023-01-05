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

package controllers

import common.SessionKeys
import config.AppConfig
import controllers.predicates.{AuthPredicate, MandationStatusPredicate}
import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.ConfirmationView

import scala.concurrent.Future

@Singleton
class ConfirmationController @Inject()(val mandationStatusCheck: MandationStatusPredicate,
                                       authPredicate: AuthPredicate,
                                       mcc: MessagesControllerComponents,
                                       confirmationView: ConfirmationView,
                                       implicit val appConfig: AppConfig) extends FrontendController(mcc) with I18nSupport {

  val show: Action[AnyContent] = (authPredicate andThen mandationStatusCheck).async { implicit user =>
    Future.successful(Ok(confirmationView()))
  }

  val submit: Action[AnyContent] = (authPredicate andThen mandationStatusCheck).async { implicit user =>
    if (user.isAgent) {
      Future.successful(
        Redirect(appConfig.manageClientUrl).removingFromSession(
          SessionKeys.inSessionPeriodKey,
          SessionKeys.submissionYear
        )
      )
    } else {
      Future.successful(
        Redirect(appConfig.vatSummaryUrl).removingFromSession(
          SessionKeys.inSessionPeriodKey,
          SessionKeys.submissionYear)
      )
    }
  }


}
