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
import assets.NrsTestData.IdentityDataTestData
import audit.mocks.MockAuditingService
import base.BaseSpec
import common.{MandationStatuses, SessionKeys}
import connectors.httpParsers.ResponseHttpParsers.HttpGetResult
import mocks.service.{MockDateService, MockVatReturnsService, MockVatSubscriptionService}
import mocks.{MockAuth, MockHonestyDeclarationAction, MockMandationPredicate, MockReceiptDataService}
import models.auth.User
import models.errors.{BadRequestError, UnexpectedJsonFormat, UnknownError}
import models.nrs.SuccessModel
import models.vatReturnSubmission.SubmissionSuccessModel
import models.{ConfirmSubmissionViewModel, CustomerDetails, SubmitVatReturnModel}
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.BearerTokenExpired
import uk.gov.hmrc.auth.core.retrieve._
import views.html.ConfirmSubmission
import views.html.errors.SubmissionError

import scala.concurrent.Future

class ConfirmSubmissionControllerSpec extends BaseSpec
  with MockAuth
  with MockMandationPredicate
  with MockVatSubscriptionService
  with MockVatReturnsService
  with MockDateService
  with MockAuditingService
  with MockReceiptDataService
  with MockHonestyDeclarationAction{

  val confirmSubmission: ConfirmSubmission = inject[ConfirmSubmission]
  val submissionError: SubmissionError = inject[SubmissionError]

  object TestConfirmSubmissionController extends ConfirmSubmissionController(
    mockMandationStatusPredicate,
    errorHandler,
    mockVatSubscriptionService,
    mockAuthPredicate,
    mockVatReturnsService,
    mockAuditService,
    mockHonestyDeclarationAction,
    mockDateService,
    mockEnrolmentsAuthService,
    mockReceiptDataService,
    mcc,
    confirmSubmission,
    submissionError,
    mockAppConfig,
    ec
  )

  val submitVatReturnModel: SubmitVatReturnModel = SubmitVatReturnModel(
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
    LocalDate.now(),
    LocalDate.now(),
    LocalDate.now()
  )

  def viewAsString(model: ConfirmSubmissionViewModel): String =
    confirmSubmission(model, isAgent = false)(messages, mockAppConfig, user).toString

  def errorViewAsString(): String = submissionError()(mockAppConfig, messages, user).toString

  "ConfirmSubmissionController .show" when {

    "user is authorised" when {

      val nineBoxModel: String = Json.stringify(Json.toJson(
        submitVatReturnModel
      ))

      "there is session data" when {

        "a successful response is returned from the vat subscription service" should {

          lazy val requestWithSessionData: User[AnyContentAsEmpty.type] =
            User[AnyContentAsEmpty.type]("123456789")(fakeRequest.withSession(
              SessionKeys.returnData -> nineBoxModel,
              SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB,
              SessionKeys.HonestyDeclaration.key -> s"$vrn-18AA"
            ))

          lazy val result: Future[Result] = {
            setupVatSubscriptionService(successCustomerInfoResponse)()
            TestConfirmSubmissionController.show("18AA")(requestWithSessionData)
          }

          "return 200" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            status(result) shouldBe Status.OK
          }

          "return HTML" in {
            contentType(result) shouldBe Some("text/html")
          }

          "return the correct view" in {
            contentAsString(result) shouldBe viewAsString(ConfirmSubmissionViewModel(submitVatReturnModel, "18AA", Some("ABC Solutions")))
          }

          "add obligation data to session" in {
            await(result).session.get("submissionYear").get shouldBe LocalDate.now().getYear.toString
            await(result).session.get("inSessionPeriodKey").get shouldBe "18AA"
          }
        }

        "an error response is returned from the vat subscription service" should {

          val vatSubscriptionResponse: Future[HttpGetResult[CustomerDetails]] = Future.successful(Left(UnexpectedJsonFormat))

          lazy val requestWithSessionData: User[AnyContentAsEmpty.type] =
            User[AnyContentAsEmpty.type]("123456789")(fakeRequest.withSession(
              SessionKeys.returnData -> nineBoxModel,
              SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB,
              SessionKeys.HonestyDeclaration.key -> s"$vrn-18AA"
            ))

          lazy val result: Future[Result] = {
            setupVatSubscriptionService(vatSubscriptionResponse)()
            TestConfirmSubmissionController.show("18AA")(requestWithSessionData)
          }

          "return 200" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            status(result) shouldBe Status.OK
          }

          "return HTML" in {
            contentType(result) shouldBe Some("text/html")
          }

          "return the correct view" in {
            contentAsString(result) shouldBe viewAsString(ConfirmSubmissionViewModel(submitVatReturnModel, "18AA", None))
          }
        }
      }

      "there is no session data" should {

        lazy val result: Future[Result] = {
          TestConfirmSubmissionController.show("18AA")(fakeRequest.withSession(
            SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB,
            SessionKeys.HonestyDeclaration.key -> s"$vrn-18AA")
          )
        }

        "return 303" in {
          mockAuthorise(mtdVatAuthorisedResponse)
          status(result) shouldBe Status.SEE_OTHER
        }

        s"redirect to ${controllers.routes.SubmitFormController.show("18AA").url}" in {
          redirectLocation(result) shouldBe Some(controllers.routes.SubmitFormController.show("18AA").url)
        }
      }
    }

    authControllerChecks(TestConfirmSubmissionController.show("18AA"), fakeRequest)
  }

  "ConfirmSubmissionController .submit" when {

    "user is authorised" when {

      "valid session data exists" when {

        val nineBoxData = Json.obj(
          "box1" -> "10.01",
          "box2" -> "10.02",
          "box3" -> "10.03",
          "box4" -> "10.04",
          "box5" -> "10.05",
          "box6" -> "10.06",
          "box7" -> "10.07",
          "box8" -> "10.08",
          "box9" -> "10.09",
          "flatRateScheme" -> false,
          "start" -> "2019-01-02",
          "end" -> "2019-01-02",
          "due" -> "2019-01-02"
        ).toString()

        "obligation end date is in the past" when {

          "a submission to the backend is successful" should {

            lazy val result: Future[Result] = {
              TestConfirmSubmissionController.submit("18AA")(fakeRequest.withSession(
                "mtdNineBoxReturnData" -> nineBoxData,
                SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB,
                SessionKeys.HonestyDeclaration.key -> s"$vrn-18AA"
              ))
            }

            "return 303" in {
              mockAuthorise(mtdVatAuthorisedResponse)
              mockDateHasPassed(response = true)
              mockVatReturnSubmission(Future.successful(Right(SubmissionSuccessModel("12345"))))()
              setupVatSubscriptionService(successCustomerInfoResponse)()
              mockFullAuthResponse(Future.successful(agentFullInformationResponse))
              mockNrsSubmission(Future.successful(Right(SuccessModel("1234567890"))))()
              mockExtractReceiptData(await(successReceiptDataResponse))()
              setupAuditExtendedEvent
              setupAuditExtendedEvent
              setupAuditExtendedEvent

              status(result) shouldBe Status.SEE_OTHER
            }

            s"redirect to ${controllers.routes.ConfirmationController.show}" in {
              redirectLocation(result) shouldBe Some(controllers.routes.ConfirmationController.show.url)
            }
          }

          "submission to backend is unsuccessful" should {

            lazy val result: Future[Result] = TestConfirmSubmissionController.submit("18AA")(fakeRequest.withSession(
              "mtdNineBoxReturnData" -> nineBoxData,
              SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB,
              SessionKeys.HonestyDeclaration.key -> s"$vrn-18AA"
            ))

            "return 500" in {
              mockAuthorise(mtdVatAuthorisedResponse)
              mockDateHasPassed(true)
              setupVatSubscriptionService(successCustomerInfoResponse)()
              mockExtractReceiptData(await(successReceiptDataResponse))()
              mockFullAuthResponse(Future.successful(agentFullInformationResponse))
              mockNrsSubmission(Future.successful(Right(SuccessModel("1234567890"))))()
              setupAuditExtendedEvent
              mockVatReturnSubmission(Future.successful(Left(UnknownError)))()
              setupAuditExtendedEvent

              status(result) shouldBe Status.INTERNAL_SERVER_ERROR
            }

            "return the correct view" in {
              contentAsString(result) shouldBe errorViewAsString()
            }
          }
        }

        "obligation end date is in the future" should {

          lazy val result: Future[Result] = TestConfirmSubmissionController.submit("18AA")(fakeRequest.withSession(
            "mtdNineBoxReturnData" -> nineBoxData,
            SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB,
            SessionKeys.HonestyDeclaration.key -> s"$vrn-18AA"
          ))

          "return 400" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            mockDateHasPassed(response = false)
            status(result) shouldBe Status.BAD_REQUEST
          }

          "render generic Bad Request page" in {
            contentAsString(result) shouldBe errorHandler.badRequestTemplate.toString()
          }
        }
      }

      "invalid session data exists" should {

        val nineBoxData = Json.obj("box10" -> "why").toString()
        lazy val result: Future[Result] = TestConfirmSubmissionController.submit("18AA")(fakeRequest.withSession(
          "mtdNineBoxReturnData" -> nineBoxData,
          SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB,
          SessionKeys.HonestyDeclaration.key -> s"$vrn-18AA"
        ))

        "return 303" in {
          mockAuthorise(mtdVatAuthorisedResponse)
          status(result) shouldBe Status.SEE_OTHER
        }

        s"redirect to ${controllers.routes.SubmitFormController.show("18AA").url}" in {
          redirectLocation(result) shouldBe Some(controllers.routes.SubmitFormController.show("18AA").url)
        }
      }

      "no session data exists for key" should {

        lazy val result: Future[Result] = TestConfirmSubmissionController.submit("18AA")(fakeRequest.withSession(
          SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB,
          SessionKeys.HonestyDeclaration.key -> s"$vrn-18AA"
        ))

        "return 303" in {
          mockAuthorise(mtdVatAuthorisedResponse)
          status(result) shouldBe Status.SEE_OTHER
        }

        s"redirect to ${controllers.routes.SubmitFormController.show("18AA").url}" in {
          redirectLocation(result) shouldBe Some(controllers.routes.SubmitFormController.show("18AA").url)
        }
      }
    }

    authControllerChecks(TestConfirmSubmissionController.submit("18AA"), fakeRequest)
  }

  "ConfirmSubmissionController .submitToNrs" when {

    implicit lazy val nrsUser: User[AnyContentAsEmpty.type] = User[AnyContentAsEmpty.type](vrn)(fakeRequest)

    "a Right is returned from buildIdentityData and the submission is successful" should {

      lazy val result = TestConfirmSubmissionController.submitToNrs("18AA", submitVatReturnModel)

      "return a SEE_OTHER" in {

        setupVatSubscriptionService(successCustomerInfoResponse)()
        mockFullAuthResponse(Future.successful(agentFullInformationResponse))
        mockNrsSubmission(Future.successful(Right(SuccessModel("1234567890"))))()
        mockVatReturnSubmission(Future.successful(Right(SubmissionSuccessModel("12345"))))()
        mockExtractReceiptData(await(successReceiptDataResponse))()
        setupAuditExtendedEvent
        setupAuditExtendedEvent
        setupAuditExtendedEvent

        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to Confirmation page" in {
        redirectLocation(result) shouldBe Some(controllers.routes.ConfirmationController.show.url)
      }
    }

    "a Left is returned from buildIdentityData" should {

      lazy val result = TestConfirmSubmissionController.submitToNrs("18AA", submitVatReturnModel)

      "return an ISE" in {
        setupVatSubscriptionService(successCustomerInfoResponse)()
        mockExtractReceiptData(await(successReceiptDataResponse))()
        mockFullAuthResponse(Future.failed(BearerTokenExpired()))

        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }

      "return the correct view" in {
        contentAsString(result) shouldBe errorViewAsString()
      }
    }

    "a Left is returned from extractReceiptData" should {

      lazy val result = TestConfirmSubmissionController.submitToNrs("18AA", submitVatReturnModel)

      "return an ISE" in {
        setupVatSubscriptionService(successCustomerInfoResponse)()
        mockFullAuthResponse(Future.successful(agentFullInformationResponse))
        mockExtractReceiptData(Left(UnknownError))()

        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }

      "return the correct view" in {
        contentAsString(result) shouldBe errorViewAsString()
      }
    }

    "a BAD_REQUEST is returned from Nrs Submission" should {

      lazy val result = TestConfirmSubmissionController.submitToNrs("18AA", submitVatReturnModel)

      "return an ISE" in {
        setupVatSubscriptionService(successCustomerInfoResponse)()
        mockFullAuthResponse(Future.successful(agentFullInformationResponse))
        mockExtractReceiptData(await(successReceiptDataResponse))()
        mockNrsSubmission(Future.successful(Left(BadRequestError("400", "error message"))))()
        setupAuditExtendedEvent

        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }

      "return the correct view" in {
        contentAsString(result) shouldBe errorViewAsString()
      }
    }
  }

  "ConfirmSubmissionController .buildIdentityData" when {

    "a successful call to the auth service is made" when {

      val expectedResponse = Right(IdentityDataTestData.correctModel)

      lazy val result = {
        mockFullAuthResponse(Future.successful(agentFullInformationResponse))
        TestConfirmSubmissionController.buildIdentityData()(user)
      }

      "return a full IdentityData model" in {
        await(result) shouldBe expectedResponse
      }
    }

    "an exception is returned from the auth service" should {

      lazy val result = {
        mockFullAuthResponse(Future.failed(BearerTokenExpired()))
        TestConfirmSubmissionController.buildIdentityData()(user)
      }

      "return an internal server error" in {
        status(Future.successful(await(result).left.get)) shouldBe 500
      }

      "return the correct view" in {
        contentAsString(Future.successful(await(result).left.get)) shouldBe errorViewAsString()
      }
    }
  }

  "ConfirmSubmissionController .handleITMPName" should {

    "return ITMP Name when one is available" in {

      val itmpName = ItmpName(Some("First"), Some("Middle"), Some("Last"))

      val expectedResult: ItmpName = itmpName

      val result: ItmpName = TestConfirmSubmissionController.handleItmpName(Some(itmpName))

      result shouldBe expectedResult

    }

    "return an empty ITMP Name when none is available" in {

      val result: ItmpName = TestConfirmSubmissionController.handleItmpName(None)

      result shouldBe ItmpName(None, None, None)

    }
  }

  "ConfirmSubmissionController .handleITMPAddress" should {

    "return an ITMP Address when one is available" in {

      val itmpAddress = ItmpAddress(Some("Line 1"), None, None, None, None, Some("Post code"), Some("United Kingdom"), Some("UK"))

      val expectedResult: ItmpAddress = itmpAddress

      val result: ItmpAddress = TestConfirmSubmissionController.handleItmpAddress(Some(itmpAddress))

      result shouldBe expectedResult

    }

    "return an empty ITMP Address when none is available" in {

      val result: ItmpAddress = TestConfirmSubmissionController.handleItmpAddress(None)

      result shouldBe ItmpAddress(None, None, None, None, None, None, None, None)

    }
  }

  "ConfirmSubmissionController .renderConfirmSubmissionView" should {

    "return html if all parameters are provided" in {

      TestConfirmSubmissionController.renderConfirmSubmissionView(
        vrn, submitVatReturnModel, await(successCustomerInfoResponse))(user).contentType shouldBe "text/html"
    }
  }
}
