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

import assets.CustomerDetailsTestAssets._
import assets.NrsTestData.IdentityDataTestData
import assets.messages.SubmissionErrorMessages
import audit.mocks.MockAuditingService
import base.BaseSpec
import common.{MandationStatuses, SessionKeys}
import connectors.httpParsers.ResponseHttpParsers.HttpGetResult
import mocks.service.{MockDateService, MockVatReturnsService, MockVatSubscriptionService}
import mocks.{MockAuth, MockMandationPredicate}
import models.auth.User
import models.errors.UnexpectedJsonFormat
import models.vatReturnSubmission.SubmissionSuccessModel
import models.{CustomerDetails, SubmitVatReturnModel}
import org.joda.time.DateTime
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.auth.core.{AffinityGroup, BearerTokenExpired}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class ConfirmSubmissionControllerSpec extends BaseSpec
  with MockAuth
  with MockMandationPredicate
  with MockVatSubscriptionService
  with MockVatReturnsService
  with MockDateService
  with MockAuditingService {

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
    mockEnrolmentsAuthService
  )

  mockAppConfig.features.nrsSubmissionEnabled(false)

  "ConfirmSubmissionController .show" when {

    "user is authorised" when {

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
          LocalDate.now(),
          LocalDate.now(),
          LocalDate.now()
        )
      ))

      "there is session data" when {

        "a successful response is returned from the vat subscription service" should {

          val vatSubscriptionResponse: Future[HttpGetResult[CustomerDetails]] = Future.successful(Right(customerDetailsWithFRS))

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

          "submission to backend is successful" should {

            lazy val result: Future[Result] = TestConfirmSubmissionController.submit("18AA")(fakeRequest.withSession(
              "mtdNineBoxReturnData" -> nineBoxData,
              SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB
            ))

            "return 303" in {
              mockAuthorise(mtdVatAuthorisedResponse)
              mockDateHasPassed(response = true)
              mockVatReturnsService(Future.successful(Right(SubmissionSuccessModel("12345"))))
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
              mockVatReturnsService(Future.successful(Left(UnexpectedJsonFormat)))
              setupAuditExtendedEvent

              status(result) shouldBe Status.INTERNAL_SERVER_ERROR
            }

            "show the Submission error page" in {
              Jsoup.parse(bodyOf(result)).title() shouldBe SubmissionErrorMessages.title
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

  "ConfirmSubmissionController .buildIdentityData" when {

    def mockAuthResponse[A](authResponse: Future[A]): Unit = {
      (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
        .expects(*, *, *, *)
        .returns(authResponse)
    }

    "a successful call to the auth service is made" when {

      "all of the information is returned from the auth service" should {

        val authResponse =
          new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(
            Some(AffinityGroup.Agent),
            IdentityDataTestData.correctModel.internalId),
            IdentityDataTestData.correctModel.externalId),
            IdentityDataTestData.correctModel.agentCode),
            Some(IdentityDataTestData.correctModel.credentials)),
            IdentityDataTestData.correctModel.confidenceLevel),
            IdentityDataTestData.correctModel.nino),
            IdentityDataTestData.correctModel.saUtr),
            Some(IdentityDataTestData.correctModel.name)),
            IdentityDataTestData.correctModel.dateOfBirth),
            IdentityDataTestData.correctModel.email),
            IdentityDataTestData.correctModel.agentInformation),
            IdentityDataTestData.correctModel.groupIdentifier),
            IdentityDataTestData.correctModel.credentialRole),
            IdentityDataTestData.correctModel.mdtpInformation),
            Some(IdentityDataTestData.correctModel.itmpName)),
            IdentityDataTestData.correctModel.itmpDateOfBirth),
            Some(IdentityDataTestData.correctModel.itmpAddress)),
            IdentityDataTestData.correctModel.credentialStrength),
            LoginTimes(new DateTime("2016-11-27T09:00:00.000Z"), Some(new DateTime("2016-11-01T12:00:00.000Z")))
          )

        val expectedResponse = Right(IdentityDataTestData.correctModel)

        lazy val result = {
          mockAuthResponse(authResponse)
          TestConfirmSubmissionController.buildIdentityData()(user)
        }

        "return a full IdentityData model" in {
          await(result) shouldBe expectedResponse
        }
      }

      "none of the mandatory fields are returned from the auth service" should {

        val authResponse =
          new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(
            Some(AffinityGroup.Agent),
            IdentityDataTestData.correctModel.internalId),
            IdentityDataTestData.correctModel.externalId),
            IdentityDataTestData.correctModel.agentCode),
            None),
            IdentityDataTestData.correctModel.confidenceLevel),
            IdentityDataTestData.correctModel.nino),
            IdentityDataTestData.correctModel.saUtr),
            None),
            IdentityDataTestData.correctModel.dateOfBirth),
            IdentityDataTestData.correctModel.email),
            IdentityDataTestData.correctModel.agentInformation),
            IdentityDataTestData.correctModel.groupIdentifier),
            IdentityDataTestData.correctModel.credentialRole),
            IdentityDataTestData.correctModel.mdtpInformation),
            Some(IdentityDataTestData.correctModel.itmpName)),
            IdentityDataTestData.correctModel.itmpDateOfBirth),
            Some(IdentityDataTestData.correctModel.itmpAddress)),
            IdentityDataTestData.correctModel.credentialStrength),
            LoginTimes(new DateTime("2016-11-27T09:00:00.000Z"), Some(new DateTime("2016-11-01T12:00:00.000Z")))
          )

        lazy val result = {
          mockAuthResponse(authResponse)
          TestConfirmSubmissionController.buildIdentityData()(user)
        }

        "return an internal server error" in {
          status(result.left.get) shouldBe 500
        }
      }
    }

    "an exception is returned from the auth service" should {

      lazy val result = {
        mockAuthResponse(Future.failed(BearerTokenExpired()))
        TestConfirmSubmissionController.buildIdentityData()(user)
      }

      "return an internal server error" in {
        status(result.left.get) shouldBe 500
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
