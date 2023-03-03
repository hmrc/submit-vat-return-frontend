/*
 * Copyright 2023 HM Revenue & Customs
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
import common.{MandationStatuses, SessionKeys}
import forms.SubmitVatReturnForm
import models.NineBoxModel
import org.jsoup.Jsoup
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import stubs.AuthStub._
import stubs.VatObligationsStub._
import stubs.VatSubscriptionStub._
import stubs.{AuthStub, VatObligationsStub, VatSubscriptionStub}

class SubmitFormPageSpec extends BaseISpec {

  "Calling /:periodKey/submit-form" when {

    "there is a GET request" when {

      val sessionValues =
        formatSessionMandationStatus(Some(MandationStatuses.nonMTDfB)) ++ Map(SessionKeys.HonestyDeclaration.key -> s"$vrn-18AA")

      def request: WSResponse = get("/18AA/submit-form", sessionValues)

      "user is authorised" when {

        "retrieval of customer details and obligation data is successful" when {

          "matching obligation end date is in the past" should {

            "return 200" in {

              AuthStub.stubResponse(OK, mtdVatAuthResponse)
              VatSubscriptionStub.stubResponse("customer-details", OK, customerInformationSuccessJson)
              VatSubscriptionStub.stubResponse("mandation-status", OK, mandationStatusSuccessJson)
              VatObligationsStub.stubResponse(OK, vatObligationsSuccessJson(endDate = LocalDate.now().minusDays(1)))

              val response: WSResponse = request

              response.status shouldBe OK
            }
          }

          "matching obligation end date is in the future" should {

            "return 400" in {

              AuthStub.stubResponse(OK, mtdVatAuthResponse)
              VatSubscriptionStub.stubResponse("customer-details", OK, customerInformationSuccessJson)
              VatSubscriptionStub.stubResponse("mandation-status", OK, mandationStatusSuccessJson)
              VatObligationsStub.stubResponse(OK, vatObligationsSuccessJson(endDate = LocalDate.now().plusDays(1)))

              val response: WSResponse = request

              response.status shouldBe BAD_REQUEST
            }
          }

          "period key in URL has an encoded character" should {

            "successfully match with a non-encoded obligation period key" in {

              val encodedPeriodKey = "%23005"
              val decodedPeriodKey = "#005"

              val obligation = Json.obj(
                "obligations" -> Json.arr(
                  Json.obj(
                    "start" -> "2018-01-01",
                    "end" -> "2018-03-31",
                    "due" -> "2018-04-07",
                    "periodKey" -> decodedPeriodKey
                  )
                )
              )

              AuthStub.stubResponse(OK, mtdVatAuthResponse)
              VatSubscriptionStub.stubResponse("customer-details", OK, customerInformationSuccessJson)
              VatSubscriptionStub.stubResponse("mandation-status", OK, mandationStatusSuccessJson)
              VatObligationsStub.stubResponse(OK, obligation)

              val response: WSResponse = get(
                s"/$encodedPeriodKey/submit-form",
                formatSessionMandationStatus(Some(MandationStatuses.nonMTDfB)) ++ Map(SessionKeys.HonestyDeclaration.key -> s"$vrn-#005")
              )

              response.status shouldBe OK
              Jsoup.parse(response.body).select("h1 > span:nth-of-type(2)").text() should
                include("1\u00a0Jan to 31\u00a0Mar\u00a02018")
            }
          }
        }

        "retrieval of customer details fails" should {

          "return 500" in {

            AuthStub.stubResponse(OK, mtdVatAuthResponse)
            VatSubscriptionStub.stubResponse("mandation-status", OK, mandationStatusSuccessJson)
            VatObligationsStub.stubResponse(OK, vatObligationsSuccessJson())
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

      "user has not checked the honesty declaration" should {

        "return 303" in {

          AuthStub.stubResponse(OK, mtdVatAuthResponse)
          VatSubscriptionStub.stubResponse("customer-details", OK, customerInformationSuccessJson)
          VatSubscriptionStub.stubResponse("mandation-status", OK, mandationStatusSuccessJson)
          VatObligationsStub.stubResponse(OK, vatObligationsSuccessJson(endDate = LocalDate.now().minusDays(1)))

          val response: WSResponse = get("/18AA/submit-form", formatSessionMandationStatus(Some(MandationStatuses.nonMTDfB)))

          response.status shouldBe SEE_OTHER
        }
      }
    }

    "there is a POST request" when {

      val sessionValues =
        formatSessionMandationStatus(Some(MandationStatuses.nonMTDfB)) ++ Map(SessionKeys.HonestyDeclaration.key -> s"$vrn-18AA")

      val validSubmissionModel = NineBoxModel(
        box1 = 1000.00,
        box2 = 1000.00,
        box4 = 1000.00,
        box6 = 1000.00,
        box7 = 1000.00,
        box8 = 1000.00,
        box9 = 1000.00
      )

      def postRequest(data: NineBoxModel): WSResponse = postForm(
        "/18AA/submit-form",
        sessionValues,
        toFormData(SubmitVatReturnForm().nineBoxForm, data)
      )

      "user is authorised" when {

        "the data posted is valid" should {

          "return 303" in {

            AuthStub.stubResponse(OK, mtdVatAuthResponse)
            VatSubscriptionStub.stubResponse("customer-details", OK, customerInformationSuccessJson)
            VatObligationsStub.stubResponse(OK, vatObligationsSuccessJson())
            val response: WSResponse = postRequest(validSubmissionModel)

            response.status shouldBe SEE_OTHER
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

        def postRequest(data: NineBoxModel): WSResponse = postForm(
          "/18AA/submit-form",
          formatSessionMandationStatus(Some("unsupportedMandationStatus")) ++ Map(SessionKeys.HonestyDeclaration.key -> s"$vrn-18AA"),
          toFormData(SubmitVatReturnForm().nineBoxForm, data)
        )

        "return 403" in {

          AuthStub.stubResponse(OK, mtdVatAuthResponse)
          VatSubscriptionStub.stubResponse("mandation-status", OK, unsupportedMandationStatusJson)

          val response: WSResponse = postRequest(validSubmissionModel)

          response.status shouldBe FORBIDDEN

        }
      }

      "user has not checked the honesty declaration" should {

        def postRequest(data: NineBoxModel): WSResponse = postForm(
          "/18AA/submit-form",
          formatSessionMandationStatus(Some(MandationStatuses.nonMTDfB)),
          toFormData(SubmitVatReturnForm().nineBoxForm, data)
        )

        "return 303" in {

          AuthStub.stubResponse(OK, mtdVatAuthResponse)
          VatSubscriptionStub.stubResponse("customer-details", OK, customerInformationSuccessJson)
          VatObligationsStub.stubResponse(OK, vatObligationsSuccessJson())

          val response: WSResponse = postRequest(validSubmissionModel)

          response.status shouldBe SEE_OTHER
        }
      }
    }
  }
}
