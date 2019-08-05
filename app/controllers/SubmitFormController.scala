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
import models.{NineBoxModel, SubmitFormViewModel, SubmitVatReturnModel, VatObligation}
import play.api.Logger
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import services.{DateService, VatObligationsService, VatSubscriptionService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import forms.SubmitVatReturnForm._
import java.net.URLDecoder

import audit.AuditService
import audit.models.SubmitVatReturnAuditModel

@Singleton
class SubmitFormController @Inject()(val messagesApi: MessagesApi,
                                     val vatSubscriptionService: VatSubscriptionService,
                                     val vatObligationsService: VatObligationsService,
                                     val mandationStatusCheck: MandationStatusPredicate,
                                     val errorHandler: ErrorHandler,
//                                     val auditService: AuditService,
                                     authPredicate: AuthPredicate,
                                     implicit val appConfig: AppConfig,
                                     val dateService: DateService) extends FrontendController with I18nSupport {

  def show(periodKey: String): Action[AnyContent] = (authPredicate andThen mandationStatusCheck).async { implicit user =>

//    auditService.audit(
//      Journey(user, sessionData, periodKey),
//      Some(controllers.routes.ConfirmSubmissionController.submit(periodKey).url)
//    )

    user.session.get(SessionKeys.returnData) match {
      case Some(model) => renderViewWithSessionData(periodKey, model)
      case _ => renderViewWithoutSessionData(periodKey, SubmitVatReturnForm.nineBoxForm)
    }
  }

  private def renderViewWithSessionData(periodKey: String, model: String)(implicit request: Request[_], user: User[_], hc: HeaderCarrier) = {

    val sessionData = Json.parse(model).as[SubmitVatReturnModel]

    val nineBoxModel = NineBoxModel(
      sessionData.box1,
      sessionData.box2,
      sessionData.box3,
      sessionData.box4,
      sessionData.box5,
      sessionData.box6,
      sessionData.box7,
      sessionData.box8,
      sessionData.box9
    )

    vatSubscriptionService.getCustomerDetails(user.vrn) map {
      case Right(customerDetails) =>
        Ok(views.html.submit_form(
          periodKey,
          customerDetails.clientName,
          sessionData.flatRateScheme,
          VatObligation(sessionData.start, sessionData.end, sessionData.due, periodKey),
          SubmitVatReturnForm.nineBoxForm.fill(nineBoxModel),
          isAgent = user.isAgent)
        )
      case _ =>
        Ok(views.html.submit_form(
          periodKey,
          None,
          sessionData.flatRateScheme,
          VatObligation(sessionData.start, sessionData.end, sessionData.due, periodKey),
          SubmitVatReturnForm.nineBoxForm.fill(nineBoxModel),
          isAgent = user.isAgent)
        )
    }
  }

  private def renderViewWithoutSessionData(periodKey: String, form: Form[NineBoxModel])(implicit request: Request[_], user: User[_], hc: HeaderCarrier) = {

    for {
      customerInformation <- vatSubscriptionService.getCustomerDetails(user.vrn)
      obligations <- vatObligationsService.getObligations(user.vrn)
    } yield {
      (customerInformation, obligations) match {
        case (Right(customerDetails), Right(obs)) =>

          val decodedPeriodKey: String = URLDecoder.decode(periodKey, "utf-8")

          obs.obligations.find(_.periodKey == decodedPeriodKey) match {
            case Some(obligation) =>
              if(dateService.dateHasPassed(obligation.end)) {
                val viewModel = SubmitFormViewModel(
                  customerDetails.hasFlatRateScheme,
                  obligation.start,
                  obligation.end,
                  obligation.due
                )

                Ok(views.html.submit_form(
                  periodKey,
                  customerDetails.clientName,
                  customerDetails.hasFlatRateScheme,
                  obligation,
                  form,
                  user.isAgent
                )).addingToSession(SessionKeys.viewModel -> Json.toJson(viewModel).toString())

              } else {
                Logger.debug(s"[SubmitFormController][renderViewWithoutSessionData] Obligation end date for period $periodKey has not yet passed.")
                errorHandler.showBadRequestError
              }
            case _ =>
              Logger.warn(s"[SubmitFormController][Show]: Obligation not found for period key $periodKey")
              Redirect(appConfig.returnDeadlinesUrl)
          }
        case (_, _) => errorHandler.showInternalServerError
      }
    }
  }

  def submit(periodKey: String): Action[AnyContent] = (authPredicate andThen mandationStatusCheck).async { implicit user =>

    validateBoxCalculations(SubmitVatReturnForm.nineBoxForm.bindFromRequest()).fold(
      failure => {

        user.session.get(SessionKeys.viewModel) match {
          case Some(model) =>

            val sessionData = Json.parse(model).as[SubmitFormViewModel]

            vatSubscriptionService.getCustomerDetails(user.vrn) map {
              case (Right(customerDetails)) =>
                BadRequest(views.html.submit_form(
                  periodKey,
                  customerDetails.clientName,
                  sessionData.hasFlatRateScheme,
                  VatObligation(sessionData.start, sessionData.end, sessionData.due, periodKey),
                  failure,
                  user.isAgent)
                )
              case (_) =>
                BadRequest(views.html.submit_form(
                  periodKey,
                  None,
                  sessionData.hasFlatRateScheme,
                  VatObligation(sessionData.start, sessionData.end, sessionData.due, periodKey),
                  failure,
                  isAgent = user.isAgent)
                )
            }
          case _ => renderViewWithoutSessionData(periodKey, failure)
        }
      },
      success => {
        submitSuccess(success, periodKey)
      }
    )
  }

  private def submitSuccess(model: NineBoxModel, periodKey: String)(implicit request: Request[_], user: User[_], hc: HeaderCarrier) = {
    for {
      customerInformation <- vatSubscriptionService.getCustomerDetails(user.vrn)
      obligations <- vatObligationsService.getObligations(user.vrn)
    } yield {
      (customerInformation, obligations) match {

        case (Right(customerDetails), Right(obs)) =>

          val decodedPeriodKey: String = URLDecoder.decode(periodKey, "utf-8")

          obs.obligations.find(_.periodKey == decodedPeriodKey) match {
            case Some(obligation) =>
              if(dateService.dateHasPassed(obligation.end)){
                val sessionModel = SubmitVatReturnModel(
                  model.box1,
                  model.box2,
                  model.box3,
                  model.box4,
                  model.box5,
                  model.box6,
                  model.box7,
                  model.box8,
                  model.box9,
                  customerDetails.hasFlatRateScheme,
                  obligation.start,
                  obligation.end,
                  obligation.due)
                Redirect(controllers.routes.ConfirmSubmissionController.show(periodKey))
                  .addingToSession(SessionKeys.returnData -> Json.toJson(sessionModel).toString())
                  .removingFromSession(SessionKeys.viewModel)
              } else {
                Logger.debug(s"[SubmitFormController][submitSuccess] Obligation end date for period $periodKey has not yet passed.")
                errorHandler.showBadRequestError
              }
            case _ =>
              Logger.warn(s"[SubmitFormController][submitSuccess]: Obligation not found for period key $periodKey")
              Redirect(appConfig.returnDeadlinesUrl)
          }
        case (Left(error), _) =>
          Logger.warn(s"[SubmitFormController][submitSuccess] Error received when retrieving customer details $error")
          errorHandler.showInternalServerError
        case (_, Left(error)) =>
          Logger.warn(s"[SubmitFormController][submitSuccess] Error received when retrieving obligation details $error")
          errorHandler.showInternalServerError
      }
    }
  }
}
