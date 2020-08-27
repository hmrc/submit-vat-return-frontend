/*
 * Copyright 2020 HM Revenue & Customs
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
import models.errors._
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
          val httpResponse = HttpResponse(OK, validJson, Map.empty[String,Seq[String]])
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
          val httpResponse = HttpResponse(OK, validJson, Map.empty[String,Seq[String]])
          val result = VatObligationsHttpParser.VatObligationsReads.read("", "", httpResponse)
          val expectedResponse = Left(UnexpectedJsonFormat)

          result shouldBe expectedResponse
        }
      }
    }

    "response is 404" should {

      "return an empty sequence" in {
        val emptyReturn = Json.obj()

        val httpResponse = HttpResponse(NOT_FOUND, emptyReturn, Map.empty[String,Seq[String]])
        val result = VatObligationsHttpParser.VatObligationsReads.read("", "", httpResponse)
        val expectedResponse = Right(VatObligations(Seq()))

        result shouldBe expectedResponse
      }
    }

    "response is 400" should {

      "return BadRequestError" in {
        val emptyReturn = Json.obj(
          "code" -> "NOOO!",
          "message" -> "summet gon messed up"
        )

        val httpResponse = HttpResponse(BAD_REQUEST, emptyReturn, Map.empty[String,Seq[String]])
        val result = VatObligationsHttpParser.VatObligationsReads.read("", "", httpResponse)
        val expectedResponse = Left(BadRequestError("NOOO!", "summet gon messed up"))

        result shouldBe expectedResponse
      }
    }

    "response is 500" should {

      "return ServerSideError" in {
        val emptyReturn = Json.obj()

        val httpResponse = HttpResponse(INTERNAL_SERVER_ERROR, emptyReturn, Map.empty[String,Seq[String]])
        val result = VatObligationsHttpParser.VatObligationsReads.read("", "", httpResponse)
        val expectedResponse = Left(ServerSideError(s"$INTERNAL_SERVER_ERROR", "{ }"))

        result shouldBe expectedResponse
      }
    }

    "response is unexpected" should {
      "return UnexpectedStatusError" in {
        val emptyReturn = Json.obj()

        val httpResponse = HttpResponse(PROXY_AUTHENTICATION_REQUIRED, emptyReturn, Map.empty[String,Seq[String]])
        val result = VatObligationsHttpParser.VatObligationsReads.read("", "", httpResponse)
        val expectedResponse = Left(UnexpectedStatusError(s"$PROXY_AUTHENTICATION_REQUIRED", "{ }"))

        result shouldBe expectedResponse
      }
    }

    s"response is $BAD_REQUEST and there are multiple errors" should {
      "return MultipleErrors" in {
        val multipleReturn = Json.obj(
          "code" -> s"$BAD_REQUEST",
          "message" -> "My god... What have I done?",
          "errors" -> Json.arr(
            Json.obj(
              "code" -> "this did a thing",
              "message" -> "the thing it did went really wrong in some way or another"
            ),
            Json.obj(
              "code" -> "this is a second error code",
              "message" -> "TWO ERRORS AT THE SAME TIME?! THE WORLD HAS GONE MAD!"
            )
          )
        )
        val expectedBody = Json.arr(
          Json.obj(
            "code" -> "this did a thing",
            "message" -> "the thing it did went really wrong in some way or another"
          ),
          Json.obj(
            "code" -> "this is a second error code",
            "message" -> "TWO ERRORS AT THE SAME TIME?! THE WORLD HAS GONE MAD!"
          )
        )

        val httpResponse = HttpResponse(BAD_REQUEST, multipleReturn, Map.empty[String,Seq[String]])
        val result = VatObligationsHttpParser.VatObligationsReads.read("", "", httpResponse)
        val expectedResponse = Left(MultipleErrors(s"$BAD_REQUEST", Json.stringify(expectedBody)))

        result shouldBe expectedResponse
      }
    }
  }
}
