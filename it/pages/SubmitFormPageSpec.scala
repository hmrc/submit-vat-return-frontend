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

package pages

import java.time.LocalDate

import base.BaseISpec
import common.MandationStatuses
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import stubs.AuthStub._
import stubs.VatObligationsStub._
import stubs.VatSubscriptionStub._
import stubs.{AuthStub, VatObligationsStub, VatSubscriptionStub}
import forms.SubmitVatReturnForm
import models.{SubmitFormViewModel, SubmitVatReturnModel}

class SubmitFormPageSpec extends BaseISpec {

  "Calling /:periodKey/submit-form" when {

    "there is a GET request" when {

      def request: WSResponse = get("/18AA/submit-form", formatSessionMandationStatus(Some(MandationStatuses.nonMTDfB)))

      "user is authorised" when {

        "retrieval of customer details and obligation data is successful" should {

          "return 200" in {

            AuthStub.stubResponse(OK, mtdVatAuthResponse)
            VatSubscriptionStub.stubResponse("customer-details", OK, customerInformationSuccessJson)
            VatSubscriptionStub.stubResponse("mandation-status", OK, mandationStatusSuccessJson)
            VatObligationsStub.stubResponse(OK, vatObligationsSuccessJson)

            val response: WSResponse = request

            response.status shouldBe OK
          }
        }

        "retrieval of customer details fails" should {

          "return 500" in {

            AuthStub.stubResponse(OK, mtdVatAuthResponse)
            VatSubscriptionStub.stubResponse("mandation-status", OK, mandationStatusSuccessJson)
            VatObligationsStub.stubResponse(OK, vatObligationsSuccessJson)
            VatSubscriptionStub.stubResponse("customer-details", SERVICE_UNAVAILABLE, Json.obj())

            val response: WSResponse = request

            response.status shouldBe INTERNAL_SERVER_ERROR
          }
        }

        "retrieval of obligation data fails" should {

          "return 500" in {

            AuthStub.stubResponse(OK, mtdVatAuthResponse)
            VatSubscriptionStub.stubResponse("mandation-status", OK, mandationStatusSuccessJson)
            VatSubscriptionStub.stubResponse("customer-details", OK, customerInformationSuccessJson)
            VatObligationsStub.stubResponse(SERVICE_UNAVAILABLE, Json.obj())

            val response: WSResponse = request

            response.status shouldBe INTERNAL_SERVER_ERROR
          }
        }
      }

      "user is unauthorised" should {

        "return 403" in {

          AuthStub.stubResponse(OK, otherEnrolmentAuthResponse)

          val response: WSResponse = request

          response.status shouldBe FORBIDDEN
        }
      }

      "user is not mandated" should {

        def requestNotMandation: WSResponse = get("/18AA/submit-form", formatSessionMandationStatus(Some("unsupportedMandationStatus")))

        "return 403" in {

          AuthStub.stubResponse(OK, mtdVatAuthResponse)
          VatSubscriptionStub.stubResponse("mandation-status", OK, unsupportedMandationStatusJson)

          val response: WSResponse = requestNotMandation

          response.status shouldBe FORBIDDEN

        }
      }
    }

    "there is a POST request" when {

      val validSubmissionModel = SubmitVatReturnModel(
        box1 = 1000.00,
        box2 = 1000.00,
        box3 = 2000.00,
        box4 = 1000.00,
        box5 = 1000.00,
        box6 = 1000.00,
        box7 = 1000.00,
        box8 = 1000.00,
        box9 = 1000.00,
        flatRateScheme = true,
        start = LocalDate.parse("2019-01-01"),
        end = LocalDate.parse("2019-01-04"),
        due = LocalDate.parse("2019-01-05")
      )

      def postRequest(data: SubmitVatReturnModel): WSResponse =
        post("/18AA/submit-form", formatSessionMandationStatus(Some(MandationStatuses.nonMTDfB)))(toFormData(SubmitVatReturnForm.submitVatReturnForm, data))

      "user is authorised" when {

        "the data posted is valid" should {

          "return 303" in {

            AuthStub.stubResponse(OK, mtdVatAuthResponse)

            val response: WSResponse = postRequest(validSubmissionModel)

            response.status shouldBe SEE_OTHER
          }
        }

        "the data posted is invalid" when {

          val invalidSubmissionModel = SubmitVatReturnModel(
            box1 = 1000.00,
            box2 = 1000.00,
            box3 = 1000.00,
            box4 = 1000.00,
            box5 = 1000.00,
            box6 = 1000.00,
            box7 = 1000.00,
            box8 = 1000.00,
            box9 = 1000.00,
            flatRateScheme = true,
            start = LocalDate.parse("2019-01-01"),
            end = LocalDate.parse("2019-01-04"),
            due = LocalDate.parse("2019-01-05")
          )

          "there is a view model in session" should {

            val viewModel: String = Json.toJson(SubmitFormViewModel(
              hasFlatRateScheme = true,
              start = LocalDate.parse("2019-01-01"),
              end = LocalDate.parse("2019-01-04"),
              due = LocalDate.parse("2019-01-05")
            )).toString

            def postRequest(data: SubmitVatReturnModel): WSResponse =
              post("/18AA/submit-form",
                formatSessionMandationStatus(Some(MandationStatuses.nonMTDfB))
                  ++ formatViewModel(Some(viewModel)))(toFormData(SubmitVatReturnForm.submitVatReturnForm, data))

            "return 200" in {

              AuthStub.stubResponse(OK, mtdVatAuthResponse)

              VatSubscriptionStub.stubResponse("customer-details", OK, customerInformationSuccessJson)

              val response: WSResponse = postRequest(invalidSubmissionModel)

              response.status shouldBe BAD_REQUEST
            }
          }

          "there is not a view model in session" should {

            def postRequest(data: SubmitVatReturnModel): WSResponse =
              post("/18AA/submit-form",
                formatSessionMandationStatus(Some(MandationStatuses.nonMTDfB)))(toFormData(SubmitVatReturnForm.submitVatReturnForm, data))

            "return 200" in {

              AuthStub.stubResponse(OK, mtdVatAuthResponse)
              VatSubscriptionStub.stubResponse("customer-details", OK, customerInformationSuccessJson)
              VatObligationsStub.stubResponse(OK, vatObligationsSuccessJson)

              val response: WSResponse = postRequest(invalidSubmissionModel)

              response.status shouldBe OK
            }
          }
        }
      }

      "user is unauthorised" should {

        "return 403" in {

          AuthStub.stubResponse(OK, otherEnrolmentAuthResponse)

          val response: WSResponse = postRequest(validSubmissionModel)

          response.status shouldBe FORBIDDEN
        }
      }

      "user is not mandated" should {

        def postRequest(data: SubmitVatReturnModel): WSResponse =
          post("/18AA/submit-form", formatSessionMandationStatus(Some("unsupportedMandationStatus")))(toFormData(SubmitVatReturnForm.submitVatReturnForm, data))

        "return 403" in {

          AuthStub.stubResponse(OK, mtdVatAuthResponse)
          VatSubscriptionStub.stubResponse("mandation-status", OK, unsupportedMandationStatusJson)

          val response: WSResponse = postRequest(validSubmissionModel)

          response.status shouldBe FORBIDDEN

        }
      }

    }
  }
}
