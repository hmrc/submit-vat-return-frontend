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

package connectors

import base.BaseISpec
import connectors.httpParsers.ResponseHttpParsers.HttpGetResult
import models.CustomerDetails
import models.errors.{ServerSideError, UnexpectedJsonFormat}
import play.api.http.Status._
import stubs.VatSubscriptionStub._
import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.ExecutionContext.Implicits.global

class VatSubscriptionConnectorISpec extends BaseISpec {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  lazy val connector: VatSubscriptionConnector = new VatSubscriptionConnector(httpClient, appConfig)

  "getCustomerDetails" when {

    val vrn = "111111111"

    "response is 200" when {

      "response JSON is valid" should {

        "return a CustomerDetails model" in {

          val expectedModel = CustomerDetails(
            Some("Rath"),
            Some("Alos"),
            Some("Blue Rathalos"),
            Some("Silver Rathalos"),
            hasFlatRateScheme = true
          )

          stubGet(s"/vat-subscription/$vrn/customer-details", customerInformationSuccessJson.toString(), OK)

          val result: HttpGetResult[CustomerDetails] = await(connector.getCustomerDetails(vrn))
          result shouldBe Right(expectedModel)
        }
      }

      "response JSON is invalid" should {

        "return an error model" in {

          stubGet(s"/vat-subscription/$vrn/customer-details", vatSubscriptionInvalidJson.toString(), OK)

          val result: HttpGetResult[CustomerDetails] = await(connector.getCustomerDetails(vrn))
          result shouldBe Left(UnexpectedJsonFormat)
        }
      }
    }

    "response is not 200" should {

      "return an error model" in {

        stubGet(s"/vat-subscription/$vrn/customer-details", "", SERVICE_UNAVAILABLE)

        val result: HttpGetResult[CustomerDetails] = await(connector.getCustomerDetails(vrn))
        result shouldBe Left(ServerSideError("503", "Received downstream error when retrieving customer details."))
      }
    }
  }
}
