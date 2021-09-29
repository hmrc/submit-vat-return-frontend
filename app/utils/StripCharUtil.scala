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

object StripCharUtil {

  def stripAll(text: String, charactersToRemove: String): String = {
    // scalastyle:off
      @scala.annotation.tailrec def start(n: Int): String =
        if (n == text.length) ""
        else if (charactersToRemove.indexOf(text.charAt(n)) < 0) end(n, text.length)
        else start(1 + n)

      @scala.annotation.tailrec def end(a: Int, n: Int): String =
        if (n <= a) text.substring(a, n)
        else if (charactersToRemove.indexOf(text.charAt(n - 1)) < 0) text.substring(a, n)
        else end(a, n - 1)

      val result = start(0)

    result.replaceAll(",","")
      .replaceAll("£","").trim
  }





}
