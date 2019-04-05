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

import java.time.LocalDate

import base.BaseSpec
import models.errors.UnexpectedJsonFormat
import models.{VatObligation, VatObligations}
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse

class VatObligationsHttpParserSpec extends BaseSpec {

  "Calling .read" when {

    "response is 200" when {

      "body of response is valid" should {

        "return a sequence of obligations" in {

          val validJson = Json.obj(
            "obligations" -> Json.arr(Json.obj(
              "start" -> "2017-01-01",
              "end" -> "2017-03-30",
              "due" -> "2017-04-30",
              "periodKey" -> "#001"
            ))

          )
          val httpResponse = HttpResponse(OK, Some(validJson))
          val result = VatObligationsHttpParser.VatObligationsReads.read("", "", httpResponse)
          val expectedResponse = Right(VatObligations(Seq(
            VatObligation(
              LocalDate.parse("2017-01-01"),
              LocalDate.parse("2017-03-30"),
              LocalDate.parse("2017-04-30"),
              periodKey = "#001"
            )
          )))

          result shouldBe expectedResponse
        }
      }

      "body of response is invalid" should {

        "return UnexpectedJsonFormat" in {
          val validJson = Json.obj(
            "obligations" -> Json.arr(Json.obj(
              "start" -> "2017-01-01",
              "end" -> "2017-03-30",
              "due" -> "2017-04-30",
              "periodkey" -> "#001"
            ))

          )
          val httpResponse = HttpResponse(OK, Some(validJson))
          val result = VatObligationsHttpParser.VatObligationsReads.read("", "", httpResponse)
          val expectedResponse = Left(UnexpectedJsonFormat)

          result shouldBe expectedResponse
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
