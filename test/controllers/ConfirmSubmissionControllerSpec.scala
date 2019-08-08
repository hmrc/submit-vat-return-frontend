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

import assets.NrsTestData.IdentityDataTestData
import assets.messages.SubmissionErrorMessages
import audit.mocks.MockAuditingService
import base.BaseSpec
import common.{MandationStatuses, SessionKeys}
import connectors.httpParsers.ResponseHttpParsers.HttpGetResult
import mocks.service.{MockDateService, MockVatReturnsService, MockVatSubscriptionService}
import mocks.{MockAuth, MockMandationPredicate, MockReceiptDataService}
import models.auth.User
import models.errors.{BadRequestError, UnexpectedJsonFormat, UnknownError}
import models.nrs.SuccessModel
import models.vatReturnSubmission.SubmissionSuccessModel
import models.{CustomerDetails, SubmitVatReturnModel}
import org.joda.time.DateTime
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.auth.core.{AffinityGroup, BearerTokenExpired}

import scala.concurrent.Future

class ConfirmSubmissionControllerSpec extends BaseSpec
  with MockAuth
  with MockMandationPredicate
  with MockVatSubscriptionService
  with MockVatReturnsService
  with MockDateService
  with MockAuditingService
  with MockReceiptDataService {

  object TestConfirmSubmissionController extends ConfirmSubmissionController(
    messagesApi,
    mockMandationStatusPredicate,
    errorHandler,
    mockVatSubscriptionService,
    mockAuthPredicate,
    mockVatReturnsService,
    mockAuditService,
    ec,
    mockAppConfig,
    mockDateService,
    mockEnrolmentsAuthService,
    mockReceiptDataService
  )
  
  val submitVatReturnModel = SubmitVatReturnModel(
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
              SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB
            )
            )

          lazy val result: Future[Result] = {
            setupVatSubscriptionService(successCustomerInfoResponse)
            TestConfirmSubmissionController.show("18AA")(requestWithSessionData)
          }

          "return 200" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            status(result) shouldBe Status.OK
          }

          "return HTML" in {
            contentType(result) shouldBe Some("text/html")
          }

          "the user name should be displayed" in {
            Jsoup.parse(bodyOf(result)).select("#content > article > section > h2").text() shouldBe "ABC Solutions"
          }
        }

        "an error response is returned from the vat subscription service" should {

          val vatSubscriptionResponse: Future[HttpGetResult[CustomerDetails]] = Future.successful(Left(UnexpectedJsonFormat))

          lazy val requestWithSessionData: User[AnyContentAsEmpty.type] =
            User[AnyContentAsEmpty.type]("123456789")(fakeRequest.withSession(
              SessionKeys.returnData -> nineBoxModel,
              SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB
            )
            )

          lazy val result: Future[Result] = {
            setupVatSubscriptionService(vatSubscriptionResponse)
            TestConfirmSubmissionController.show("18AA")(requestWithSessionData)
          }

          "return 200" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            status(result) shouldBe Status.OK
          }

          "return HTML" in {
            contentType(result) shouldBe Some("text/html")
          }

          "the user name should not be displayed" in {
            Jsoup.parse(bodyOf(result)).select("#content > article > div > h2").text() shouldBe ""
          }
        }
      }

      "there is no session data" should {

        lazy val result: Future[Result] = {
          TestConfirmSubmissionController.show("18AA")(fakeRequest.withSession(
            SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB)
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

          "when the nrs feature switch is enabled and a submission to the back is successful" should {

            implicit lazy val nrsUser: User[AnyContentAsEmpty.type] = User[AnyContentAsEmpty.type](vrn)(fakeRequest)

            lazy val result: Future[Result] = {
              mockAppConfig.features.nrsSubmissionEnabled(true)
              TestConfirmSubmissionController.submit("18AA")(fakeRequest.withSession(
                "mtdNineBoxReturnData" -> nineBoxData,
                SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB
              ))
            }

            "return 303" in {
              mockAuthorise(mtdVatAuthorisedResponse)
              mockDateHasPassed(response = true)
              mockVatReturnSubmission(Future.successful(Right(SubmissionSuccessModel("12345"))))
              setupVatSubscriptionService(successCustomerInfoResponse)
              mockFullAuthResponse(agentFullInformationResponse)
              mockNrsSubmission(Future.successful(Right(SuccessModel("1234567890"))))
              mockExtractReceiptData(successReceiptDataResponse)
              setupAuditExtendedEvent
              setupAuditExtendedEvent
              setupAuditExtendedEvent

              status(result) shouldBe Status.SEE_OTHER
            }

            s"redirect to ${controllers.routes.ConfirmationController.show()}" in {
              redirectLocation(result) shouldBe Some(controllers.routes.ConfirmationController.show().url)
            }
          }

          "submission to backend is successful" should {

            lazy val result: Future[Result] = TestConfirmSubmissionController.submit("18AA")(fakeRequest.withSession(
              "mtdNineBoxReturnData" -> nineBoxData,
              SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB
            ))

            "return 303" in {
              mockAuthorise(mtdVatAuthorisedResponse)
              mockDateHasPassed(response = true)
              mockVatReturnSubmission(Future.successful(Right(SubmissionSuccessModel("12345"))))
              setupAuditExtendedEvent
              setupAuditExtendedEvent

              status(result) shouldBe Status.SEE_OTHER
            }

            s"redirect to ${controllers.routes.ConfirmationController.show()}" in {
              redirectLocation(result) shouldBe Some(controllers.routes.ConfirmationController.show().url)
            }
          }

          "submission to backend is unsuccessful" should {

            lazy val result: Future[Result] = TestConfirmSubmissionController.submit("18AA")(fakeRequest.withSession(
              "mtdNineBoxReturnData" -> nineBoxData,
              SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB
            ))

            "return 500" in {
              mockAuthorise(mtdVatAuthorisedResponse)
              mockDateHasPassed(response = true)
              mockVatReturnSubmission(Future.successful(Left(UnexpectedJsonFormat)))
              setupAuditExtendedEvent

              status(result) shouldBe Status.INTERNAL_SERVER_ERROR
            }

            "show the Submission error page" in {
              Jsoup.parse(bodyOf(result)).title() shouldBe SubmissionErrorMessages.heading
            }
          }
        }

        "obligation end date is in the future" should {

          lazy val result: Future[Result] = TestConfirmSubmissionController.submit("18AA")(fakeRequest.withSession(
            "mtdNineBoxReturnData" -> nineBoxData,
            SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB
          ))

          "return 400" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            mockDateHasPassed(response = false)
            status(result) shouldBe Status.BAD_REQUEST
          }

          "render generic Bad Request page" in {
            Jsoup.parse(bodyOf(result)).title() shouldBe "Bad request - 400"
          }
        }
      }

      "invalid session data exists" should {

        val nineBoxData = Json.obj("box10" -> "why").toString()
        lazy val result: Future[Result] = TestConfirmSubmissionController.submit("18AA")(fakeRequest.withSession(
          "mtdNineBoxReturnData" -> nineBoxData,
          SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB
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
          SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB
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

        setupVatSubscriptionService(successCustomerInfoResponse)
        mockFullAuthResponse(agentFullInformationResponse)
        mockNrsSubmission(Future.successful(Right(SuccessModel("1234567890"))))
        mockVatReturnSubmission(Future.successful(Right(SubmissionSuccessModel("12345"))))
        mockExtractReceiptData(successReceiptDataResponse)
        setupAuditExtendedEvent
        setupAuditExtendedEvent
        setupAuditExtendedEvent

        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to Confirmation page" in {
        redirectLocation(result) shouldBe Some(controllers.routes.ConfirmationController.show().url)
      }
    }

    "a Left is returned from buildIdentityData" should {

      lazy val result = TestConfirmSubmissionController.submitToNrs("18AA", submitVatReturnModel)

      "return an ISE" in {
        setupVatSubscriptionService(successCustomerInfoResponse)
        mockExtractReceiptData(successReceiptDataResponse)
        mockFullAuthResponse(Future.failed(BearerTokenExpired()))

        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }

      "render the submission error page" in {
        Jsoup.parse(bodyOf(result)).title shouldBe "Sorry, there is a problem with the service"
      }
    }

    "a Left is returned from extractReceiptData" should {

      lazy val result = TestConfirmSubmissionController.submitToNrs("18AA", submitVatReturnModel)

      "return an ISE" in {
        setupVatSubscriptionService(successCustomerInfoResponse)
        mockFullAuthResponse(agentFullInformationResponse)
        mockExtractReceiptData(Future.successful(Left(UnknownError)))

        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }

      "render the submission error page" in {
        Jsoup.parse(bodyOf(result)).title shouldBe "Sorry, there is a problem with the service"
      }

    }

    "a BAD_REQUEST is returned from Nrs Submission" should {

      lazy val result = TestConfirmSubmissionController.submitToNrs("18AA", submitVatReturnModel)

      "return an ISE" in {
        setupVatSubscriptionService(successCustomerInfoResponse)
        mockFullAuthResponse(agentFullInformationResponse)
        mockExtractReceiptData(successReceiptDataResponse)
        mockNrsSubmission(Future.successful(Left(BadRequestError("400", "error message"))))

        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }

      "render the submission error page" in {
        Jsoup.parse(bodyOf(result)).title shouldBe "Sorry, there is a problem with the service"
      }
    }
  }

  "ConfirmSubmissionController .buildIdentityData" when {

    "a successful call to the auth service is made" when {

      val expectedResponse = Right(IdentityDataTestData.correctModel)

      lazy val result = {
        mockFullAuthResponse(agentFullInformationResponse)
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
        status(result.left.get) shouldBe 500
      }

      "render the submission error page" in {
        Jsoup.parse(bodyOf(result.left.get)).title shouldBe "Sorry, there is a problem with the service"
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
}
