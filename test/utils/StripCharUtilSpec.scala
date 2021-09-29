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

package utils

import uk.gov.hmrc.play.test.UnitSpec
import utils.StripCharUtil.stripAll

class StripCharUtilSpec extends UnitSpec {

  "return the expected string with no pound sign" in {
    stripAll("£100","£") shouldBe "100"
  }
  "return the expected string if pound signs and commas are included" in {
    stripAll("£100,,,","£ ,") shouldBe "100"
  }
  "return the expected string if pound signs, commas and a full stop is present" in {
    stripAll("£100.56,,,...","£ , .") shouldBe "100.56"
  }
  "return the expected string if a pound sign is included in the middle and full stops and commas are present" in {
    stripAll("£100£.56...,,","£ . ,") shouldBe "100.56"
  }
  "return the expected string when commas are present" in {
    stripAll("£100,000,000","£") shouldBe "100000000"
  }

}
