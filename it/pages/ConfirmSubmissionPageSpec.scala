/*
 * Copyright 2020 HM Revenue & Customs
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

import assets.NrsAssets
import common.SessionKeys
import org.jsoup.Jsoup
import org.scalatest.GivenWhenThen
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.WSResponse
import stubs.AuthStub._
import stubs.VatReturnsStub._
import stubs.{AuthStub, BtaLinkPartialStub, VatReturnsStub, VatSubscriptionStub}


class ConfirmSubmissionPageSpec extends NrsAssets with GivenWhenThen {

  "Calling /:periodKey/confirm-submission" when {

    def nineBoxSessionData(endDate: String): String = Json.obj(
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
      "end" -> endDate,
      "due" -> "2018-03-07"
    ).toString()

    val mandationStatusSessionValue = Map("mtdVatMandationStatus" -> "Non MTDfB")
    val nineBoxSessionValue = Map("mtdNineBoxReturnData" -> nineBoxSessionData(LocalDate.now().minusDays(1).toString))
    val honestySessionValue: String => Map[String, String] = periodKey => Map(SessionKeys.HonestyDeclaration.key -> s"$vrn-$periodKey")
    val fullSessionValues: Map[String, String] = mandationStatusSessionValue ++ nineBoxSessionValue ++ honestySessionValue("18AA")

    def getRequest(sessionValues: Map[String, String] = Map.empty): WSResponse = get("/18AA/confirm-submission", sessionValues)
    def request(sessionValues: Map[String, String] = Map.empty): WSResponse = postJson("/18AA/confirm-submission", sessionValues)

    "user is authorised" when {

      "valid nine box session data exists" when {

        "matching obligation end date is in the past" when {

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


          val customerDetails: JsObject = Json.obj(
            "firstName" -> "Test",
            "lastName" -> "Name",
            "tradingName" -> "",
            "organisationName" -> "",
            "hasFlatRateScheme" -> false
          )

          "NRS returns successful response and backend submission returns 200" should {

            "redirect to confirmation page" in {
              When("The user is authenticated and authorised")
              AuthStub.stubResponse(OK, mtdVatAuthResponse)

              And("The customer-details call is successful")
              VatSubscriptionStub.stubResponse("customer-details", OK, customerDetails)

              And("The auth call for full information is successful")
              AuthStub.stubResponse(OK, fullNRSAuthResponse)

              And("The request to BTA is successful")
              BtaLinkPartialStub.stubResponse(OK)

              And("The POST to NRS is successful")
              VatReturnsStub.stubResponse(nrsSubmissionUri)(ACCEPTED, Json.obj("nrSubmissionId" -> "nrsIDExample"))

              And("The POST to vat-returns is successful")
              VatReturnsStub.stubResponse(vatReturnUri("999999999"))(OK, Json.obj("formBundleNumber" -> "12345"))

              val response: WSResponse = request(fullSessionValues)

              And("The NRS submission was made with the correct request body")
              VatReturnsStub.nrsRegexMatcher(nrsFullSubmissionJson)

              And("The backend submission was made with the correct nine box value mappings and headers")
              VatReturnsStub.verifyVatReturnSubmission("999999999", postRequestJsonBody)

              Then("The response should be 303")
              response.status shouldBe SEE_OTHER

              And("The redirect location is correct")
              response.header("Location") shouldBe Some(controllers.routes.ConfirmationController.show().url)
            }
          }

          "NRS returns a BAD_REQUEST" should {

            "return ISE" in {
              When("The user is authenticated and authorised")
              AuthStub.stubResponse(OK, mtdVatAuthResponse)

              And("The customer-details call is successful")
              VatSubscriptionStub.stubResponse("customer-details", OK, customerDetails)

              And("The auth call for full information is successful")
              AuthStub.stubResponse(OK, fullNRSAuthResponse)

              And("The response from NRS is BAD_REQUEST")
              VatReturnsStub.stubResponse(nrsSubmissionUri)(BAD_REQUEST, Json.obj())

              val response: WSResponse = request(fullSessionValues)

              Then("The response should be 500")
              response.status shouldBe INTERNAL_SERVER_ERROR
            }
          }

          "NRS returns an error other than a BAD_REQUEST and the backend submission returns 200" should {

            "redirect to confirmation page" in {
              When("The user is authenticated and authorised")
              AuthStub.stubResponse(OK, mtdVatAuthResponse)

              And("The request to BTA is successful")
              BtaLinkPartialStub.stubResponse(OK)

              And("The customer-details call is successful")
              VatSubscriptionStub.stubResponse("customer-details", OK, customerDetails)

              And("The auth call for full information is successful")
              AuthStub.stubResponse(OK, fullNRSAuthResponse)

              And("The POST to NRS returns an internal server error")
              VatReturnsStub.stubResponse(nrsSubmissionUri)(INTERNAL_SERVER_ERROR, Json.obj())

              And("The POST to vat-returns is successful")
              VatReturnsStub.stubResponse(vatReturnUri("999999999"))(OK, Json.obj("formBundleNumber" -> "12345"))

              val response: WSResponse = request(fullSessionValues)

              And("The backend submission was made with the correct nine box value mappings and headers")
              VatReturnsStub.verifyVatReturnSubmission("999999999", postRequestJsonBody)

              Then("The response should be 303")
              response.status shouldBe SEE_OTHER

              And("The redirect location is correct")
              response.header("Location") shouldBe Some(controllers.routes.ConfirmationController.show().url)
            }
          }

          "The Bta service for the partial returns a SERVICE_UNAVAILABLE" should {

            "display the backup partial" in {
              When("The user is authenticated and authorised")
              AuthStub.stubResponse(OK, mtdVatAuthResponse)

              And("The request to BTA is unsuccessful")
              BtaLinkPartialStub.stubResponse(SERVICE_UNAVAILABLE)

              And("The customer-details call is successful")
              VatSubscriptionStub.stubResponse("customer-details", OK, customerDetails)

              And("The auth call for full information is successful")
              AuthStub.stubResponse(OK, fullNRSAuthResponse)

              And("The POST to NRS is successful")
              VatReturnsStub.stubResponse(nrsSubmissionUri)(OK, Json.obj())

              And("The POST to vat-returns is successful")
              VatReturnsStub.stubResponse(vatReturnUri("999999999"))(OK, Json.obj("formBundleNumber" -> "12345"))

              val response: WSResponse = getRequest(fullSessionValues)

              And("The backend submission was made with the correct nine box value mappings and headers")
              VatReturnsStub.verifyVatReturnSubmission("999999999", postRequestJsonBody)

              Jsoup.parse(response.body).getElementById("service-info-home-link").text shouldBe "Home"
              Jsoup.parse(response.body).getElementById("service-info-manage-account-link").text shouldBe "Manage account"
              Jsoup.parse(response.body).getElementById("service-info-messages-link").text shouldBe "Messages"
              Jsoup.parse(response.body).getElementById("service-info-help-and-contact-link").text shouldBe "Help and contact"
            }
          }


        }

        "matching obligation end date is in the future" should {

          "return 400" in {

            When("The user is authenticated and authorised")
            AuthStub.stubResponse(OK, mtdVatAuthResponse)

            And("The request to BTA is successful")
            BtaLinkPartialStub.stubResponse(OK)

            val response: WSResponse = request(
              Map("mtdNineBoxReturnData" -> nineBoxSessionData(LocalDate.now().plusDays(1).toString)) ++
                mandationStatusSessionValue ++ honestySessionValue("18AA")
            )

            Then("The response should be 400")
            response.status shouldBe BAD_REQUEST
          }
        }

        "period key in URL has an encoded character" should {

          "make the backend submission with the decoded period key" in {

            val encodedPeriodKey = "%23005"
            val decodedPeriodKey = "#005"

            val postRequestJsonBody: JsValue = Json.parse(
              s"""
                 |{
                 |  "periodKey" : "$decodedPeriodKey",
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

            When("The user is authenticated and authorised")
            AuthStub.stubResponse(OK, fullNRSAuthResponse)

            And("The POST to vat-returns is successful")
            VatReturnsStub.stubResponse(vatReturnUri("999999999"))(OK, Json.obj("formBundleNumber" -> "12345"))

            val response: WSResponse = postJson(s"/$encodedPeriodKey/confirm-submission",
              mandationStatusSessionValue ++ nineBoxSessionValue ++ honestySessionValue("#005")
            )

            And("The backend submission contained the decoded period key")
            VatReturnsStub.verifyVatReturnSubmission("999999999", postRequestJsonBody)

            Then("The response should be 303")
            response.status shouldBe SEE_OTHER

            And("The redirect location is correct")
            response.header("Location") shouldBe Some(controllers.routes.ConfirmationController.show().url)
          }
        }
      }

      "session data is invalid" should {

        "redirect to entry page" in {

          When("The user is authenticated and authorised")
          AuthStub.stubResponse(OK, mtdVatAuthResponse)

          And("The session data for mtdNineBoxReturnData is empty")
          val invalidSessionData = Map("mtdNineBoxReturnData" -> "") ++ mandationStatusSessionValue ++ honestySessionValue("18AA")

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
          val response: WSResponse = request(mandationStatusSessionValue ++ honestySessionValue("18AA"))

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

    "user has not checked the honesty declaration" should {

      "return 303" in {

        When("The user is authenticated")
        AuthStub.stubResponse(OK, mtdVatAuthResponse)

        val response: WSResponse = request(mandationStatusSessionValue ++ nineBoxSessionValue)

        Then("The response should be 303")
        response.status shouldBe SEE_OTHER
      }
    }
  }
}
