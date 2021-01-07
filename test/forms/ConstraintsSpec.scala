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

package forms

import base.BaseSpec
import play.api.data.validation.{Invalid, Valid}
import forms.Constraints._

class ConstraintsSpec extends BaseSpec {

  ".twoDecimalPlaces" when {

    "value is 1000.01" should {

      val value = BigDecimal(1000.01)

      "return Valid" in {
        twoDecimalPlaces("error")(value) shouldBe Valid
      }
    }

    "value is 1000.001" should {

      val value = BigDecimal(1000.001)

      "return Invalid" in {
        twoDecimalPlaces("error")(value) shouldBe Invalid("error")
      }
    }

    "value is 1000" should {

      val value = BigDecimal(1000)

      "return Valid" in {
        twoDecimalPlaces("error")(value) shouldBe Valid
      }
    }
  }

  ".noDecimalPlaces" when {

    "value is 1000.00" should {

      val value = BigDecimal(1000.00)

      "return Valid" in {
        noDecimalPlaces("error")(value) shouldBe Valid
      }
    }

    "value is 1000.01" should {

      val value = BigDecimal(1000.01)

      "return Invalid" in {
        noDecimalPlaces("error")(value) shouldBe Invalid("error")
      }
    }

    "value is 1000" should {

      val value = BigDecimal(1000)

      "return Valid" in {
        noDecimalPlaces("error")(value) shouldBe Valid
      }
    }
  }

  ".max" when {

    "value is higher than max" should {

      val value = BigDecimal(1.02)

      "return Invalid" in {
        max(1.01, "error")(value) shouldBe Invalid("error")
      }
    }

    "value is lower than max" should {

      val value = BigDecimal(1.00)

      "return Valid" in {
        max(1.01, "error")(value) shouldBe Valid
      }
    }

    "value is same as max" should {

      val value = BigDecimal(1.01)

      "return Valid" in {
        max(1.01, "error")(value) shouldBe Valid
      }
    }
  }

  ".min" when {

    "value is higher than min" should {

      val value = BigDecimal(1.02)

      "return Valid" in {
        min(1.01, "error")(value) shouldBe Valid
      }
    }

    "value is lower than min" should {

      val value = BigDecimal(1.00)

      "return Valid" in {
        min(1.01, "error")(value) shouldBe Invalid("error")
      }
    }

    "value is same as min" should {

      val value = BigDecimal(1.01)

      "return Valid" in {
        min(1.01, "error")(value) shouldBe Valid
      }
    }
  }

  ".validBigDecimal" when {

    "value is valid" should {

      val value = "1.01"

      "return Valid" in {
        validBigDecimal("empty", "invalid")(value) shouldBe Valid
      }
    }

    "value is not numeric" should {

      val value = "abc"

      "return Invalid" in {
        validBigDecimal("empty", "invalid")(value) shouldBe Invalid("invalid")
      }
    }

    "value is empty" should {

      val value = ""

      "return Invalid" in {
        validBigDecimal("empty", "invalid")(value) shouldBe Invalid("empty")
      }
    }
  }
}
