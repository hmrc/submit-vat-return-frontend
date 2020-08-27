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

import base.BaseSpec
import connectors.httpParsers.NrsSubmissionHttpParser.NrsSubmissionReads
import models.errors.{BadRequestError, ServerSideError}
import models.nrs.SuccessModel
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HttpResponse

class NrsSubmissionHttpParserSpec extends BaseSpec {

  val correctResponseJson: JsObject = Json.obj(
    "nrSubmissionId" -> "2dd537bc-4244-4ebf-bac9-96321be13cdc"
  )

  val incorrectResponseJson: JsObject = Json.obj(
    "nope" -> "nope"
  )

  val badRequestResponseJson: JsObject = Json.obj(
    "reason" -> "you sent us dodgy data"
  )
  
  "NrsSubmissionReads" when {

    "response is 202" when {

      "response body is in expected JSON format" should {

        val httpResponse = HttpResponse(ACCEPTED, correctResponseJson, Map.empty[String,Seq[String]])
        val expectedResult = SuccessModel("2dd537bc-4244-4ebf-bac9-96321be13cdc")

        val result = NrsSubmissionReads.read("", "", httpResponse)

        "return a success model" in {
          result shouldBe Right(expectedResult)
        }
      }

      "response body is not in expected JSON format" should {

        val httpResponse = HttpResponse(ACCEPTED, incorrectResponseJson, Map.empty[String,Seq[String]])

        val result = NrsSubmissionReads.read("", "", httpResponse)

        "return an UnexpectedJsonFormat model" in {
          result shouldBe Right(SuccessModel(""))
        }
      }
    }

    "response is 400" should {

      val httpResponse = HttpResponse(BAD_REQUEST, badRequestResponseJson, Map.empty[String,Seq[String]])
      val expectedResult = BadRequestError("400", "Bad Request response when submitting to NRS.")

      val result = NrsSubmissionReads.read("", "", httpResponse)

      "return a BadRequestError" in {
        result shouldBe Left(expectedResult)
      }
    }
    
    "response is any other status" should {

      val httpResponse = HttpResponse(SERVICE_UNAVAILABLE, "", Map.empty[String,Seq[String]])
      val expectedResult = ServerSideError(SERVICE_UNAVAILABLE.toString, "Received downstream error when submitting to NRS")

      val result = NrsSubmissionReads.read("", "", httpResponse)

      "return a ServerSideError model" in {
        result shouldBe Left(expectedResult)
      }
    }
  }
}
