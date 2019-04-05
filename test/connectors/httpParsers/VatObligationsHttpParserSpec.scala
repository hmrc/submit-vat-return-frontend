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

class VatObligationsHttpParserSpec extends BaseSpec {

  "Calling .read" when {

    "response is 200" when {

      "body of response is valid" should {

        "return a sequence of obligations" in {

        }
      }

      "body of response is invalid" should {

        "return UnexpectedJsonFormat" in {

        }
      }
    }

    "response is 404" should {

      "return an empty sequence" in {

      }
    }

    "response is 400" should {

      "return BadRequestError" in {

      }
    }

    "response is 500" should {

      "return ServerSideError" in {

      }
    }

    "response is unexpected" should {

      "return UnexpectedStatusError" in {

      }
    }
  }
}
