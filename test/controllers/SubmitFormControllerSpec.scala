/*
 * Copyright 2021 HM Revenue & Customs
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

import assets.CustomerDetailsTestAssets._
import audit.mocks.MockAuditingService
import base.BaseSpec
import common.{MandationStatuses, SessionKeys}
import connectors.httpParsers.ResponseHttpParsers.HttpGetResult
import forms.SubmitVatReturnForm
import mocks.service.{MockDateService, MockVatObligationsService, MockVatSubscriptionService}
import mocks.{MockAuth, MockHonestyDeclarationAction, MockMandationPredicate}
import models._
import models.auth.User
import models.errors.UnexpectedJsonFormat
import play.api.data.Form
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.SubmitForm

import scala.concurrent.Future

class SubmitFormControllerSpec extends BaseSpec
  with MockVatSubscriptionService
  with MockVatObligationsService
  with MockAuth
  with MockMandationPredicate
  with MockDateService
  with MockAuditingService
  with MockHonestyDeclarationAction {

  val submitForm: SubmitForm = inject[SubmitForm]

  val vatSubscriptionResponse: Future[HttpGetResult[CustomerDetails]] = Future.successful(Right(customerDetailsWithFRS))
  val vatSubscriptionFailureResponse: Future[HttpGetResult[CustomerDetails]] = Future.successful(Left(UnexpectedJsonFormat))

  val obligations: VatObligations = VatObligations(Seq(
    VatObligation(
      LocalDate.parse("2019-01-12"),
      LocalDate.parse("2019-04-12"),
      LocalDate.parse("2019-05-12"),
      "18AA"
    ),
    VatObligation(
      LocalDate.parse("2019-01-12"),
      LocalDate.now().plusYears(1),
      LocalDate.parse("2019-05-12"),
      "18AB"
    )
  ))

  val vatObligationsResponse: Future[HttpGetResult[VatObligations]] = Future.successful(Right(obligations))
  val vatObligationsErrorResponse: Future[HttpGetResult[VatObligations]] = Future.successful(Left(UnexpectedJsonFormat))

  object TestSubmitFormController extends SubmitFormController(
    mcc,
    mockVatSubscriptionService,
    mockVatObligationsService,
    mockMandationStatusPredicate,
    mockDateService,
    mockAuditService,
    mockAuthPredicate,
    mockHonestyDeclarationAction,
    submitForm,
    errorHandler,
    mockAppConfig,
    ec
  )

  def viewAsString[A](form: Form[NineBoxModel],
                      clientName: Option[String] = Some("ABC Solutions"))(user: User[A]): String = submitForm(
    "18AA",
    clientName,
    flatRateScheme = true,
    obligations.obligations.head,
    form,
    isAgent = false,
    nIProtocolEnabled = false
  )(messages, mockAppConfig, user).toString

  "SubmitFormController .show" when {

    "user is authorised" when {

      "there is a SubmitVatReturn model in session" when {

        val model = Map(
          "box1" -> "1000",
          "box2" -> "1000",
          "box3" -> "1000",
          "box4" -> "1000",
          "box5" -> "1000",
          "box6" -> "1000",
          "box7" -> "1000",
          "box8" -> "1000",
          "box9" -> "1000"
        )

        val nineBoxModel: String = Json.stringify(Json.toJson(
          SubmitVatReturnModel(
            1000.00,
            1000.00,
            1000.00,
            1000.00,
            1000.00,
            1000.00,
            1000.00,
            1000.00,
            1000.00,
            flatRateScheme = true,
            LocalDate.parse("2019-01-12"),
            LocalDate.parse("2019-04-12"),
            LocalDate.parse("2019-05-12")
          )
        ))

        "a successful response is received from the service" should {

          lazy val requestWithSessionData = User[AnyContentAsEmpty.type]("123456789")(fakeRequest.withSession(
            SessionKeys.returnData -> nineBoxModel,
            SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB,
            SessionKeys.HonestyDeclaration.key -> s"$vrn-18AA",
            SessionKeys.insolventWithoutAccessKey -> "false"
          ))

          lazy val result: Future[Result] = {
            mockAppConfig.features.nineBoxNIProtocolContentEnabled(false)
            TestSubmitFormController.show("18AA")(requestWithSessionData)
          }

          "return 200" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            setupAuditExtendedEvent
            setupVatSubscriptionService(vatSubscriptionResponse)
            status(result) shouldBe Status.OK
          }

          "return HTML" in {
            contentType(result) shouldBe Some("text/html")
          }

          "return the correct view" in {
            contentAsString(result) shouldBe viewAsString(SubmitVatReturnForm().nineBoxForm.bind(model))(requestWithSessionData)
          }
        }

        "an unsuccessful response is received from the service" should {

          lazy val requestWithSessionData = User[AnyContentAsEmpty.type]("123456789")(fakeRequest.withSession(
            SessionKeys.returnData -> nineBoxModel,
            SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB,
            SessionKeys.HonestyDeclaration.key -> s"$vrn-18AA")
          )

          lazy val result: Future[Result] = {
            TestSubmitFormController.show("18AA")(requestWithSessionData)
          }

          "return 200" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            setupAuditExtendedEvent
            setupVatSubscriptionService(vatSubscriptionFailureResponse)
            status(result) shouldBe Status.OK
          }

          "return HTML" in {
            contentType(result) shouldBe Some("text/html")
          }

          "return the correct view" in {
            contentAsString(result) shouldBe viewAsString(SubmitVatReturnForm().nineBoxForm.bind(model), None)(requestWithSessionData)
          }
        }
      }

      "there is no view model in session" when {

        "a successful response is received from the service" when {

          "obligation end date is in the past" should {

            val obligations: VatObligations = VatObligations(Seq(
              VatObligation(
                LocalDate.parse("2019-01-12"),
                LocalDate.parse("2019-04-12"),
                LocalDate.parse("2019-05-12"),
                "18AA"
              )
            ))

            val vatObligationsResponse: Future[HttpGetResult[VatObligations]] = Future.successful(Right(obligations))

            lazy val result = {
              TestSubmitFormController.show("18AA")(fakeRequest.withSession(
                SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB,
                SessionKeys.HonestyDeclaration.key -> s"$vrn-18AA",
                SessionKeys.insolventWithoutAccessKey -> "false"
              ))
            }

            "return 200" in {
              mockAuthorise(mtdVatAuthorisedResponse)
              setupAuditExtendedEvent
              setupVatSubscriptionService(vatSubscriptionResponse)
              setupVatObligationsService(vatObligationsResponse)
              mockDateHasPassed(response = true)
              status(result) shouldBe Status.OK
            }

            "return HTML" in {
              contentType(result) shouldBe Some("text/html")
            }

            "return the correct view" in {
              contentAsString(result) shouldBe viewAsString(SubmitVatReturnForm().nineBoxForm)(user)
            }
          }

          "obligation end date is in the future" should {

            lazy val result = {
              TestSubmitFormController.show("18AA")(fakeRequest.withSession(
                SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB,
                SessionKeys.HonestyDeclaration.key -> s"$vrn-18AA"
              ))
            }

            "return 400" in {
              mockAuthorise(mtdVatAuthorisedResponse)
              setupAuditExtendedEvent
              setupVatSubscriptionService(vatSubscriptionResponse)
              setupVatObligationsService(vatObligationsResponse)
              mockDateHasPassed(response = false)
              status(result) shouldBe Status.BAD_REQUEST
            }

            "render generic Bad Request page" in {
              contentAsString(result) shouldBe errorHandler.badRequestTemplate.toString()
            }
          }
        }

        "an obligation doesn't match the provided period key" should {

          lazy val result = {
            TestSubmitFormController.show("18AA")(fakeRequest.withSession(
              SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB,
              SessionKeys.HonestyDeclaration.key -> s"$vrn-18AA",
              SessionKeys.insolventWithoutAccessKey -> "false"
            ))
          }

          val badPeriodKeyObs: VatObligations = VatObligations(Seq(
            VatObligation(
              LocalDate.parse("2019-01-12"),
              LocalDate.parse("2019-04-12"),
              LocalDate.parse("2019-05-12"),
              "17AA"
            )
          ))

          val badPeriodKeyObsResponse: Future[HttpGetResult[VatObligations]] = Future.successful(Right(badPeriodKeyObs))

          "return a 303" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            setupAuditExtendedEvent
            setupVatSubscriptionService(vatSubscriptionResponse)
            setupVatObligationsService(badPeriodKeyObsResponse)
            status(result) shouldBe Status.SEE_OTHER
          }

          s"redirect to ${mockAppConfig.returnDeadlinesUrl}" in {
            redirectLocation(result) shouldBe Some(mockAppConfig.returnDeadlinesUrl)
          }
        }

        "an error response is returned from the service" should {

          "return an internal server status" in {

            val vatSubscriptionErrorResponse: Future[HttpGetResult[CustomerDetails]] = Future.successful(Left(UnexpectedJsonFormat))
            val vatObligationsErrorResponse: Future[HttpGetResult[VatObligations]] = Future.successful(Left(UnexpectedJsonFormat))

            mockAuthorise(mtdVatAuthorisedResponse)
            setupAuditExtendedEvent
            setupVatSubscriptionService(vatSubscriptionErrorResponse)
            setupVatObligationsService(vatObligationsErrorResponse)

            lazy val result = TestSubmitFormController.show("18AA")(fakeRequest.withSession(
              SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB,
              SessionKeys.HonestyDeclaration.key -> s"$vrn-18AA",
              SessionKeys.insolventWithoutAccessKey -> "false"
            ))
            status(result) shouldBe Status.INTERNAL_SERVER_ERROR

          }
        }
      }

    }

    authControllerChecks(TestSubmitFormController.show("18AA"), fakeRequest)
  }

  "SubmitFormController .submit" when {

    "submission data is valid" when {

      "service calls are successful" when {

        "matching obligation is found for period key" should {

          lazy val request = FakeRequest().withFormUrlEncodedBody(
            "box1" -> "1000.11",
            "box2" -> "1000",
            "box3" -> "2000.11",
            "box4" -> "1000",
            "box5" -> "1000.11",
            "box6" -> "1000",
            "box7" -> "1000",
            "box8" -> "1234567890123",
            "box9" -> "1234567890123"
          ).withSession(
            SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB,
            SessionKeys.HonestyDeclaration.key -> s"$vrn-18AA",
            SessionKeys.insolventWithoutAccessKey -> "false",
            SessionKeys.futureInsolvencyBlock -> "false"
          )

          lazy val result = {
            TestSubmitFormController.submit(periodKey = "18AA")(request)
          }

          "status is SEE_OTHER" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            mockDateHasPassed(response = true)
            setupVatSubscriptionService(vatSubscriptionResponse)
            setupVatObligationsService(vatObligationsResponse)
            status(result) shouldBe SEE_OTHER
          }

          s"redirect to ${controllers.routes.ConfirmationController.show()}" in {
            redirectLocation(result).get.contains(controllers.routes.ConfirmSubmissionController.show("18AA").url) shouldBe true
          }
        }

        "no matching obligation is found for period key" should {

          lazy val request = FakeRequest().withFormUrlEncodedBody(
            "box1" -> "1000.11",
            "box2" -> "1000",
            "box3" -> "2000.11",
            "box4" -> "1000",
            "box5" -> "1000.11",
            "box6" -> "1000",
            "box7" -> "1000",
            "box8" -> "1234567890123",
            "box9" -> "1234567890123"
          ).withSession(
            SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB,
            SessionKeys.HonestyDeclaration.key -> s"$vrn-18AA",
            SessionKeys.insolventWithoutAccessKey -> "false",
            SessionKeys.futureInsolvencyBlock -> "false"
          )

          lazy val result = {
            TestSubmitFormController.submit(periodKey = "18AA")(request)
          }

          val badPeriodKeyObs: VatObligations = VatObligations(Seq(
            VatObligation(
              LocalDate.parse("2019-01-12"),
              LocalDate.parse("2019-04-12"),
              LocalDate.parse("2019-05-12"),
              "17AA"
            )
          ))

          val badPeriodKeyObsResponse: Future[HttpGetResult[VatObligations]] = Future.successful(Right(badPeriodKeyObs))

          "return a 303" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            setupVatSubscriptionService(vatSubscriptionResponse)
            setupVatObligationsService(badPeriodKeyObsResponse)
            status(result) shouldBe Status.SEE_OTHER
          }

          s"redirect to ${mockAppConfig.returnDeadlinesUrl}" in {
            redirectLocation(result) shouldBe Some(mockAppConfig.returnDeadlinesUrl)
          }
        }
      }

      "the call to retrieve obligations fails" should {

        lazy val request = FakeRequest().withFormUrlEncodedBody(
          "box1" -> "1000.11",
          "box2" -> "1000",
          "box3" -> "2000.11",
          "box4" -> "1000",
          "box5" -> "1000.11",
          "box6" -> "1000",
          "box7" -> "1000",
          "box8" -> "1234567890123",
          "box9" -> "1234567890123"
        ).withSession(
          SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB,
          SessionKeys.HonestyDeclaration.key -> s"$vrn-18AA",
          SessionKeys.insolventWithoutAccessKey -> "false",
          SessionKeys.futureInsolvencyBlock -> "false"
        )

        lazy val result = {
          TestSubmitFormController.submit(periodKey = "18AA")(request)
        }

        "status is INTERNAL_SERVER_ERROR" in {
          mockAuthorise(mtdVatAuthorisedResponse)
          setupVatSubscriptionService(vatSubscriptionResponse)
          setupVatObligationsService(vatObligationsErrorResponse)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }

      "the call to retrieve customer information fails" should {
        lazy val request = FakeRequest().withFormUrlEncodedBody(
          "box1" -> "1000.11",
          "box2" -> "1000",
          "box3" -> "2000.11",
          "box4" -> "1000",
          "box5" -> "1000.11",
          "box6" -> "1000",
          "box7" -> "1000",
          "box8" -> "1234567890123",
          "box9" -> "1234567890123"
        ).withSession(
          SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB,
          SessionKeys.HonestyDeclaration.key -> s"$vrn-18AA",
          SessionKeys.insolventWithoutAccessKey -> "false",
          SessionKeys.futureInsolvencyBlock -> "false"
        )

        lazy val result = {
          TestSubmitFormController.submit(periodKey = "18AA")(request)
        }

        "status is INTERNAL_SERVER_ERROR" in {
          mockAuthorise(mtdVatAuthorisedResponse)
          setupVatSubscriptionService(vatSubscriptionFailureResponse)
          setupVatObligationsService(vatObligationsResponse)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }
    }

    "matching obligation end date is in the future" should {

      lazy val request = FakeRequest().withFormUrlEncodedBody(
        "box1" -> "1000.11",
        "box2" -> "1000",
        "box3" -> "2000.11",
        "box4" -> "1000",
        "box5" -> "1000.11",
        "box6" -> "1000",
        "box7" -> "1000",
        "box8" -> "1234567890123",
        "box9" -> "1234567890123",
        "flatRateScheme" -> "true",
        "start" -> "2019-01-01",
        "end" -> "2019-01-04",
        "due" -> "2019-01-05"
      ).withSession(
        SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB,
        SessionKeys.HonestyDeclaration.key -> s"$vrn-18AA",
        SessionKeys.insolventWithoutAccessKey -> "false",
        SessionKeys.futureInsolvencyBlock -> "false"
      )

      lazy val result = {
        TestSubmitFormController.submit(periodKey = "18AA")(request)
      }

      "return 400" in {
        mockAuthorise(mtdVatAuthorisedResponse)
        mockDateHasPassed(response = false)
        setupVatSubscriptionService(vatSubscriptionResponse)
        setupVatObligationsService(vatObligationsResponse)
        status(result) shouldBe BAD_REQUEST
      }

      "render generic Bad Request page" in {
        contentAsString(result) shouldBe errorHandler.badRequestTemplate(request).toString()
      }
    }

    "submission data is invalid" when {

      "there is a Submit Form view model in session" when {

        val sessionModel: String = Json.toJson(
          SubmitFormViewModel(
            hasFlatRateScheme = true,
            LocalDate.parse("2019-01-12"),
            LocalDate.parse("2019-04-12"),
            LocalDate.parse("2019-05-12"))
        ).toString()

        "an error occurs (unentered value in a box)" should {

          val inputs = Map(
            "box1" -> "1000",
            "box2" -> "1000",
            "box3" -> "2000",
            "box4" -> "1000",
            "box5" -> "3000",
            "box6" -> "1000",
            "box7" -> "1000",
            "box8" -> "1000",
            "box9" -> ""
          )

          lazy val request = FakeRequest().withFormUrlEncodedBody(
            "box1" -> "1000",
            "box2" -> "1000",
            "box3" -> "2000",
            "box4" -> "1000",
            "box5" -> "3000",
            "box6" -> "1000",
            "box7" -> "1000",
            "box8" -> "1000",
            "box9" -> "",
            "flatRateScheme" -> "true",
            "start" -> "2019-01-12",
            "end" -> "2019-04-12",
            "due" -> "2019-05-12"
          ).withSession(
            SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB,
            SessionKeys.viewModel -> sessionModel,
            SessionKeys.HonestyDeclaration.key -> s"$vrn-18AA",
            SessionKeys.insolventWithoutAccessKey -> "false",
            SessionKeys.futureInsolvencyBlock -> "false"
          )

          lazy val user = User("")(request)
          lazy val result = {
            TestSubmitFormController.submit(periodKey = "18AA")(request)
          }

          "status is BAD_REQUEST" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            setupVatSubscriptionService(vatSubscriptionResponse)
            status(result) shouldBe BAD_REQUEST
          }

          "return the correct view with errors" in {
            contentAsString(result) shouldBe viewAsString(form = SubmitVatReturnForm().nineBoxForm.bind(inputs))(user)
          }
        }

        "an error occurs (too many numbers)" should {

          val inputs = Map(
            "box1" -> "1000",
            "box2" -> "1000",
            "box3" -> "2000",
            "box4" -> "12345.000",
            "box5" -> "3000",
            "box6" -> "1000",
            "box7" -> "1000",
            "box8" -> "12345",
            "box9" -> "1234567890123456"
          )

          lazy val request = FakeRequest().withFormUrlEncodedBody(
            "box1" -> "1000",
            "box2" -> "1000",
            "box3" -> "2000",
            "box4" -> "12345.000",
            "box5" -> "3000",
            "box6" -> "1000",
            "box7" -> "1000",
            "box8" -> "12345",
            "box9" -> "1234567890123456",
            "flatRateScheme" -> "true",
            "start" -> "2019-01-12",
            "end" -> "2019-04-12",
            "due" -> "2019-05-12"
          ).withSession(
            SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB,
            SessionKeys.viewModel -> sessionModel,
            SessionKeys.HonestyDeclaration.key -> s"$vrn-18AA",
            SessionKeys.insolventWithoutAccessKey -> "false",
            SessionKeys.futureInsolvencyBlock -> "false"
          )

          lazy val user = User("")(request)
          lazy val result = {
            TestSubmitFormController.submit(periodKey = "18AA")(request)
          }

          "status is BAD_REQUEST" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            setupVatSubscriptionService(vatSubscriptionResponse)
            status(result) shouldBe BAD_REQUEST
          }

          "return the correct view with errors" in {
            contentAsString(result) shouldBe viewAsString(form = SubmitVatReturnForm().nineBoxForm.bind(inputs))(user)
          }
        }

        "an error occurs (incorrect format)" when {

          val inputs = Map(
            "box1" -> "1000",
            "box2" -> "1000",
            "box3" -> "2000",
            "box4" -> "1000",
            "box5" -> "3000",
            "box6" -> "1000",
            "box7" -> "12345",
            "box8" -> "12345",
            "box9" -> "1234+][567,./;'#890123456"
          )

          lazy val request = FakeRequest().withFormUrlEncodedBody(
            "box1" -> "1000",
            "box2" -> "1000",
            "box3" -> "2000",
            "box4" -> "1000",
            "box5" -> "3000",
            "box6" -> "1000",
            "box7" -> "12345",
            "box8" -> "12345",
            "box9" -> "1234+][567,./;'#890123456",
            "flatRateScheme" -> "true",
            "start" -> "2019-01-12",
            "end" -> "2019-04-12",
            "due" -> "2019-05-12"
          ).withSession(
            SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB,
            SessionKeys.viewModel -> sessionModel,
            SessionKeys.HonestyDeclaration.key -> s"$vrn-18AA",
            SessionKeys.insolventWithoutAccessKey -> "false",
            SessionKeys.futureInsolvencyBlock -> "false"
          )

          lazy val user = User("")(request)
          lazy val result = {
            TestSubmitFormController.submit(periodKey = "18AA")(request)
          }

          "status is BAD_REQUEST" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            setupVatSubscriptionService(vatSubscriptionResponse)
            status(result) shouldBe BAD_REQUEST
          }

          "return the correct view with errors" in {
            contentAsString(result) shouldBe viewAsString(form = SubmitVatReturnForm().nineBoxForm.bind(inputs))(user)
          }
        }

        "an unsuccessful response is received from the service" should {

          val inputs = Map(
            "box1" -> "1000",
            "box2" -> "1000",
            "box3" -> "2000",
            "box4" -> "1000",
            "box5" -> "3000",
            "box6" -> "1000",
            "box7" -> "1000",
            "box8" -> "1000",
            "box9" -> ""
          )

          val vatSubscriptionFailureResponse: Future[HttpGetResult[CustomerDetails]] = Future.successful(Left(UnexpectedJsonFormat))

          lazy val request = FakeRequest().withFormUrlEncodedBody(
            "box1" -> "1000",
            "box2" -> "1000",
            "box3" -> "2000",
            "box4" -> "1000",
            "box5" -> "3000",
            "box6" -> "1000",
            "box7" -> "1000",
            "box8" -> "1000",
            "box9" -> "",
            "flatRateScheme" -> "true",
            "start" -> "2019-01-12",
            "end" -> "2019-04-12",
            "due" -> "2019-05-12"
          ).withSession(
            SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB,
            SessionKeys.viewModel -> sessionModel,
            SessionKeys.HonestyDeclaration.key -> s"$vrn-18AA",
            SessionKeys.insolventWithoutAccessKey -> "false",
            SessionKeys.futureInsolvencyBlock -> "false"
          )

          lazy val user = User("")(request)
          lazy val result: Future[Result] = {
            TestSubmitFormController.submit("18AA")(request)
          }

          "return BAD_REQUEST" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            setupVatSubscriptionService(vatSubscriptionFailureResponse)
            status(result) shouldBe Status.BAD_REQUEST
          }

          "return HTML" in {
            contentType(result) shouldBe Some("text/html")
          }

          "return the correct view with errors" in {
            contentAsString(result) shouldBe viewAsString(form = SubmitVatReturnForm().nineBoxForm.bind(inputs), clientName = None)(user)
          }
        }
      }

      "there is no model in session" should {

        lazy val requestWithError = FakeRequest().withFormUrlEncodedBody(
          "box1" -> "1000.11",
          "box2" -> "1000",
          "box3" -> "2000.11",
          "box4" -> "1000",
          "box5" -> "1000.11",
          "box6" -> "1000",
          "box7" -> "1000.3",
          "box8" -> "1234567890123",
          "box9" -> "",
          "flatRateScheme" -> "true",
          "start" -> "2019-01-12",
          "end" -> "2019-04-12",
          "due" -> "2019-05-12"
        ).withSession(
          SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB,
          SessionKeys.HonestyDeclaration.key -> s"$vrn-18AA",
          SessionKeys.insolventWithoutAccessKey -> "false",
          SessionKeys.futureInsolvencyBlock -> "false"
        )

        "a successful response is received from the service" should {

          val vatObligationsResponse: Future[HttpGetResult[VatObligations]] = Future.successful(Right(obligations))

          lazy val result = {
            TestSubmitFormController.submit("18AA")(requestWithError)
          }

          "return 200" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            mockDateHasPassed(response = true)
            setupVatSubscriptionService(vatSubscriptionResponse)
            setupVatObligationsService(vatObligationsResponse)
            status(result) shouldBe Status.OK
          }

          "return HTML" in {
            contentType(result) shouldBe Some("text/html")
          }
        }

        "an obligation doesn't match the provided period key" should {

          val obligations: VatObligations = VatObligations(Seq(
            VatObligation(
              LocalDate.parse("2019-01-12"),
              LocalDate.parse("2019-04-12"),
              LocalDate.parse("2019-05-12"),
              "17AA"
            )
          ))

          val vatObligationsResponse: Future[HttpGetResult[VatObligations]] = Future.successful(Right(obligations))

          lazy val result = {
            TestSubmitFormController.submit("18AA")(requestWithError)
          }

          "return a 303" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            setupVatSubscriptionService(vatSubscriptionResponse)
            setupVatObligationsService(vatObligationsResponse)
            status(result) shouldBe Status.SEE_OTHER
          }

          s"redirect to ${mockAppConfig.returnDeadlinesUrl}" in {
            redirectLocation(result) shouldBe Some(mockAppConfig.returnDeadlinesUrl)
          }

        }

        "an error response is returned from the service" should {

          "return an internal server status" in {

            val vatSubscriptionErrorResponse: Future[HttpGetResult[CustomerDetails]] = Future.successful(Left(UnexpectedJsonFormat))
            val vatObligationsErrorResponse: Future[HttpGetResult[VatObligations]] = Future.successful(Left(UnexpectedJsonFormat))

            mockAuthorise(mtdVatAuthorisedResponse)

            setupVatSubscriptionService(vatSubscriptionErrorResponse)
            setupVatObligationsService(vatObligationsErrorResponse)

            lazy val result = TestSubmitFormController.submit("18AA")(requestWithError)
            status(result) shouldBe Status.INTERNAL_SERVER_ERROR

          }
        }
      }
    }

    authControllerChecks(TestSubmitFormController.submit(periodKey = "93DH"), fakeRequest)
  }
}
