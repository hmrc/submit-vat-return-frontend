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

package connectors

import base.BaseISpec
import models.{CustomerDetails, ErrorModel, FailedToParseCustomerDetails}
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

class VatSubscriptionConnectorISpec extends BaseISpec {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val correctReturnJson: JsObject = Json.obj(
    "firstName" -> "Rath",
    "lastName" -> "Alos",
    "tradingName" -> "Blue Rathalos",
    "organisationName" -> "Silver Rathalos",
    "hasFlatRateScheme" -> true
  )

  val incorrectReturnJson: JsObject = Json.obj(
    "monster" -> "Rathalos",
    "bestWeapon" -> "Swaxe"
  )

  "getCustomerDetails" should {
    "return a CustomerDetails model" when {
      "correct JSON is returned from vat-subscription" in {
        val vrn = "111111111"

        val expectedReturn = CustomerDetails(
          Some("Rath"),
          Some("Alos"),
          Some("Blue Rathalos"),
          Some("Silver Rathalos"),
          hasFlatRateScheme = true
        )

        stubGet(s"/vat-subscription/$vrn/customer-details", Json.stringify(correctReturnJson), OK)

        val result: Either[ErrorModel, CustomerDetails] = await(connector.getCustomerDetails(vrn))
        result shouldBe Right(expectedReturn)
      }
    }
    "return an error model" when {
      "the JSON cannot be parsed" in {
        val vrn = "111111111"

        val expectedReturn = FailedToParseCustomerDetails

        stubGet(s"/vat-subscription/$vrn/customer-details", Json.stringify(incorrectReturnJson), OK)

        val result: Either[ErrorModel, CustomerDetails] = await(connector.getCustomerDetails(vrn))
        result shouldBe Left(expectedReturn)
      }
    }
  }
}
