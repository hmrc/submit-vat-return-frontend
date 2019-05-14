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

import base.BaseSpec
import common.MandationStatuses.nonMTDfB
import connectors.httpParsers.ResponseHttpParsers.HttpGetResult
import mocks.{MockAuth, MockMandationPredicate}
import mocks.service.{MockVatObligationsService, MockVatSubscriptionService}
import models.errors.UnexpectedJsonFormat
import models.{CustomerDetails, MandationStatus, VatObligation, VatObligations}
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class SubmitFormControllerSpec extends BaseSpec with MockVatSubscriptionService with MockVatObligationsService with MockAuth with MockMandationPredicate {

  object TestSubmitFormController extends SubmitFormController(
    messagesApi,
    mockVatSubscriptionService,
    mockVatObligationsService,
    mockMandationStatusPredicate,
    errorHandler,
    mockAuthPredicate,
    mockAppConfig
  )

  "SubmitFormController .show" when {

    "user is authorised" when {

      "a successful response is received from the service" should {

        val customerInformation: CustomerDetails = CustomerDetails(
          Some("Test"), Some("User"), Some("ABC Solutions"), Some("ABCL"), hasFlatRateScheme = true
        )

        val obligations: VatObligations = VatObligations(Seq(
          VatObligation(
            LocalDate.parse("2019-01-12"),
            LocalDate.parse("2019-04-12"),
            LocalDate.parse("2019-05-12"),
            "18AA"
          )
        ))

        val vatSubscriptionResponse: Future[HttpGetResult[CustomerDetails]] = Future.successful(Right(customerInformation))
        val vatObligationsResponse: Future[HttpGetResult[VatObligations]] = Future.successful(Right(obligations))

        lazy val result = {
          setupMockMandationStatus(Right(MandationStatus(nonMTDfB)))
          TestSubmitFormController.show("18AA")(fakeRequest)
        }

        "return 200" in {
          mockAuthorise(mtdVatAuthorisedResponse)
          setupVatSubscriptionService(vatSubscriptionResponse)
          setupVatObligationsService(vatObligationsResponse)
          status(result) shouldBe Status.OK
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
        }
      }

      "an error response is returned from the service" should {

        "return an internal server status" in {

          val vatSubscriptionErrorResponse: Future[HttpGetResult[CustomerDetails]] = Future.successful(Left(UnexpectedJsonFormat))
          val vatObligationsErrorResponse: Future[HttpGetResult[VatObligations]] = Future.successful(Left(UnexpectedJsonFormat))

          mockAuthorise(mtdVatAuthorisedResponse)
          setupVatSubscriptionService(vatSubscriptionErrorResponse)
          setupVatObligationsService(vatObligationsErrorResponse)
          setupMockMandationStatus(Right(MandationStatus(nonMTDfB)))

          lazy val result = TestSubmitFormController.show("18AA")(fakeRequest)
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR

        }
      }
    }

    authControllerChecks(TestSubmitFormController.show("18AA"), fakeRequest)
  }

  "SubmitFormController .submit" should {
    "redirects to next page" when {
      "successful" in {
        mockAuthorise(mtdVatAuthorisedResponse)
        setupMockMandationStatus(Right(MandationStatus(nonMTDfB)))

        val request = FakeRequest().withFormUrlEncodedBody(
          "box1" -> "1000",
          "box2" -> "1000",
          "box3" -> "2000",
          "box4" -> "1000",
          "box5" -> "1000",
          "box6" -> "1000",
          "box7" -> "1000",
          "box8" -> "1000",
          "box9" -> "1000"
        )

        val result = TestSubmitFormController.submit(hasFlatRateScheme = false, "", "93DH", None)(request)
        status(result) shouldBe SEE_OTHER

        redirectLocation(result).get.contains(controllers.routes.ConfirmSubmissionController.show(frs = false, "", "", None, "93DH").url) shouldBe true
      }
    }
    "throws an internal server error" when {
      "an error occurs (unentered value in a box)" in {
        mockAuthorise(mtdVatAuthorisedResponse)
        setupMockMandationStatus(Right(MandationStatus(nonMTDfB)))

        val request = FakeRequest().withFormUrlEncodedBody(
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

        val obligationInput = Json.stringify(Json.toJson(VatObligations(Seq(VatObligation(
          LocalDate.now(),
          LocalDate.now(),
          LocalDate.now(),
          ""
        )))))

        val result = TestSubmitFormController.submit(hasFlatRateScheme = false, obligationInput, "", Some("Duanne"))(request)
        status(result) shouldBe OK
        contentAsString(result) should include("Enter a maximum of 13 decimal places for pounds.\nEnter a maximum of 2 decimal places for pence.\nYou can use a negative amount eg -13.2")
      }
      "an error occurs (incorrect format)" in {
        mockAuthorise(mtdVatAuthorisedResponse)
        setupMockMandationStatus(Right(MandationStatus(nonMTDfB)))

        val request = FakeRequest().withFormUrlEncodedBody(
          "box1" -> "1000",
          "box2" -> "1000",
          "box3" -> "2000",
          "box4" -> "1000",
          "box5" -> "3000",
          "box6" -> "1000",
          "box7" -> "12345.000",
          "box8" -> "12345.",
          "box9" -> "1234567890123456"
        )

        val obligationInput = Json.stringify(Json.toJson(VatObligations(Seq(VatObligation(
          LocalDate.now(),
          LocalDate.now(),
          LocalDate.now(),
          ""
        )))))

        val result = TestSubmitFormController.submit(hasFlatRateScheme = false, obligationInput, "", Some("Duanne"))(request)
        status(result) shouldBe OK
        contentAsString(result) should include("Enter a number in the format 0.00")
      }
      "an error occurs (negative number)" in {
        mockAuthorise(mtdVatAuthorisedResponse)
        setupMockMandationStatus(Right(MandationStatus(nonMTDfB)))

        val request = FakeRequest().withFormUrlEncodedBody(
          "box1" -> "1000",
          "box2" -> "1000",
          "box3" -> "2000",
          "box4" -> "1000",
          "box5" -> "-3000",
          "box6" -> "1000",
          "box7" -> "1000",
          "box8" -> "1000",
          "box9" -> "1000"
        )

        val obligationInput = Json.stringify(Json.toJson(VatObligations(Seq(VatObligation(
          LocalDate.now(),
          LocalDate.now(),
          LocalDate.now(),
          ""
        )))))

        val result = TestSubmitFormController.submit(hasFlatRateScheme = false, obligationInput, "", Some("Duanne"))(request)
        status(result) shouldBe OK
        contentAsString(result) should include("Enter a maximum of 13 decimal places for pounds.\nEnter a maximum of 2 decimal places for pence.\nDo not use a negative amount eg -13.2")
      }
      "an error occurs (incorrect box additions)" when {
        mockAuthorise(mtdVatAuthorisedResponse)
        setupMockMandationStatus(Right(MandationStatus(nonMTDfB)))

        val request = FakeRequest().withFormUrlEncodedBody(
          "box1" -> "1000",
          "box2" -> "1000",
          "box3" -> "12000",
          "box4" -> "1000",
          "box5" -> "121000",
          "box6" -> "1000",
          "box7" -> "1000",
          "box8" -> "1000",
          "box9" -> "1000"
        )

        val obligationInput = Json.stringify(Json.toJson(VatObligations(Seq(VatObligation(
          LocalDate.now(),
          LocalDate.now(),
          LocalDate.now(),
          ""
        )))))
        val result = TestSubmitFormController.submit(hasFlatRateScheme = false, obligationInput, "", Some("Duanne"))(request)

        "status is OK" in {
          status(result) shouldBe OK
        }
        "contains box 3 error" in {
          contentAsString(result) should include("Add the number from box 1 to the number from box 2 and write it here")
        }
        "contains box 5 error" in {
          contentAsString(result) should include("Subtract the number in box 4 away from the number in box 3 and write it here")
        }
      }
    }
  }
}
