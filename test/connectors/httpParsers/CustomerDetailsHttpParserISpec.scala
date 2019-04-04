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

package connectors.httpParsers

import base.BaseSpec
import connectors.httpParsers.CustomerDetailsHttpParser.CustomerDetailsReads
import models.{CustomerDetails, FailedToParseCustomerDetails}
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HttpResponse
import play.api.http.Status._

class CustomerDetailsHttpParserISpec extends BaseSpec {

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

  "CustomerDetailsReads" should {
    "parse the HTTP response correctly" when {
      "the returned JSON is valid" in {
        val httpResponse = HttpResponse(
          OK,
          Some(correctReturnJson)
        )

        val expectedResult = CustomerDetails(
          Some("Rath"),
          Some("Alos"),
          Some("Blue Rathalos"),
          Some("Silver Rathalos"),
          hasFlatRateScheme = true
        )

        val result = CustomerDetailsReads.read("", "", httpResponse)

        result shouldBe Right(expectedResult)
      }
    }
    "return an error model" when {
      "there is invalid json" in {
        val httpResponse = HttpResponse(
          OK,
          Some(incorrectReturnJson)
        )

        val expectedResult = FailedToParseCustomerDetails

        val result = CustomerDetailsReads.read("", "", httpResponse)

        result shouldBe Left(expectedResult)
      }
    }
  }
}
