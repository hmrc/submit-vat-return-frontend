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

package connectors.httpParsers

import assets.CustomerDetailsTestAssets._
import base.BaseSpec
import connectors.httpParsers.CustomerDetailsHttpParser.CustomerDetailsReads
import models.errors.{ErrorModel, UnexpectedJsonError}
import play.api.http.Status._
import uk.gov.hmrc.http.HttpResponse

class CustomerDetailsHttpParserSpec extends BaseSpec {

  "CustomerDetailsReads" should {
    "parse the HTTP response correctly" when {
      "the returned JSON is valid" in {
        val httpResponse = HttpResponse(
          OK,
          customerDetailsJson,
          Map.empty[String,Seq[String]]
        )

        val expectedResult = customerDetailsModel

        val result = CustomerDetailsReads.read("", "", httpResponse)

        result shouldBe Right(expectedResult)
      }
      "the returned JSON is mostly empty" in {
        val httpResponse = HttpResponse(
          OK,
          customerDetailsJsonMin,
          Map.empty[String,Seq[String]]
        )

        val expectedResult = customerDetailsModelMin

        val result = CustomerDetailsReads.read("", "", httpResponse)

        result shouldBe Right(expectedResult)
      }
    }
    "return an error model" when {
      "there is invalid json" in {
        val httpResponse = HttpResponse(
          OK,
          incorrectReturnJson,
          Map.empty[String,Seq[String]]
        )

        val expectedResult = UnexpectedJsonError

        val result = CustomerDetailsReads.read("", "", httpResponse)

        result shouldBe Left(expectedResult)
      }
      "a non OK status is returned" in {
        val httpResponse = HttpResponse(
          INTERNAL_SERVER_ERROR,
          ""
        )

        val expectedResult = Left(ErrorModel(INTERNAL_SERVER_ERROR, "Received downstream error when retrieving customer details."))

        val result = CustomerDetailsReads.read("", "", httpResponse)

        result shouldBe expectedResult
      }
    }
  }
}
