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

package connectors.httpParsers

import base.BaseSpec
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HttpResponse
import common.MandationStatuses.nonMTDfB
import connectors.httpParsers.MandationStatusHttpParser.MandationStatusReads
import models.MandationStatus
import models.errors._
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND, OK}

class MandationStatusHttpParserSpec extends BaseSpec {

  val validJson: JsObject = Json.obj("mandationStatus" -> nonMTDfB)
  val invalidJson: JsObject = Json.obj("invalid" -> "json")

  "MandationStatusHttpReads" should {

    "successfully parse the HTTP response" when {

      "valid JSON is returned" in {

        val httpResponse = HttpResponse(OK, validJson, Map.empty[String,Seq[String]])

        val expectedResult = MandationStatus(nonMTDfB)

        val result = MandationStatusReads.read("", "", httpResponse)

        result shouldBe Right(expectedResult)

      }
    }

    "return an error model" when {

      "invalid JSON is returned" in {

        val httpResponse = HttpResponse(OK, invalidJson, Map.empty[String,Seq[String]])

        val result = MandationStatusReads.read("", "", httpResponse)

        result shouldBe Left(UnexpectedJsonFormat)
      }

      "a 500 (INTERNAL_SERVER_ERROR) response is returned" in {

        val errorJson: JsObject = Json.obj()

        val httpResponse = HttpResponse(INTERNAL_SERVER_ERROR, errorJson, Map.empty[String,Seq[String]])

        val expectedResult = Left(ServerSideError(INTERNAL_SERVER_ERROR.toString, "{ }"))

        val result = MandationStatusReads.read("", "", httpResponse)

        result shouldBe expectedResult
      }

      "a 400 (BAD_REQUEST) response is returned" when {

        "a single error is returned" in {
          val errorJson: JsObject = Json.obj(
            "code" -> s"$BAD_REQUEST",
            "message" -> "bad request"
          )

          val httpResponse = HttpResponse(BAD_REQUEST, errorJson, Map.empty[String,Seq[String]])

          val expectedResult = Left(BadRequestError(BAD_REQUEST.toString, "bad request"))

          val result = MandationStatusReads.read("", "", httpResponse)

          result shouldBe expectedResult
        }

        "multiple errors are returned" in {

          val errorJson: JsObject = Json.obj(
            "code" -> s"$BAD_REQUEST",
            "message" -> "bad request",
            "errors" -> Json.arr(
              Json.obj(
                "code" -> s"$BAD_REQUEST",
                "message" -> "this is a bad request"
              ),
              Json.obj(
                "code" -> s"$BAD_REQUEST",
                "message" -> "this is also a bad request"
              )
            )
          )

          val expectedBody = Json.arr(
            Json.obj(
              "code" -> s"$BAD_REQUEST",
              "message" -> "this is a bad request"
            ),
            Json.obj(
              "code" -> s"$BAD_REQUEST",
              "message" -> "this is also a bad request"
            )
          )

          val httpResponse = HttpResponse(BAD_REQUEST, errorJson, Map.empty[String,Seq[String]])

          val expectedResult = Left(MultipleErrors(BAD_REQUEST.toString, Json.stringify(expectedBody)))

          val result = MandationStatusReads.read("", "", httpResponse)

          result shouldBe expectedResult

        }
      }

      "an unexpected error is returned" in {

        val errorJson: JsObject = Json.obj()

        val httpResponse = HttpResponse(NOT_FOUND, errorJson, Map.empty[String,Seq[String]])

        val expectedResult = Left(UnexpectedStatusError(NOT_FOUND.toString, "{ }"))

        val result = MandationStatusReads.read("", "", httpResponse)

        result shouldBe expectedResult
      }
    }
  }
}
