/*
 * Copyright 2018 HM Revenue & Customs
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

import base.BaseISpec
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import stubs.AuthStub._
import stubs.VatSubscriptionStub._
import stubs.{AuthStub, VatReturnsStub, VatSubscriptionStub}

class ConfirmSubmissionPageSpec extends BaseISpec {

  "Calling /:periodKey/confirm-submission" when {

    val mandationStatus = Map("mtdVatMandationStatus" -> "Non MTDfB")

    def request: WSResponse = post("/18AA/confirm-submission",  mandationStatus)

    "user is authorised" when {

      "valid nine box session data exists" when {

        val nineBoxData = Json.obj(
          "box1" -> "10.01",
          "box2" -> "10.02",
          "box3" -> "10.03",
          "box4" -> "10.04",
          "box5" -> "10.05",
          "box6" -> "10.06",
          "box7" -> "10.07",
          "box8" -> "10.08",
          "box9" -> "10.09"
        ).toString()

        val headers = Map("mtdNineBoxReturnData" -> nineBoxData) ++ mandationStatus

        "backend submission returns 200" should {

          "redirect to confirmation page" in {

            AuthStub.stubResponse(OK, mtdVatAuthResponse)
            VatReturnsStub.stubResponse("999999999")(OK, Json.obj("formBundleNumber" -> "12345"))

            val response: WSResponse = post("/18AA/confirm-submission", headers)

            response.status shouldBe SEE_OTHER
          }
        }

        "backend submission returns an error" should {

          "return ISE" in {

            AuthStub.stubResponse(OK, mtdVatAuthResponse)
            VatReturnsStub.stubResponse("999999999")(SERVICE_UNAVAILABLE, Json.obj("oh no" -> "oh yes"))

            val response: WSResponse = post("/18AA/confirm-submission", headers)

            response.status shouldBe INTERNAL_SERVER_ERROR
          }
        }
      }

      "session data is invalid" should {

        "return ISE" in {

          AuthStub.stubResponse(OK, mtdVatAuthResponse)

          val response: WSResponse = post("/18AA/confirm-submission", Map("mtdNineBoxReturnData" -> "") ++ mandationStatus, Json.obj())

          response.status shouldBe INTERNAL_SERVER_ERROR
        }
      }

      "session data is missing" should {

        "redirect to entry page" in {

          AuthStub.stubResponse(OK, mtdVatAuthResponse)

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

      "return 403" in {

        AuthStub.stubResponse(OK, mtdVatAuthResponse)
        VatSubscriptionStub.stubResponse("mandation-status", OK, unsupportedMandationStatusJson)

        val response: WSResponse = post("/18AA/confirm-submission", body = Json.obj())

        response.status shouldBe FORBIDDEN
      }
    }
  }
}
