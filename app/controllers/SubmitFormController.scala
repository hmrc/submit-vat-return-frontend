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

import java.time.LocalDate

import forms.SubmitVatReturnForm

import common.SessionKeys
import config.AppConfig
import javax.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, MessagesApi}
import config.ErrorHandler
import controllers.predicates.AuthPredicate
import controllers.predicates.MandationStatusPredicate
import models.VatObligation
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import services.{VatObligationsService, VatSubscriptionService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.Future

@Singleton
class SubmitFormController @Inject()(val messagesApi: MessagesApi,
                                     val vatSubscriptionService: VatSubscriptionService,
                                     val vatObligationsService: VatObligationsService,
                                     val mandationStatusCheck: MandationStatusPredicate,
                                     val errorHandler: ErrorHandler,
                                     authPredicate: AuthPredicate,
                                     implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  val SubmitVatReturnForm = new SubmitVatReturnForm()(messagesApi)

  def show(periodKey: String): Action[AnyContent] = (authPredicate andThen mandationStatusCheck).async { implicit user =>

    val customerInformationCall = vatSubscriptionService.getCustomerDetails(user.vrn)
    val obligationsCall = vatObligationsService.getObligations(user.vrn)

    for {
      customerInformation <- customerInformationCall
      obligations <- obligationsCall
    } yield {
      (customerInformation, obligations) match {
        case (Right(customerDetails), Right(obs)) => {

          val obligationToSubmit: Seq[VatObligation] = obs.obligations.filter(x => x.periodKey == periodKey)

          Ok(views.html.submit_form(
            periodKey,
            customerDetails.clientName,
            customerDetails.hasFlatRateScheme,
            obligationToSubmit,
            SubmitVatReturnForm.submitVatReturnForm,
            user.isAgent
          ))
        }
        case (_, _) => errorHandler.showInternalServerError
      }
    }
  }

  def submit(periodKey: String,
             clientName: Option[String],
             hasFlatRateScheme: Boolean,
             start: String,
             end: String,
             due: String): Action[AnyContent] = (authPredicate andThen mandationStatusCheck).async { implicit user =>

    SubmitVatReturnForm.submitVatReturnForm.bindFromRequest().fold(
      failure => {
        Future.successful(
          Ok(
            views.html.submit_form(periodKey,
              clientName,
              hasFlatRateScheme,
              Seq(VatObligation(LocalDate.parse(start), LocalDate.parse(end), LocalDate.parse(due), periodKey)),
              failure,
              user.isAgent))
        )
      },
        success => {
          Future.successful(
            Redirect(controllers.routes.ConfirmSubmissionController.show(periodKey)).addingToSession(SessionKeys.viewModel -> Json.toJson(success).toString())
          )
        }
      )
    }
}
