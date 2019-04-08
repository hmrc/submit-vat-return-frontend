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
import connectors.VatSubscriptionConnector
import models.{CustomerDetails, FailedToParseCustomerDetails}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class VatSubscriptionServiceSpec extends BaseSpec {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val mockConnector: VatSubscriptionConnector = mock[VatSubscriptionConnector]
  val service: VatSubscriptionService = new VatSubscriptionService(mockConnector)

  "getCustomerDetails" should {
    "return a CustomerDetails model" when {
      "the model is returned from the connector" in {
        val expectedResult = CustomerDetails(
          Some("Hiccup"),
          Some("Toothless"),
          Some("Rimuru Tempest"),
          Some("Sadao Maou"),
          hasFlatRateScheme = true
        )

        (mockConnector.getCustomerDetails(_: String)(_: HeaderCarrier, _: ExecutionContext))
          .expects("111111111", *, *)
          .returning(Future.successful(Right(expectedResult)))

        val result = await(service.getCustomerDetails("111111111"))

        result shouldBe Right(expectedResult)
      }
    }
    "return an error model" when {
      "an error is returned from the connector" in {
        val expectedResult = FailedToParseCustomerDetails

        (mockConnector.getCustomerDetails(_: String)(_: HeaderCarrier, _: ExecutionContext))
          .expects("111111111", *, *)
          .returning(Future.successful(Left(expectedResult)))

        val result = await(service.getCustomerDetails("111111111"))

        result shouldBe Left(expectedResult)
      }
    }
  }
}
