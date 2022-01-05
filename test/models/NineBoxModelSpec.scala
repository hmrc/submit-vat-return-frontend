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

package models

import java.time.LocalDate

import base.BaseSpec
import play.api.libs.json.{JsValue, Json}

class NineBoxModelSpec extends BaseSpec {

  val validJsonString: JsValue = Json.obj(
    "box1" -> "1000",
    "box2" -> "1000",
    "box3" -> "1000",
    "box4" -> "1000",
    "box5" -> "1000",
    "box6" -> "1000",
    "box7" -> "1000",
    "box8" -> "1000",
    "box9" -> "1000",
    "flatRateScheme" -> true,
    "start" -> LocalDate.parse("2018-01-01").toString,
    "end" -> LocalDate.parse("2018-01-04").toString,
    "due" -> LocalDate.parse("2018-01-05").toString
  )

  val validJsonBigInt: JsValue = Json.obj(
    "box1" -> 1000,
    "box2" -> 1000,
    "box3" -> 1000,
    "box4" -> 1000,
    "box5" -> 1000,
    "box6" -> 1000,
    "box7" -> 1000,
    "box8" -> 1000,
    "box9" -> 1000,
    "flatRateScheme" -> true,
    "start" -> LocalDate.parse("2018-01-01").toString,
    "end" -> LocalDate.parse("2018-01-04").toString,
    "due" -> LocalDate.parse("2018-01-05").toString
  )

  val validModel = SubmitVatReturnModel(
    1000,
    1000,
    1000,
    1000,
    1000,
    1000,
    1000,
    1000,
    1000,
    flatRateScheme = true,
    LocalDate.parse("2018-01-01"),
    LocalDate.parse("2018-01-04"),
    LocalDate.parse("2018-01-05")
  )

  "SubmitVatReturnModel" should {
    "correctly parse" when {
      "provided with valid json" in {
        validJsonString.as[SubmitVatReturnModel] shouldBe validModel
        validJsonBigInt.as[SubmitVatReturnModel] shouldBe validModel
      }
    }
    "correctly parse into json" in {
      Json.toJson(validModel) shouldBe validJsonBigInt
    }
  }
}
