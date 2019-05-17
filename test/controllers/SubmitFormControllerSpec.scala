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
import common.{MandationStatuses, SessionKeys}
import connectors.httpParsers.ResponseHttpParsers.HttpGetResult
import mocks.{MockAuth, MockMandationPredicate}
import mocks.service.{MockVatObligationsService, MockVatSubscriptionService}
import models.errors.UnexpectedJsonFormat
import models.{CustomerDetails, MandationStatus, VatObligation, VatObligations}
import play.api.http.Status
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class SubmitFormControllerSpec extends BaseSpec with MockVatSubscriptionService with MockVatObligationsService with MockAuth with MockMandationPredicate {

  lazy val nonMTDfB = "Non MTDfB"

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

        lazy val customerInformation: CustomerDetails = CustomerDetails(
          Some("Test"), Some("User"), Some("ABC Solutions"), Some("ABCL"), hasFlatRateScheme = true
        )

        lazy val obligations: VatObligations = VatObligations(Seq(
          VatObligation(
            LocalDate.parse("2019-01-12"),
            LocalDate.parse("2019-04-12"),
            LocalDate.parse("2019-05-12"),
            "18AA"
          )
        ))

        lazy val vatSubscriptionResponse: Future[HttpGetResult[CustomerDetails]] = Future.successful(Right(customerInformation))
        lazy val vatObligationsResponse: Future[HttpGetResult[VatObligations]] = Future.successful(Right(obligations))

        lazy val result = {
          TestSubmitFormController.show("18AA")(fakeRequest.withSession(SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB))
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

          lazy val result = TestSubmitFormController.show("18AA")(fakeRequest.withSession(SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB))
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR

        }
      }
    }

    authControllerChecks(TestSubmitFormController.show("18AA"), fakeRequest)
  }


  "SubmitFormController .submit" should {

    "redirect to next page" when {

      "successful" should {

        lazy val request = FakeRequest().withFormUrlEncodedBody(
          "box1" -> "1000.11",
          "box2" -> "1000",
          "box3" -> "2000.11",
          "box4" -> "1000",
          "box5" -> "1000.11",
          "box6" -> "1000",
          "box7" -> "1000.3",
          "box8" -> "1234567890123",
          "box9" -> "1234567890123.45",
          "flatRateScheme" -> "true",
          "start" -> "2019-01-01",
          "end" -> "2019-01-04",
          "due" -> "2019-01-05"
        ).withSession(
          SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB
        )


        lazy val result = {
          TestSubmitFormController.submit(
            periodKey = "93DH",
            clientName = Some("Test user"),
            hasFlatRateScheme = true,
            start = "2019-01-01",
            end = "2019-01-01",
            due = "2019-01-01")(request)
        }
        "status is SEE_OTHER" in {
          mockAuthorise(mtdVatAuthorisedResponse)
          status(result) shouldBe SEE_OTHER
        }

        s"redirect to ${controllers.routes.ConfirmationController.show()}" in {
          redirectLocation(result).get.contains(controllers.routes.ConfirmSubmissionController.show("93DH").url) shouldBe true
        }
      }
    }

    "display a validation error" when {

      "an error occurs (unentered value in a box)" should {

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
          "start" -> "2019-01-01",
          "end" -> "2019-01-04",
          "due" -> "2019-01-05"
        ).withSession(
          SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB
        )

        lazy val result = {
          TestSubmitFormController.submit(
            periodKey = "93DH",
            clientName = Some("Test user"),
            hasFlatRateScheme = true,
            start = "2019-01-01",
            end = "2019-01-01",
            due = "2019-01-01")(request)
        }

        "status is OK" in {
          mockAuthorise(mtdVatAuthorisedResponse)
          status(result) shouldBe OK
        }

        "contains common header" in {
          contentAsString(result) should include("You have one or more errors")
        }

        "contains missing number error" in {
          contentAsString(result) should include("Enter a number")
        }
      }

      "an error occurs (too many numbers)" should {

        lazy val request = FakeRequest().withFormUrlEncodedBody(
          "box1" -> "1000",
          "box2" -> "1000",
          "box3" -> "2000",
          "box4" -> "1000",
          "box5" -> "3000",
          "box6" -> "1000",
          "box7" -> "12345.000",
          "box8" -> "12345.",
          "box9" -> "1234567890123456",
          "flatRateScheme" -> "true",
          "start" -> "2019-01-01",
          "end" -> "2019-01-04",
          "due" -> "2019-01-05"
        ).withSession(
          SessionKeys.mandationStatus ->  MandationStatuses.nonMTDfB
        )

        lazy val result = {
          TestSubmitFormController.submit(
            periodKey = "93DH",
            clientName = Some("Test user"),
            hasFlatRateScheme = true,
            start = "2019-01-01",
            end = "2019-01-01",
            due = "2019-01-01")(request)
        }

        "status is OK" in {
          mockAuthorise(mtdVatAuthorisedResponse)
          status(result) shouldBe OK
        }

        "contains common header" in {
          contentAsString(result) should include("You have one or more errors")
        }

        "contains the too many numbers error" in {
          contentAsString(result) should include("Enter a maximum of 13 digits for pounds." +
            "\nEnter a maximum of 2 decimal places for pence.\nYou can use a negative amount eg -13.2")
        }
      }

      "an error occurs (incorrect format)" when {

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
          "start" -> "2019-01-01",
          "end" -> "2019-01-04",
          "due" -> "2019-01-05"
        ).withSession(
          SessionKeys.mandationStatus ->  MandationStatuses.nonMTDfB
        )

        lazy val result = {
          TestSubmitFormController.submit(
            periodKey = "93DH",
            clientName = Some("Test user"),
            hasFlatRateScheme = true,
            start = "2019-01-01",
            end = "2019-01-01",
            due = "2019-01-01")(request)
        }

        "status is OK" in {
          mockAuthorise(mtdVatAuthorisedResponse)
          status(result) shouldBe OK
        }

        "contains common header" in {
          contentAsString(result) should include("You have one or more errors")
        }

        "has the correct form error shown" in {
          contentAsString(result) should include("Enter a number in the correct format")
        }
      }

      "an error occurs (negative number)" when {
        lazy val request = FakeRequest().withFormUrlEncodedBody(
          "box1" -> "1000",
          "box2" -> "1000",
          "box3" -> "2000",
          "box4" -> "1000",
          "box5" -> "-3000",
          "box6" -> "1000",
          "box7" -> "1000",
          "box8" -> "1000",
          "box9" -> "1000",
          "flatRateScheme" -> "true",
          "start" -> "2019-01-01",
          "end" -> "2019-01-04",
          "due" -> "2019-01-05"
        ).withSession(
          SessionKeys.mandationStatus ->  MandationStatuses.nonMTDfB
        )

        lazy val result = {
          TestSubmitFormController.submit(
            periodKey = "93DH",
            clientName = Some("Test user"),
            hasFlatRateScheme = true,
            start = "2019-01-01",
            end = "2019-01-01",
            due = "2019-01-01")(request)
        }

        "status is OK" in {
          mockAuthorise(mtdVatAuthorisedResponse)
          status(result) shouldBe OK
        }

        "contains common header" in {
          contentAsString(result) should include("You have one or more errors")
        }

        "contains negative number error" in {
          contentAsString(result) should include("Enter a maximum of 13 digits for pounds." +
            "\nEnter a maximum of 2 decimal places for pence.\nDo not use a negative amount eg -13.2")
        }
      }

      "an error occurs (incorrect box additions)" when {

        lazy val request = FakeRequest().withFormUrlEncodedBody(
          "box1" -> "1000",
          "box2" -> "1000",
          "box3" -> "12000",
          "box4" -> "1000",
          "box5" -> "121000",
          "box6" -> "1000",
          "box7" -> "1000",
          "box8" -> "1000",
          "box9" -> "1000",
          "flatRateScheme" -> "true",
          "start" -> "2019-01-01",
          "end" -> "2019-01-04",
          "due" -> "2019-01-05"
        ).withSession(
          SessionKeys.mandationStatus ->  MandationStatuses.nonMTDfB
        )

        lazy val result = {
          TestSubmitFormController.submit(
            periodKey = "93DH",
            clientName = Some("Test user"),
            hasFlatRateScheme = true,
            start = "2019-01-01",
            end = "2019-01-01",
            due = "2019-01-01")(request)
        }

        "status is OK" in {
          mockAuthorise(mtdVatAuthorisedResponse)
          status(result) shouldBe OK
        }

        "contains common header" in {
          contentAsString(result) should include("You have one or more errors")
        }

        "contains box 3 error" in {
          contentAsString(result) should include("Add the number from box 1 to the number from box 2 and write it here")
        }

        "contains box 5 error" in {
          contentAsString(result) should include("Subtract the number in box 4 away from the number in box 3 and write it here")
        }
      }

      "display box 3 error if box 1 or box 2 are not numbers" when {

        def request(input: Boolean): FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest().withFormUrlEncodedBody(
          "box1" -> (if (input) "e" else "1000"),
          "box2" -> (if (!input) "e" else "1000"),
          "box3" -> "1000",
          "box4" -> "1000",
          "box5" -> "1000",
          "box6" -> "1000",
          "box7" -> "1000",
          "box8" -> "1000",
          "box9" -> "1000",
          "flatRateScheme" -> "true",
          "start" -> "2019-01-01",
          "end" -> "2019-01-04",
          "due" -> "2019-01-05"
        ).withSession(
          SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB
        )

        lazy val resultBox1Error = {
          TestSubmitFormController.submit(
            periodKey = "93DH",
            clientName = Some("Test user"),
            hasFlatRateScheme = true,
            start = "2019-01-01",
            end = "2019-01-01",
            due = "2019-01-01")(request(true))
        }

        lazy val resultBox2Error = {
          TestSubmitFormController.submit(
            periodKey = "93DH",
            clientName = Some("Test user"),
            hasFlatRateScheme = true,
            start = "2019-01-01",
            end = "2019-01-01",
            due = "2019-01-01")(request(false))
        }

        "status is OK for box1Error" in {
          mockAuthorise(mtdVatAuthorisedResponse)
          status(resultBox1Error) shouldBe OK
        }

        "status is OK for box2Error" in {
          mockAuthorise(mtdVatAuthorisedResponse)
          status(resultBox2Error) shouldBe OK
        }

        "error is displayed for box1Error" in {
          contentAsString(resultBox1Error) should include("Add the number from box 1 to the number from box 2 and write it here")
        }

        "error is displayed for box2Error" in {
          contentAsString(resultBox1Error) should include("Add the number from box 1 to the number from box 2 and write it here")
        }

        "display box 5 error if box 3 or box 4 are not numbers" when {

          def request(input: Boolean): FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest().withFormUrlEncodedBody(
            "box1" -> "1000",
            "box2" -> "1000",
            "box3" -> (if (input) "e" else "1000"),
            "box4" -> (if (!input) "e" else "1000"),
            "box5" -> "1000",
            "box6" -> "1000",
            "box7" -> "1000",
            "box8" -> "1000",
            "box9" -> "1000",
            "flatRateScheme" -> "true",
            "start" -> "2019-01-01",
            "end" -> "2019-01-04",
            "due" -> "2019-01-05"
          ).withSession(
            SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB
          )

          lazy val resultBox3Error = {
            TestSubmitFormController.submit(
              periodKey = "93DH",
              clientName = Some("Test user"),
              hasFlatRateScheme = true,
              start = "2019-01-01",
              end = "2019-01-01",
              due = "2019-01-01")(request(true))
          }
          lazy val resultBox4Error = {
            TestSubmitFormController.submit(
              periodKey = "93DH",
              clientName = Some("Test user"),
              hasFlatRateScheme = true,
              start = "2019-01-01",
              end = "2019-01-01",
              due = "2019-01-01")(request(false))
          }

          "status is OK for box3Error" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            status(resultBox3Error) shouldBe OK
          }

          "status is OK for box4Error" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            status(resultBox4Error) shouldBe OK
          }

          "error is displayed for box3Error" in {
            contentAsString(resultBox3Error) should include("Subtract the number in box 4 away from the number in box 3 and write it here")
          }

          "error is displayed for box4Error" in {
            contentAsString(resultBox4Error) should include("Subtract the number in box 4 away from the number in box 3 and write it here")
          }
        }

        "an error occurs (box3 empty)" when {
          lazy val request = FakeRequest().withFormUrlEncodedBody(
            "box1" -> "1000",
            "box2" -> "1000",
            "box3" -> "",
            "box4" -> "1000",
            "box5" -> "1000",
            "box6" -> "1000",
            "box7" -> "1000",
            "box8" -> "1000",
            "box9" -> "1000",
            "flatRateScheme" -> "true",
            "start" -> "2019-01-01",
            "end" -> "2019-01-04",
            "due" -> "2019-01-05").withSession(
            SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB
          )

          lazy val result = {
            TestSubmitFormController.submit(
              periodKey = "93DH",
              clientName = Some("Test user"),
              hasFlatRateScheme = true,
              start = "2019-01-01",
              end = "2019-01-01",
              due = "2019-01-01")(request)
          }

          "status is OK" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            status(result) shouldBe OK
          }

          "error is displayed" in {
            contentAsString(result) should include("Enter a number")
          }
        }

        "an error occurs (box3 invalid format)" when {
          lazy val request = FakeRequest().withFormUrlEncodedBody(
            "box1" -> "1000",
            "box2" -> "1000",
            "box3" -> "e",
            "box4" -> "1000",
            "box5" -> "1000",
            "box6" -> "1000",
            "box7" -> "1000",
            "box8" -> "1000",
            "box9" -> "1000",
            "flatRateScheme" -> "true",
            "start" -> "2019-01-01",
            "end" -> "2019-01-04",
            "due" -> "2019-01-05"
          ).withSession(
            SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB
          )

          lazy val result = {
            TestSubmitFormController.submit(
              periodKey = "93DH",
              clientName = Some("Test user"),
              hasFlatRateScheme = true,
              start = "2019-01-01",
              end = "2019-01-01",
              due = "2019-01-01")(request)
          }

          "status is OK" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            status(result) shouldBe OK
          }

          "error is displayed" in {
            contentAsString(result) should include("Enter a number in the correct format")
          }
        }

        "an error occurs (box3 incorrect amount of numbers)" when {

          lazy val request1 = FakeRequest().withFormUrlEncodedBody(
            "box1" -> "1000",
            "box2" -> "1000",
            "box3" -> "1234567890098765",
            "box4" -> "1000",
            "box5" -> "1000",
            "box6" -> "1000",
            "box7" -> "1000",
            "box8" -> "1000",
            "box9" -> "1000",
            "flatRateScheme" -> "true",
            "start" -> "2019-01-01",
            "end" -> "2019-01-04",
            "due" -> "2019-01-05"
          ).withSession(
            SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB
          )

          lazy val request2 = FakeRequest().withFormUrlEncodedBody(
            "box1" -> "1000",
            "box2" -> "1000",
            "box3" -> "1234.3456789",
            "box4" -> "1000",
            "box5" -> "1000",
            "box6" -> "1000",
            "box7" -> "1000",
            "box8" -> "1000",
            "box9" -> "1000",
            "flatRateScheme" -> "true",
            "start" -> "2019-01-01",
            "end" -> "2019-01-04",
            "due" -> "2019-01-05"
          ).withSession(
            SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB
          )

          lazy val result1 = {
            TestSubmitFormController.submit(
              periodKey = "93DH",
              clientName = Some("Test user"),
              hasFlatRateScheme = true,
              start = "2019-01-01",
              end = "2019-01-01",
              due = "2019-01-01")(request1)
          }

          lazy val result2 = {
            TestSubmitFormController.submit(
              periodKey = "93DH",
              clientName = Some("Test user"),
              hasFlatRateScheme = true,
              start = "2019-01-01",
              end = "2019-01-01",
              due = "2019-01-01")(request2)
          }

          "status is OK for too many non decimal digits" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            status(result1) shouldBe OK
          }

          "status is OK for too many decimal digits" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            status(result2) shouldBe OK
          }

          "error is displayed for too many non decimal digits" in {
            contentAsString(result1) should include("Enter a maximum of 13 digits for pounds." +
              "\nEnter a maximum of 2 decimal places for pence.\nYou can use a negative amount eg -13.2")
          }

          "error is displayed for too many decimal digits" in {
            contentAsString(result2) should include("Enter a maximum of 13 digits for pounds." +
              "\nEnter a maximum of 2 decimal places for pence.\nYou can use a negative amount eg -13.2")
          }
        }

        "an error occurs (box5 empty)" when {
          lazy val request = FakeRequest().withFormUrlEncodedBody(
            "box1" -> "1000",
            "box2" -> "1000",
            "box3" -> "12000",
            "box4" -> "1000",
            "box5" -> "",
            "box6" -> "1000",
            "box7" -> "1000",
            "box8" -> "1000",
            "box9" -> "1000",
            "flatRateScheme" -> "true",
            "start" -> "2019-01-01",
            "end" -> "2019-01-04",
            "due" -> "2019-01-05"
          ).withSession(
            SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB
          )

          lazy val result = {
            TestSubmitFormController.submit(
              periodKey = "93DH",
              clientName = Some("Test user"),
              hasFlatRateScheme = true,
              start = "2019-01-01",
              end = "2019-01-01",
              due = "2019-01-01")(request)
          }

          "status is OK" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            status(result) shouldBe OK
          }

          "error is displayed" in {
            contentAsString(result) should include("Enter a number")
          }
        }

        "an error occurs (box5 invalid format)" when {

          lazy val request = FakeRequest().withFormUrlEncodedBody(
            "box1" -> "1000",
            "box2" -> "1000",
            "box3" -> "12000",
            "box4" -> "1000",
            "box5" -> "e",
            "box6" -> "1000",
            "box7" -> "1000",
            "box8" -> "1000",
            "box9" -> "1000",
            "flatRateScheme" -> "true",
            "start" -> "2019-01-01",
            "end" -> "2019-01-04",
            "due" -> "2019-01-05").withSession(
            SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB
          )

          lazy val result = {
            TestSubmitFormController.submit(
              periodKey = "93DH",
              clientName = Some("Test user"),
              hasFlatRateScheme = true,
              start = "2019-01-01",
              end = "2019-01-01",
              due = "2019-01-01")(request)
          }

          "status is OK" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            status(result) shouldBe OK
          }

          "error is displayed" in {
            contentAsString(result) should include("Enter a number in the correct format")
          }
        }

        "an error occurs (box5 incorrect amount of numbers)" when {

          lazy val request1 = FakeRequest().withFormUrlEncodedBody(
            "box1" -> "1000",
            "box2" -> "1000",
            "box3" -> "2000",
            "box4" -> "1000",
            "box5" -> "1000123456789012345",
            "box6" -> "1000",
            "box7" -> "1000",
            "box8" -> "1000",
            "box9" -> "1000",
            "flatRateScheme" -> "true",
            "start" -> "2019-01-01",
            "end" -> "2019-01-04",
            "due" -> "2019-01-05"
          ).withSession(
            SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB
          )

          lazy val request2 = FakeRequest().withFormUrlEncodedBody(
            "box1" -> "1000",
            "box2" -> "1000",
            "box3" -> "2000",
            "box4" -> "1000",
            "box5" -> "1000.1234567",
            "box6" -> "1000",
            "box7" -> "1000",
            "box8" -> "1000",
            "box9" -> "1000",
            "flatRateScheme" -> "true",
            "start" -> "2019-01-01",
            "end" -> "2019-01-04",
            "due" -> "2019-01-05"
          ).withSession(
            SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB
          )

          lazy val result1 = {
            TestSubmitFormController.submit(
              periodKey = "93DH",
              clientName = Some("Test user"),
              hasFlatRateScheme = true,
              start = "2019-01-01",
              end = "2019-01-01",
              due = "2019-01-01")(request1)
          }

          lazy val result2 = {
            TestSubmitFormController.submit(
              periodKey = "93DH",
              clientName = Some("Test user"),
              hasFlatRateScheme = true,
              start = "2019-01-01",
              end = "2019-01-01",
              due = "2019-01-01")(request2)
          }

          "status is OK for too many non decimal digits" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            status(result1) shouldBe OK
          }

          "status is OK for too many decimal digits" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            status(result2) shouldBe OK
          }

          "error is displayed for too many non decimal digits" in {
            contentAsString(result1) should include("Enter a maximum of 13 digits for pounds." +
              "\nEnter a maximum of 2 decimal places for pence.\nDo not use a negative amount eg -13.2")
          }

          "error is displayed for too many decimal digits" in {
            contentAsString(result2) should include("Enter a maximum of 13 digits for pounds." +
              "\nEnter a maximum of 2 decimal places for pence.\nDo not use a negative amount eg -13.2")
          }
        }
      }
    }
  }
}
