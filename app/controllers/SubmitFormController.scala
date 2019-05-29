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

import forms.SubmitVatReturnForm
import common.SessionKeys
import config.AppConfig
import javax.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Request
import config.ErrorHandler
import controllers.predicates.AuthPredicate
import controllers.predicates.MandationStatusPredicate
import models.auth.User
import models.{SubmitFormViewModel, SubmitVatReturnModel, VatObligation}
import play.api.Logger
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import services.{VatObligationsService, VatSubscriptionService}
import uk.gov.hmrc.http.HeaderCarrier
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

  def show(periodKey: String): Action[AnyContent] = (authPredicate andThen mandationStatusCheck).async { implicit user =>

    user.session.get(SessionKeys.returnData) match {
      case Some(model) => renderViewWithSessionData(periodKey, model)
      case _ => renderViewWithoutSessionData(periodKey, SubmitVatReturnForm.submitVatReturnForm)
    }
  }

  private def renderViewWithSessionData(periodKey: String, model: String)(implicit request: Request[_], user: User[_], hc: HeaderCarrier) = {

    val sessionData = Json.parse(model).as[SubmitVatReturnModel]

    vatSubscriptionService.getCustomerDetails(user.vrn) map {
      case Right(customerDetails) => {
        Ok(views.html.submit_form(
          periodKey,
          customerDetails.clientName,
          sessionData.flatRateScheme,
          VatObligation(sessionData.start, sessionData.end, sessionData.due, periodKey),
          SubmitVatReturnForm.submitVatReturnForm.fill(sessionData),
          isAgent = user.isAgent)
        )
      }
      case _ => {
        Ok(views.html.submit_form(
          periodKey,
          None,
          sessionData.flatRateScheme,
          VatObligation(sessionData.start, sessionData.end, sessionData.due, periodKey),
          SubmitVatReturnForm.submitVatReturnForm.fill(sessionData),
          isAgent = user.isAgent)
        )
      }
    }
  }

  private def renderViewWithoutSessionData(periodKey: String,
                                           form: Form[SubmitVatReturnModel])(implicit request: Request[_], user: User[_], hc: HeaderCarrier) = {

    for {
      customerInformation <- vatSubscriptionService.getCustomerDetails(user.vrn)
      obligations <- vatObligationsService.getObligations(user.vrn)
    } yield {
      (customerInformation, obligations) match {
        case (Right(customerDetails), Right(obs)) => {

          val obligationToSubmit: Seq[VatObligation] = obs.obligations.filter(_.periodKey == periodKey)

          obligationToSubmit.length match {
            case 1 => {

              val viewModel = SubmitFormViewModel(
                customerDetails.hasFlatRateScheme,
                obligationToSubmit.head.start,
                obligationToSubmit.head.end,
                obligationToSubmit.head.due
              )

              Ok(views.html.submit_form(
                periodKey,
                customerDetails.clientName,
                customerDetails.hasFlatRateScheme,
                obligationToSubmit.head,
                form,
                user.isAgent
              )).addingToSession(SessionKeys.viewModel -> Json.toJson(viewModel).toString())
            }
            case _ => {
              Logger.warn("[SubmitFormController][Show]: Length of matched obligations to period key is not equal to 1")
              Redirect(appConfig.returnDeadlinesUrl)
            }
          }
        }
        case (_, _) => errorHandler.showInternalServerError
      }
    }
  }

  def submit(periodKey: String): Action[AnyContent] = (authPredicate andThen mandationStatusCheck).async { implicit user =>

    SubmitVatReturnForm.submitVatReturnForm.bindFromRequest().fold(
      failure => {

        user.session.get(SessionKeys.viewModel) match {
          case Some(model) => {

            val sessionData = Json.parse(model).as[SubmitFormViewModel]

            vatSubscriptionService.getCustomerDetails(user.vrn) map {
              case (Right(customerDetails)) => {
                Ok(views.html.submit_form(
                  periodKey,
                  customerDetails.clientName,
                  sessionData.hasFlatRateScheme,
                  VatObligation(sessionData.start, sessionData.end, sessionData.due, periodKey),
                  failure,
                  user.isAgent)
                )
              }
              case (_) => {
                Ok(views.html.submit_form(
                  periodKey,
                  None,
                  sessionData.hasFlatRateScheme,
                  VatObligation(sessionData.start, sessionData.end, sessionData.due, periodKey),
                  failure,
                  isAgent = user.isAgent)
                )
              }
            }
          }
          case _ => renderViewWithoutSessionData(periodKey, failure)
        }
      },
      success => {
        Future.successful(
          Redirect(controllers.routes.ConfirmSubmissionController.show(periodKey))
            .addingToSession(SessionKeys.returnData -> Json.toJson(success).toString())
            .removingFromSession(SessionKeys.viewModel)
        )
      }
    )
  }
}
