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

package connectors

import base.BaseSpec
import mocks.MockHttp
import assets.CustomerDetailsTestAssets._
import connectors.httpParsers.ResponseHttpParsers.HttpResult
import models.{CustomerDetails, MandationStatus}
import common.MandationStatuses.nonMTDfB
import play.api.http.Status
import uk.gov.hmrc.http.HttpResponse
import play.api.test.Helpers.{await, defaultAwaitTimeout}

import scala.concurrent.Future

class VatSubscriptionConnectorSpec extends BaseSpec with MockHttp {

  val errorModel: HttpResponse = HttpResponse(Status.BAD_REQUEST, "Error Message")

  object TestVatSubscriptionConnector extends VatSubscriptionConnector(mockHttp, mockAppConfig)

  "VatSubscriptionConnector" when {

    "calling the 'vatSubscriptionUrl' method" should {

      "format the url correctly" in {

        val testUrl = TestVatSubscriptionConnector.vatSubscriptionUrl(vrn, "endpoint")
        testUrl shouldBe s"${mockAppConfig.vatSubscriptionBaseUrl}/vat-subscription/999999999/endpoint"
      }
    }

    "calling 'getCustomerDetails'" when {

      def result: Future[HttpResult[CustomerDetails]] = TestVatSubscriptionConnector.getCustomerDetails(vrn)

      "a successful response is returned" should {

        "return a CustomerDetailsModel" in {
          setupMockHttpGet(Right(customerDetailsModel))
          await(result) shouldBe Right(customerDetailsModel)
        }
      }

      "given an error should" should {

        "return a Left with an ErrorModel" in {
          setupMockHttpGet(Left(errorModel))
          await(result) shouldBe Left(errorModel)
        }
      }
    }

    "calling the 'getMandationStatus' method" when {

      def result: Future[HttpResult[MandationStatus]] = TestVatSubscriptionConnector.getCustomerMandationStatus(vrn)

      "a successful response is returned" should {

        "return a mandation status" in {
          setupMockHttpGet(Right(MandationStatus(nonMTDfB)))
          await(result) shouldBe Right(MandationStatus(nonMTDfB))
        }
      }

      "an error is returned" should {

        "return a Left with an ErrorModel" in {
          setupMockHttpGet(Left(errorModel))
          await(result) shouldBe Left(errorModel)
        }
      }
    }
  }
}
