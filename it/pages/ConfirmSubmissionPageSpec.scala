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
import org.scalatest.GivenWhenThen
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSResponse
import stubs.AuthStub._
import stubs.{AuthStub, VatReturnsStub}

class ConfirmSubmissionPageSpec extends BaseISpec with GivenWhenThen {

  "Calling /:periodKey/confirm-submission" when {

    val nineBoxSessionData = Json.obj(
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
      "start" -> "2018-01-01",
      "end" -> "2018-01-31",
      "due" -> "2018-03-07"
    ).toString()

    val mandationStatusSessionValue = Map("mtdVatMandationStatus" -> "Non MTDfB")
    val nineBoxSessionValue = Map("mtdNineBoxReturnData" -> nineBoxSessionData)
    val fullSessionValues = mandationStatusSessionValue ++ nineBoxSessionValue

    def request(sessionValues: Map[String, String] = Map.empty): WSResponse = postJson("/18AA/confirm-submission", sessionValues)

    "user is authorised" when {

      "valid nine box session data exists" when {

        val postRequestJsonBody: JsValue = Json.parse(
          """
            |{
            |  "periodKey" : "18AA",
            |  "vatDueSales" : 10.01,
            |  "vatDueAcquisitions" : 10.02,
            |  "vatDueTotal" : 10.03,
            |  "vatReclaimedCurrPeriod" : 10.04,
            |  "vatDueNet" : 10.05,
            |  "totalValueSalesExVAT" : 10.06,
            |  "totalValuePurchasesExVAT" : 10.07,
            |  "totalValueGoodsSuppliedExVAT" : 10.08,
            |  "totalAllAcquisitionsExVAT" : 10.09
            |}
          """.stripMargin
        )

        "backend submission returns 200" should {

          "redirect to confirmation page" in {

            When("The user is authenticated and authorised")
            AuthStub.stubResponse(OK, mtdVatAuthResponse)

            And("The POST to vat-returns is successful")
            VatReturnsStub.stubResponse("999999999")(OK, Json.obj("formBundleNumber" -> "12345"))

            val response: WSResponse = request(fullSessionValues)

            And("The backend submission was made with the correct nine box value mappings and headers")
            VatReturnsStub.verifySubmission("999999999", postRequestJsonBody)

            Then("The response should be 303")
            response.status shouldBe SEE_OTHER

            And("The redirect location is correct")
            response.header("Location") shouldBe Some(controllers.routes.ConfirmationController.show().url)
          }
        }

        "backend submission returns an error" should {

          "return ISE" in {

            When("The user is authenticated and authorised")
            AuthStub.stubResponse(OK, mtdVatAuthResponse)

            And("The POST to vat-returns is unsuccessful")
            VatReturnsStub.stubResponse("999999999")(SERVICE_UNAVAILABLE, Json.obj("oh no" -> "oh yes"))

            val response: WSResponse = request(fullSessionValues)

            And("The backend submission was made with the correct nine box values")
            VatReturnsStub.verifySubmission("999999999", postRequestJsonBody)

            Then("The response should be 500")
            response.status shouldBe INTERNAL_SERVER_ERROR
          }
        }
      }

      "session data is invalid" should {

        "redirect to entry page" in {

          When("The user is authenticated and authorised")
          AuthStub.stubResponse(OK, mtdVatAuthResponse)

          And("The session data for mtdNineBoxReturnData is empty")
          val invalidSessionData = Map("mtdNineBoxReturnData" -> "") ++ mandationStatusSessionValue

          val response: WSResponse = request(invalidSessionData)

          Then("The response should be 303")
          response.status shouldBe SEE_OTHER

          And("The redirect location is correct")
          response.header("Location") shouldBe Some(controllers.routes.SubmitFormController.show("18AA").url)
        }
      }

      "nine box session data is missing" should {

        "redirect to entry page" in {

          When("The user is authenticated and authorised")
          AuthStub.stubResponse(OK, mtdVatAuthResponse)

          And("No session data exists for mtdNineBoxReturnData")
          val response: WSResponse = request(mandationStatusSessionValue)

          Then("The response should be 303")
          response.status shouldBe SEE_OTHER

          And("The redirect location is correct")
          response.header("Location") shouldBe Some(controllers.routes.SubmitFormController.show("18AA").url)
        }
      }
    }

    "user is unauthorised" should {

      "return 403" in {

        When("The user is unauthorised")
        AuthStub.stubResponse(OK, otherEnrolmentAuthResponse)

        val response: WSResponse = request()

        Then("The response should be 403")
        response.status shouldBe FORBIDDEN
      }
    }

    "user is not opted-out" should {

      "return 403" in {

        When("The user is authenticated")
        AuthStub.stubResponse(OK, mtdVatAuthResponse)

        val response: WSResponse = request(Map("mtdVatMandationStatus" -> "MTDfB Voluntary"))

        Then("The response should be 403")
        response.status shouldBe FORBIDDEN
      }
    }
  }
}
