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
import models.errors.{ServerSideError, UnexpectedJsonFormat}
import models.vatReturnSubmission.SubmissionSuccessModel
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HttpResponse

class SubmitVatReturnHttpParserSpec extends BaseSpec {

  val submitVatReturnReads = SubmitVatReturnHttpParser("123456789", "18AA").SubmitVatReturnReads

  val correctResponseJson: JsObject = Json.obj(
    "formBundleNumber" -> "12345"
  )

  val incorrectResponseJson: JsObject = Json.obj(
    "nope" -> "nope"
  )

  val errorResponseJson: JsObject = Json.obj(
    "code" -> "SERVICE_UNAVAILABLE",
    "reason" -> "oh no"
  )

  "CustomerDetailsReads" when {

    "response is 200" when {

      "response body is in expected JSON format" should {

        val httpResponse = HttpResponse(
          OK,
          Some(correctResponseJson)
        )

        val expectedResult = SubmissionSuccessModel(
          "12345"
        )

        val result = submitVatReturnReads.read("", "", httpResponse)

        "return a success model" in {
          result shouldBe Right(expectedResult)
        }
      }

      "response body is not in expected JSON format" should {

        val httpResponse = HttpResponse(
          OK,
          Some(incorrectResponseJson)
        )

        val expectedResult = UnexpectedJsonFormat

        val result = submitVatReturnReads.read("", "", httpResponse)

        "return an UnexpectedJsonFormat model" in {
          result shouldBe Left(expectedResult)
        }
      }
    }

    "response is not 200" should {

      val httpResponse = HttpResponse(
        SERVICE_UNAVAILABLE,
        Some(errorResponseJson)
      )

      val expectedResult = ServerSideError(SERVICE_UNAVAILABLE.toString, "Received downstream error when submitting VAT return.")

      val result = submitVatReturnReads.read("", "", httpResponse)

      "return a ServerSideError model" in {
        result shouldBe Left(expectedResult)
      }
    }
  }
}
