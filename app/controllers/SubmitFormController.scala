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
import play.api.mvc.Request
import config.ErrorHandler
import controllers.predicates.AuthPredicate
import controllers.predicates.MandationStatusPredicate
import models.auth.User
import models.{SubmitVatReturnModel, VatObligation}
import play.api.Logger
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

  val SubmitVatReturnForm = new SubmitVatReturnForm()(messagesApi)

  def show(periodKey: String): Action[AnyContent] = (authPredicate andThen mandationStatusCheck).async { implicit user =>

    user.session.get(SessionKeys.viewModel) match {
      case Some(model) => {
        val sessionData = Json.parse(model).as[SubmitVatReturnModel]

        vatSubscriptionService.getCustomerDetails(user.vrn) map {
          case Right(customerDetails) => {
            Ok(views.html.submit_form(
              periodKey,
              customerDetails.clientName,
              sessionData.flatRateScheme,
              VatObligation(sessionData.start, sessionData.end, sessionData.due, periodKey),
              SubmitVatReturnForm.submitVatReturnForm.fill(sessionData),
              user.isAgent)
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
      case _ => renderViewWithoutSessionDate(periodKey)
    }
  }

  private def renderViewWithoutSessionDate(periodKey: String)(implicit request: Request[_], user: User[_], hc: HeaderCarrier) = {

    for {
      customerInformation <- vatSubscriptionService.getCustomerDetails(user.vrn)
      obligations <- vatObligationsService.getObligations(user.vrn)
    } yield {
      (customerInformation, obligations) match {
        case (Right(customerDetails), Right(obs)) => {

          val obligationToSubmit: Seq[VatObligation] = obs.obligations.filter(_.periodKey == periodKey)

          obligationToSubmit.length match {
            case 1 => {
              Ok(views.html.submit_form(
                periodKey,
                customerDetails.clientName,
                customerDetails.hasFlatRateScheme,
                obligationToSubmit.head,
                SubmitVatReturnForm.submitVatReturnForm,
                user.isAgent
              ))
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
              VatObligation(LocalDate.parse(start), LocalDate.parse(end), LocalDate.parse(due), periodKey),
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
