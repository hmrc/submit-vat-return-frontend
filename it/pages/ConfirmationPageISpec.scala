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

import base.BaseISpec
import play.api.http.Status.{OK, FORBIDDEN}
import play.api.libs.ws.WSResponse
import stubs.AuthStub.{mtdVatAuthResponse, otherEnrolmentAuthResponse}
import stubs.{AuthStub, VatSubscriptionStub}
import stubs.VatSubscriptionStub.{customerInformationSuccessJson, mandationStatusSuccessJson, unsupportedMandationStatusJson}

class ConfirmationPageISpec extends BaseISpec {

  "Calling /submission-confirmation" when {

    def request: WSResponse = get("/submission-confirmation")

    "user is authorised" when {

      "return 200" in {

        AuthStub.stubResponse(OK, mtdVatAuthResponse)
        VatSubscriptionStub.stubResponse("customer-details", OK, customerInformationSuccessJson)
        VatSubscriptionStub.stubResponse("mandation-status", OK, mandationStatusSuccessJson)

        val response: WSResponse = request

        response.status shouldBe OK

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

        val response: WSResponse = request

        response.status shouldBe FORBIDDEN

      }
    }

  }
}
