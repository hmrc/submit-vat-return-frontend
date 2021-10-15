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

import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class HashUtilSpec extends AnyWordSpecLike with Matchers with OptionValues {

  "Calling .encode" should {

    val decodedValue = "hello"
    val encodedValue = "aGVsbG8="

    "return an encoded string version of input" in {
      HashUtil.encode(decodedValue) shouldBe encodedValue
    }
  }

  "Calling .getChecksum" should {

    val value = "hello"
    val checksum = "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824"

    "return the hash of the input" in {
      HashUtil.getHash(value) shouldBe checksum
    }
  }
}
