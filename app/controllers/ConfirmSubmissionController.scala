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

package controllers

import common.SessionKeys
import config.{AppConfig, ErrorHandler}
import controllers.predicates.{AuthPredicate, MandationStatusPredicate}
import javax.inject.{Inject, Singleton}
import models.{ConfirmSubmissionViewModel, SubmitVatReturnModel}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._
import services.VatSubscriptionService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.Future

@Singleton
class ConfirmSubmissionController @Inject()(val messagesApi: MessagesApi,
                                            val mandationStatusCheck: MandationStatusPredicate,
                                            val errorHandler: ErrorHandler,
                                            val vatSubscriptionService: VatSubscriptionService,
                                            authPredicate: AuthPredicate,
                                            implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  def show(periodKey: String): Action[AnyContent] = (authPredicate andThen mandationStatusCheck).async { implicit user =>

    user.session.get(SessionKeys.viewModel) match {
      case Some(model) => {
        val sessionData = Json.parse(model).as[SubmitVatReturnModel]

        vatSubscriptionService.getCustomerDetails(user.vrn) map {
          case (Right(customerDetails)) => {
            val viewModel = ConfirmSubmissionViewModel(sessionData, periodKey, customerDetails.clientName)
            Ok(views.html.confirm_submission(viewModel, user.isAgent))
          }
          case _ => {
            val viewModel = ConfirmSubmissionViewModel(sessionData, periodKey, None)
            Ok(views.html.confirm_submission(viewModel, user.isAgent))
          }
        }
      }
      case _ => Future.successful(Redirect(controllers.routes.SubmitFormController.show(periodKey)))
    }
  }

  def submit(periodKey: String): Action[AnyContent] = (authPredicate andThen mandationStatusCheck) { implicit user =>
    Redirect(controllers.routes.ConfirmationController.show().url)
  }
}
