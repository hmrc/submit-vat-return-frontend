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

import audit.AuditService
import audit.models.SubmitVatReturnAuditModel
import common.SessionKeys
import config.{AppConfig, ErrorHandler}
import controllers.predicates.{AuthPredicate, MandationStatusPredicate}
import javax.inject.{Inject, Singleton}
import models.vatReturnSubmission.SubmissionModel
import models.{ConfirmSubmissionViewModel, SubmitVatReturnModel}
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._
import services.{DateService, VatReturnsService, VatSubscriptionService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class ConfirmSubmissionController @Inject()(val messagesApi: MessagesApi,
                                            val mandationStatusCheck: MandationStatusPredicate,
                                            val errorHandler: ErrorHandler,
                                            val vatSubscriptionService: VatSubscriptionService,
                                            authPredicate: AuthPredicate,
                                            vatReturnsService: VatReturnsService,
                                            val auditService: AuditService,
                                            implicit val executionContext: ExecutionContext,
                                            implicit val appConfig: AppConfig,
                                            val dateService: DateService) extends FrontendController with I18nSupport {

  def show(periodKey: String): Action[AnyContent] = (authPredicate andThen mandationStatusCheck).async { implicit user =>

    user.session.get(SessionKeys.returnData) match {
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

  def submit(periodKey: String): Action[AnyContent] = (authPredicate andThen mandationStatusCheck) async {
    implicit user =>
      user.session.get(SessionKeys.returnData) match {
        case Some(data) =>
          Try(Json.parse(data).as[SubmitVatReturnModel]) match {
            case Success(model) =>
              if(dateService.dateHasPassed(model.end)) {
                val submissionModel = SubmissionModel(
                  periodKey = periodKey,
                  vatDueSales = model.box1,
                  vatDueAcquisitions = model.box2,
                  vatDueTotal = model.box3,
                  vatReclaimedCurrPeriod = model.box4,
                  vatDueNet = model.box5,
                  totalValueSalesExVAT = model.box6,
                  totalValuePurchasesExVAT = model.box7,
                  totalValueGoodsSuppliedExVAT = model.box8,
                  totalAllAcquisitionsExVAT = model.box9,
                  agentReferenceNumber = user.arn
                )
                vatReturnsService.submitVatReturn(user.vrn, submissionModel) map {
                  case Right(_) =>
                    auditService.audit(
                      SubmitVatReturnAuditModel(user, model, periodKey),
                      Some(controllers.routes.ConfirmSubmissionController.submit(periodKey).url)
                    )
                    Redirect(controllers.routes.ConfirmationController.show().url).removingFromSession(SessionKeys.returnData)
                  case Left(error) =>
                    Logger.warn(s"[ConfirmSubmissionController][submit] Error returned from vat-returns service: $error")
                    InternalServerError(views.html.errors.submission_error())
                }
              } else {
                Logger.debug(s"[ConfirmSubmissionController][submit] Obligation end date for period $periodKey has not yet passed.")
                Future.successful(errorHandler.showBadRequestError)
              }
            case Failure(error) =>
              Logger.warn(s"[ConfirmSubmissionController][submit] Invalid session data found for key: ${SessionKeys.returnData}")
              Logger.debug(s"[ConfirmSubmissionController][submit] Invalid session data found for key: ${SessionKeys.returnData}. Error: $error")
              Future.successful(Redirect(controllers.routes.SubmitFormController.show(periodKey))
                .removingFromSession(SessionKeys.returnData))
          }
        case None =>
          Logger.warn(s"[ConfirmSubmissionController][submit] Required session data not found for key: ${SessionKeys.returnData}." +
            "Redirecting to 9 box entry page.")
          Future.successful(Redirect(controllers.routes.SubmitFormController.show(periodKey)))
      }
  }
}
