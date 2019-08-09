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

import java.net.URLDecoder
import java.time.{Instant, LocalDateTime, ZoneId}

import audit.AuditService
import audit.models.{NrsErrorAuditModel, SubmitNrsModel, SubmitVatReturnAuditModel}
import audit.models.journey.{FailureAuditModel, SuccessAuditModel}
import common.SessionKeys
import config.{AppConfig, ErrorHandler}
import controllers.predicates.{AuthPredicate, MandationStatusPredicate}
import javax.inject.{Inject, Singleton}
import models.auth.User
import models.errors.{BadRequestError, HttpError, ServerSideError}
import models.nrs.{IdentityData, IdentityLoginTimes}
import models.vatReturnSubmission.SubmissionModel
import models.{ConfirmSubmissionViewModel, CustomerDetails, SubmitVatReturnModel}
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Result, _}
import play.twirl.api.Html
import services.{DateService, EnrolmentsAuthService, VatReturnsService, VatSubscriptionService}
import uk.gov.hmrc.auth.core.AffinityGroup._
import uk.gov.hmrc.auth.core.AuthorisationException
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.{ItmpAddress, ItmpName, ~}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{HashUtil, ReceiptDataHelper}

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
                                            val dateService: DateService,
                                            authService: EnrolmentsAuthService,
                                            receiptDataHelper: ReceiptDataHelper
                                           ) extends FrontendController with I18nSupport {

  def show(periodKey: String): Action[AnyContent] = (authPredicate andThen mandationStatusCheck).async { implicit user =>

    user.session.get(SessionKeys.returnData) match {
      case Some(model) =>
        val sessionData = Json.parse(model).as[SubmitVatReturnModel]
        vatSubscriptionService.getCustomerDetails(user.vrn) map { model =>
          Ok(renderConfirmSubmissionView(periodKey, sessionData, model))
        }
      case _ => Future.successful(Redirect(controllers.routes.SubmitFormController.show(periodKey)))
    }
  }

  private def renderConfirmSubmissionView[A](periodKey: String,
                                             sessionData: SubmitVatReturnModel,
                                             customerDetails: Either[HttpError, CustomerDetails])(implicit user: User[A]): Html = {

    val clientName = customerDetails match {
      case (Right(model)) => model.clientName
      case _ => None
    }

    val viewModel = ConfirmSubmissionViewModel(sessionData, periodKey, clientName)
    views.html.confirm_submission(viewModel, user.isAgent)
  }

  def submit(periodKey: String): Action[AnyContent] = (authPredicate andThen mandationStatusCheck) async {
    implicit user =>
      user.session.get(SessionKeys.returnData) match {
        case Some(data) =>
          Try(Json.parse(data).as[SubmitVatReturnModel]) match {
            case Success(model) =>
              if(dateService.dateHasPassed(model.end)) {

                if(appConfig.features.nrsSubmissionEnabled()){
                  submitToNrs(periodKey, model)
                } else {
                  submitVatReturn(periodKey, model)
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

  private[controllers] def submitVatReturn[A](periodKey: String,
                                              sessionData: SubmitVatReturnModel)(implicit user: User[A]): Future[Result] = {
    val submissionModel = SubmissionModel(
      periodKey = URLDecoder.decode(periodKey, "utf-8"),
      vatDueSales = sessionData.box1,
      vatDueAcquisitions = sessionData.box2,
      vatDueTotal = sessionData.box3,
      vatReclaimedCurrPeriod = sessionData.box4,
      vatDueNet = sessionData.box5,
      totalValueSalesExVAT = sessionData.box6,
      totalValuePurchasesExVAT = sessionData.box7,
      totalValueGoodsSuppliedExVAT = sessionData.box8,
      totalAllAcquisitionsExVAT = sessionData.box9,
      agentReferenceNumber = user.arn
    )
    vatReturnsService.submitVatReturn(user.vrn, submissionModel) map {
      case Right(_) =>
        auditService.audit(
          SubmitVatReturnAuditModel(user, sessionData, periodKey),
          Some(controllers.routes.ConfirmSubmissionController.submit(periodKey).url)
        )
        auditService.audit(
          SuccessAuditModel(user.vrn, periodKey, user.arn),
          Some(controllers.routes.ConfirmSubmissionController.submit(periodKey).url)
        )
        Redirect(controllers.routes.ConfirmationController.show().url).removingFromSession(SessionKeys.returnData)
      case Left(error) =>
        auditService.audit(
          FailureAuditModel(user.vrn, periodKey, user.arn, error.message),
          Some(controllers.routes.ConfirmSubmissionController.submit(periodKey).url)
        )
        Logger.warn(s"[ConfirmSubmissionController][submitVatReturn] Error returned from vat-returns service: $error")
        InternalServerError(views.html.errors.submission_error())
    }
  }

  private[controllers] def submitToNrs[A](periodKey: String,
                                          sessionData: SubmitVatReturnModel)(implicit user: User[A]): Future[Result] = {
    for {
      customerDetails <- vatSubscriptionService.getCustomerDetails(user.vrn)
      html = renderConfirmSubmissionView(periodKey, sessionData, customerDetails)
      receiptData = receiptDataHelper.extractReceiptData(sessionData, customerDetails)
      htmlPayload = HashUtil.encode(html.body)
      sha256Checksum = HashUtil.getHash(html.body)
      identity <- buildIdentityData()
      result <- (identity, receiptData) match {
        case (Right(id), Right(receipt)) => vatReturnsService.nrsSubmission(periodKey, htmlPayload, sha256Checksum, id, receipt) flatMap {
          case Left(error: BadRequestError) =>
            Logger.debug(s"[ConfirmSubmissionController][submitToNRS] - NRS returned BAD_REQUEST: $error")
            Logger.warn("[ConfirmSubmissionController][submitToNRS] - NRS returned BAD_REQUEST")
            auditService.audit(
              NrsErrorAuditModel(user.vrn, sessionData.start, sessionData.end, sessionData.due, error.code),
              Some(controllers.routes.ConfirmSubmissionController.submit(periodKey).url)
            )
            Future.successful(InternalServerError(views.html.errors.submission_error()))
          case Right(success) =>
            auditService.audit(
              SubmitNrsModel(user.vrn, sessionData.start, sessionData.end, sessionData.due, success.nrSubmissionId),
              Some(controllers.routes.ConfirmSubmissionController.submit(periodKey).url)
            )
            submitVatReturn(periodKey, sessionData)
          case Left(other: ServerSideError) =>
            auditService.audit(
              NrsErrorAuditModel(user.vrn, sessionData.start, sessionData.end, sessionData.due, other.code),
              Some(controllers.routes.ConfirmSubmissionController.submit(periodKey).url)
            )
            submitVatReturn(periodKey, sessionData)
        }
        case (Left(errorResult), _) => Future(errorResult)
        case (_, Left(error)) =>
          Logger.debug(s"[ConfirmSubmissionController][submitToNRS] - extractReceiptData helper returned error of: $error")
          Logger.warn("[ConfirmSubmissionController][submitToNRS] - extractReceiptData helper returned error")
          Future.successful(InternalServerError(views.html.errors.submission_error()))
      }
    } yield result
  }

  private val authRetrievals = Retrievals.affinityGroup and Retrievals.internalId and
    Retrievals.externalId and Retrievals.agentCode and
    Retrievals.credentials and Retrievals.confidenceLevel and
    Retrievals.nino and Retrievals.saUtr and
    Retrievals.name and Retrievals.dateOfBirth and
    Retrievals.email and Retrievals.agentInformation and
    Retrievals.groupIdentifier and Retrievals.credentialRole and
    Retrievals.mdtpInformation and Retrievals.itmpName and
    Retrievals.itmpDateOfBirth and Retrievals.itmpAddress and
    Retrievals.credentialStrength and Retrievals.loginTimes

  private[controllers] def buildIdentityData[A]()(implicit user: User[A]): Future[Either[Result, IdentityData]] = {
    authService.authorised().retrieve(authRetrievals) {

      case affinityGroup ~ internalId ~
        externalId ~ agentCode ~
        credentials ~ confidenceLevel ~
        nino ~ saUtr ~
        name ~ dateOfBirth ~
        email ~ agentInfo ~
        groupId ~ credentialRole ~
        mdtpInfo ~ itmpName ~
        itmpDateOfBirth ~ itmpAddress ~
        credentialStrength ~ loginTimes =>

        val identityData = IdentityData(internalId, externalId, agentCode,
          credentials, confidenceLevel, nino, saUtr, name, dateOfBirth,
          email, agentInfo, groupIdentifier = groupId,
          credentialRole, mdtpInformation = mdtpInfo,
          itmpName = handleItmpName(itmpName), itmpDateOfBirth,
          itmpAddress = handleItmpAddress(itmpAddress), affinityGroup, credentialStrength,
          loginTimes = IdentityLoginTimes(
            LocalDateTime.ofInstant(Instant.parse(loginTimes.currentLogin.toInstant.toString), ZoneId.of("UTC")),
            loginTimes.previousLogin.map(dateTime => LocalDateTime.ofInstant(Instant.parse(dateTime.toInstant.toString), ZoneId.of("UTC")))
          )
        )

        Future.successful(Right(identityData))

    } recover {
      case exception: AuthorisationException =>
        Logger.warn(s"[ConfirmSubmissionController][buildIdentityData]" +
          s"Client authorisation failed due to internal server error. auth-client exception was $exception")
        Left(InternalServerError(views.html.errors.submission_error()))
    }
  }

  private[controllers] def handleItmpName(itmpName: Option[ItmpName]): ItmpName = itmpName.fold(ItmpName(None, None, None))(name => name)
  private[controllers] def handleItmpAddress(itmpAddress: Option[ItmpAddress]): ItmpAddress =
    itmpAddress.fold(ItmpAddress(None, None, None, None, None, None, None, None))(address => address)

}
