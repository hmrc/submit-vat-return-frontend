/*
 * Copyright 2022 HM Revenue & Customs
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

package connectors

import utils.NrsTestData
import base.BaseISpec
import models.errors.{ErrorModel, UnexpectedJsonError}
import models.nrs.SuccessModel
import models.vatReturnSubmission.{SubmissionModel, SubmissionSuccessModel}
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import stubs.VatReturnsStub
import stubs.VatReturnsStub._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

class VatReturnsConnectorISpec extends BaseISpec {
  
  private trait Test {
    val connector: VatReturnsConnector = app.injector.instanceOf[VatReturnsConnector]
    implicit val hc: HeaderCarrier = HeaderCarrier()
  }
  
  "Calling .submitVatReturn" when {

    val model: SubmissionModel = SubmissionModel(
      periodKey = "#001",
      vatDueSales = 9999999999999.99,
      vatDueAcquisitions = -9999999999999.99,
      vatDueTotal = 0.00,
      vatReclaimedCurrPeriod = 0.00,
      vatDueNet = 0.00,
      totalValueSalesExVAT = 0.00,
      totalValuePurchasesExVAT = 0.00,
      totalValueGoodsSuppliedExVAT = 0.00,
      totalAllAcquisitionsExVAT = 0.00,
      agentReferenceNumber = Some("XAIT1234567")
    )

    val postRequestJsonBody: JsValue = Json.parse(
      """
        |{
        |  "periodKey" : "#001",
        |  "vatDueSales" : 9999999999999.99,
        |  "vatDueAcquisitions" : -9999999999999.99,
        |  "vatDueTotal" : 0.00,
        |  "vatReclaimedCurrPeriod" : 0.00,
        |  "vatDueNet" : 0.00,
        |  "totalValueSalesExVAT" : 0.00,
        |  "totalValuePurchasesExVAT" : 0.00,
        |  "totalValueGoodsSuppliedExVAT" : 0.00,
        |  "totalAllAcquisitionsExVAT" : 0.00,
        |  "agentReferenceNumber" : "XAIT1234567"
        |}
      """.stripMargin
    )

    "response is 200" when {

      "response body is valid" should {

        "return a SuccessModel" in new Test {

          VatReturnsStub.stubResponse(vatReturnUri("999999999"))(OK, Json.obj("formBundleNumber" -> "12345"))

          private val result = await(connector.submitVatReturn("999999999", model))
          VatReturnsStub.verifyVatReturnSubmission("999999999", postRequestJsonBody)

          result shouldBe Right(SubmissionSuccessModel(formBundleNumber = "12345"))
        }
      }

      "response body is invalid" should {

        "return an InvalidJsonResponse" in new Test {

          VatReturnsStub.stubResponse(vatReturnUri("999999999"))(OK, Json.obj())

          private val result = await(connector.submitVatReturn("999999999", model))
          VatReturnsStub.verifyVatReturnSubmission("999999999", postRequestJsonBody)

          result shouldBe Left(UnexpectedJsonError)
        }
      }
    }

    "response is unexpected" should {

      "return a ServerSideError" in new Test {

        VatReturnsStub.stubResponse(vatReturnUri("999999999"))(
          INTERNAL_SERVER_ERROR,
          Json.obj("code" -> "500", "reason" -> "DES")
        )

        private val result = await(connector.submitVatReturn("999999999", model))
        VatReturnsStub.verifyVatReturnSubmission("999999999", postRequestJsonBody)

        result shouldBe Left(ErrorModel(INTERNAL_SERVER_ERROR, "Received downstream error when submitting VAT return."))
      }
    }
  }

  "Calling .nrsSubmission" when {

    val postRequestJsonBody: JsValue = NrsTestData.FullRequestTestData.expectedJson
    val postRequestModel = NrsTestData.FullRequestTestData.requestModel

    "response is 202" when {

      "response body is valid" should {

        "return a SuccessModel" in new Test {

          VatReturnsStub.stubResponse(nrsSubmissionUri)(ACCEPTED, Json.obj("nrSubmissionId" -> "12345"))

          private val result = await(connector.nrsSubmission(postRequestModel, vrn))
          VatReturnsStub.verifyNrsSubmission(postRequestJsonBody)

          result shouldBe Right(SuccessModel(nrSubmissionId = "12345"))
        }
      }

      "response body is invalid" should {

        "return an InvalidJsonResponse" in new Test {

          VatReturnsStub.stubResponse(nrsSubmissionUri)(ACCEPTED, Json.obj("nope" -> "nope"))

          private val result = await(connector.nrsSubmission(postRequestModel, vrn))
          VatReturnsStub.verifyNrsSubmission(postRequestJsonBody)

          result shouldBe Right(SuccessModel(""))
        }
      }
    }

    "response is unexpected" should {

      "return an error model" in new Test {

        VatReturnsStub.stubResponse(nrsSubmissionUri)(
          INTERNAL_SERVER_ERROR,
          Json.obj("code" -> "500", "reason" -> "oh no")
        )

        private val result = await(connector.nrsSubmission(postRequestModel, vrn))
        VatReturnsStub.verifyNrsSubmission(postRequestJsonBody)

        result shouldBe Left(ErrorModel(INTERNAL_SERVER_ERROR, "Received downstream error when submitting to NRS"))
      }
    }
  }
}
