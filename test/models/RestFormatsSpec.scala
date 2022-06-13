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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsNumber, JsResultException, JsString, Json, JsonValidationError}

import java.time.LocalDate

class RestFormatsSpec extends AnyWordSpecLike with Matchers {

  "The LocalDate reads" should {

    "parse the provided JSON date to a LocalDate object when it matches the expected regex" in {
      val json = JsString("2000-01-01")
      json.as[LocalDate](RestFormats.localDateRead) shouldBe LocalDate.parse("2000-01-01")
    }

    "fail to parse" when {

      "the provided JSON date is not a valid date" in {
        val json = JsString("2000-01-32")
        val exception = intercept[JsResultException](json.as[LocalDate](RestFormats.localDateRead))
        exception.errors.head._2 shouldBe List(JsonValidationError(List("2000-01-32 is not a valid date")))
      }

      "the provided JSON date does not match the expected regex" in {
        val json = JsString("20000-001-001")
        val exception = intercept[JsResultException](json.as[LocalDate](RestFormats.localDateRead))
        exception.errors.head._2 shouldBe List(JsonValidationError(List("Cannot parse 20000-001-001 as a LocalDate")))
      }

      "the provided JSON date was not a String" in {
        val json = JsNumber(1)
        val exception = intercept[JsResultException](json.as[LocalDate](RestFormats.localDateRead))
        exception.errors.head._2 shouldBe List(JsonValidationError(List("Expected value to be a string, was actually 1")))
      }
    }
  }

  "The LocalDate writes" should {

    "write a LocalDate object to a JSON String in the expected format" in {
      Json.toJson(LocalDate.parse("2000-01-01"))(RestFormats.localDateWrite) shouldBe JsString("2000-01-01")
    }
  }
}
