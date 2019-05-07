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

package models

import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec

class VatReturnDetailsSpec extends UnitSpec {

  "A VatReturnDetails object" should {

    val exampleVatReturn: VatReturnDetails = VatReturnDetails(
      boxOne = 1000.01,
      boxTwo = 1000.01,
      boxThree = 1000.01,
      boxFour = 1000.01,
      boxFive = 1000.01,
      boxSix = 1000.01,
      boxSeven = 1000.01,
      boxEight = 1000.01,
      boxNine = 1000.01
    )

    val exampleVatReturnJson =
      """{
        |"boxOne":1000.01,
        |"boxTwo":1000.01,
        |"boxThree":1000.01,
        |"boxFour":1000.01,
        |"boxFive":1000.01,
        |"boxSix":1000.01,
        |"boxSeven":1000.01,
        |"boxEight":1000.01,
        |"boxNine":1000.01
        |}"""
        .stripMargin.replace("\n","")

    "parse to JSON" in {
      val result = Json.toJson(exampleVatReturn).toString()
      result shouldBe exampleVatReturnJson
    }

    "be parsed successfully from appropriate JSON" in {
      val result = Json.parse(exampleVatReturnJson).as[VatReturnDetails]
      result shouldBe exampleVatReturn
    }
  }
}
