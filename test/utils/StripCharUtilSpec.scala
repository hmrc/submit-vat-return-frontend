/*
 * Copyright 2023 HM Revenue & Customs
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

package utils

import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import utils.StripCharUtil.stripAll

class StripCharUtilSpec extends AnyWordSpecLike with Matchers with OptionValues {

  "The stripAll function" should {

    "return the expected string with no pound sign" in {
      stripAll("£100") shouldBe "100"
    }
    "return the expected string if a pound sign is present at the start and commas exist" in {
      stripAll("£100,000,000") shouldBe "100000000"
    }
    "return the expected string if a pound sign is present at the start and a full stop exists at the end" in {
      stripAll("£100.56.") shouldBe "100.56"
    }
    "return the expected the expected string with leading whitespaces" in {
      stripAll("   £100") shouldBe "100"
    }
    "return the expected the expected string with trailing white spaces" in {
      stripAll("£100   ") shouldBe "100"
    }
    "return the expected string if whitespaces, commas, pound signs and a full stop exist" in {
      stripAll("     £100,000,000.    ") shouldBe "100000000"
    }
    "return expected string as normal if no characters or whitespaces are present" in {
      stripAll("100.56") shouldBe "100.56"
    }

  }

}


