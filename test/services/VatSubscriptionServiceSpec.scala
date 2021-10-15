/*
 * Copyright 2021 HM Revenue & Customs
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

import assets.CustomerDetailsTestAssets.customerDetailsModel
import base.BaseSpec
import connectors.VatSubscriptionConnector
import models.errors.UnexpectedJsonFormat
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class VatSubscriptionServiceSpec extends BaseSpec {

  val mockConnector: VatSubscriptionConnector = mock[VatSubscriptionConnector]
  val service: VatSubscriptionService = new VatSubscriptionService(mockConnector)

  "getCustomerDetails" should {
    "return a CustomerDetails model" when {
      "the model is returned from the connector" in {
        val expectedResult = customerDetailsModel

        (mockConnector.getCustomerDetails(_: String)(_: HeaderCarrier, _: ExecutionContext))
          .expects("111111111", *, *)
          .returning(Future.successful(Right(expectedResult)))

        val result = service.getCustomerDetails("111111111")

        result shouldBe Right(expectedResult)
      }
    }
    "return an error model" when {
      "an error is returned from the connector" in {
        val expectedResult = UnexpectedJsonFormat

        (mockConnector.getCustomerDetails(_: String)(_: HeaderCarrier, _: ExecutionContext))
          .expects("111111111", *, *)
          .returning(Future.successful(Left(expectedResult)))

        val result = service.getCustomerDetails("111111111")

        result shouldBe Left(expectedResult)
      }
    }
  }
}
