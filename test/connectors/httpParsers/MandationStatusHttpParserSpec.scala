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

package connectors.httpParsers

import base.BaseSpec
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HttpResponse
import common.MandationStatuses.nonMTDfB
import connectors.httpParsers.MandationStatusHttpParser.MandationStatusReads
import models.MandationStatus
import models.errors._
import play.api.http.Status._

class MandationStatusHttpParserSpec extends BaseSpec {

  val validJson: JsObject = Json.obj("mandationStatus" -> nonMTDfB)
  val invalidJson: JsObject = Json.obj("invalid" -> "json")

  "MandationStatusHttpReads" should {

    "successfully parse the HTTP response" when {

      "valid JSON is returned" in {

        val httpResponse = HttpResponse(OK, validJson, Map.empty[String, Seq[String]])

        val expectedResult = MandationStatus(nonMTDfB)

        val result = MandationStatusReads.read("", "", httpResponse)

        result shouldBe Right(expectedResult)

      }
    }

    "return an error model" when {

      "invalid JSON is returned" in {

        val httpResponse = HttpResponse(OK, invalidJson, Map.empty[String, Seq[String]])

        val result = MandationStatusReads.read("", "", httpResponse)

        result shouldBe Left(UnexpectedJsonError)
      }

      "a 500 (INTERNAL_SERVER_ERROR) response is returned" in {

        val errorJson: JsObject = Json.obj()

        val httpResponse = HttpResponse(INTERNAL_SERVER_ERROR, errorJson, Map.empty[String, Seq[String]])

        val expectedResult = Left(ErrorModel(INTERNAL_SERVER_ERROR, "{ }"))

        val result = MandationStatusReads.read("", "", httpResponse)

        result shouldBe expectedResult
      }

      "an unexpected error is returned" in {

        val errorJson: JsObject = Json.obj()

        val httpResponse = HttpResponse(NOT_FOUND, errorJson, Map.empty[String, Seq[String]])

        val expectedResult = Left(ErrorModel(NOT_FOUND, "{ }"))

        val result = MandationStatusReads.read("", "", httpResponse)

        result shouldBe expectedResult
      }
    }
  }
}
