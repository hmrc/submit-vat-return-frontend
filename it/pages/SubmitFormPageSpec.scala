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
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import stubs.AuthStub._
import stubs.VatObligationsStub._
import stubs.VatSubscriptionStub._
import stubs.{AuthStub, VatObligationsStub, VatSubscriptionStub}

class SubmitFormPageSpec extends BaseISpec {

  "Calling /:periodKey/submit-form" when {

    def request: WSResponse = get("/18AA/submit-form")

    "user is authorised" when {

      "retrieval of customer details and obligation data is successful" should {

        "return 200" in {

          AuthStub.stubResponse(OK, mtdVatAuthResponse)
          VatSubscriptionStub.stubResponse(OK, vatSubscriptionSuccessJson)
          VatObligationsStub.stubResponse(OK, vatObligationsSuccessJson)

          val response: WSResponse = request

          response.status shouldBe OK
        }
      }

      "retrieval of customer details fails" should {

        "return 500" in {

          AuthStub.stubResponse(OK, mtdVatAuthResponse)
          VatSubscriptionStub.stubResponse(SERVICE_UNAVAILABLE, Json.obj())
          VatObligationsStub.stubResponse(OK, vatObligationsSuccessJson)

          val response: WSResponse = request

          response.status shouldBe INTERNAL_SERVER_ERROR
        }
      }

      "retrieval of obligation data fails" should {

        "return 500" in {

          AuthStub.stubResponse(OK, mtdVatAuthResponse)
          VatSubscriptionStub.stubResponse(OK, vatSubscriptionSuccessJson)
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
  }
}
