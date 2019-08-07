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

package services

import base.BaseSpec
import connectors.VatReturnsConnector
import connectors.httpParsers.ResponseHttpParsers.HttpGetResult
import models.auth.User
import models.errors.UnexpectedJsonFormat
import models.nrs.{RequestModel, SearchKeys, SuccessModel}
import models.vatReturnSubmission.{SubmissionModel, SubmissionSuccessModel}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import assets.NrsTestData.IdentityDataTestData.{correctModel => testIdentityModel}
import assets.NrsTestData.ReceiptTestData.{correctModel => testReceiptDataModel}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class VatReturnsServiceSpec extends BaseSpec {

  val mockConnector: VatReturnsConnector = mock[VatReturnsConnector]
  val service = new VatReturnsService(mockConnector)

  "Calling .submitVatReturn" when {

    val submissionModel = SubmissionModel(
      periodKey = "19AA",
      vatDueSales = 10.01,
      vatDueAcquisitions = 10.02,
      vatDueTotal = 10.03,
      vatReclaimedCurrPeriod = 10.04,
      vatDueNet = 10.05,
      totalValueSalesExVAT = 10.06,
      totalValuePurchasesExVAT = 10.07,
      totalValueGoodsSuppliedExVAT = 10.08,
      totalAllAcquisitionsExVAT = 10.09,
      agentReferenceNumber = Some("XARN1234567")
    )

    "submission is successful" should {

      val expectedResult = Right(SubmissionSuccessModel(formBundleNumber = "12345"))

      "return a SubmissionModel" in {
        (mockConnector.submitVatReturn(_: String, _: SubmissionModel)(_: HeaderCarrier, _: ExecutionContext))
          .expects(*, *, *, *)
          .returning(Future.successful(expectedResult))

        val result: HttpGetResult[SubmissionSuccessModel] = await(service.submitVatReturn("999999999", submissionModel))

        result shouldBe expectedResult
      }
    }

    "submission is unsuccessful" should {

      val expectedResult = Left(UnexpectedJsonFormat)

      "return a HttpError" in {
        (mockConnector.submitVatReturn(_: String, _: SubmissionModel)(_: HeaderCarrier, _: ExecutionContext))
          .expects(*, *, *, *)
          .returning(Future.successful(expectedResult))

        val result: HttpGetResult[SubmissionSuccessModel] = await(service.submitVatReturn("999999999", submissionModel))

        result shouldBe expectedResult
      }
    }
  }

  "Calling .nrsSubmission" when {

    lazy val request: User[AnyContentAsEmpty.type] = User[AnyContentAsEmpty.type](vrn)(FakeRequest().withHeaders("Authorization" -> ""))

    "submission is successful" should {

      val expectedResult = Right(SuccessModel(nrSubmissionId = "12345"))

      "return a SuccessModel" in {

        (mockConnector.nrsSubmission(_: RequestModel, _: String)(_: HeaderCarrier, _: ExecutionContext))
          .expects(*, *, *, *)
          .returning(Future.successful(expectedResult))

        val result: HttpGetResult[SuccessModel] =
          await(service.nrsSubmission("18AA", "payload", "checksum", testIdentityModel, testReceiptDataModel)(hc, ec,request))

        result shouldBe expectedResult
      }
    }

    "submission is unsuccessful" should {

      val expectedResult = Left(UnexpectedJsonFormat)

      "return a HttpError" in {

        (mockConnector.nrsSubmission(_: RequestModel, _: String)(_: HeaderCarrier, _: ExecutionContext))
          .expects(*, *, *, *)
          .returning(Future.successful(expectedResult))

        val result: HttpGetResult[SuccessModel] =
          await(service.nrsSubmission("18AA", "payload", "checksum", testIdentityModel, testReceiptDataModel)(hc, ec, request))

        result shouldBe expectedResult
      }
    }
  }

  "Calling .searchKeys" should {

    "return vrn and decoded period key in a model" in {
      service.searchKeys("123456789", "%23001") shouldBe SearchKeys("123456789", "#001")
    }
  }
}
