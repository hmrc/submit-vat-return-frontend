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

package models.errors

import base.BaseSpec
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}

class ErrorModelSpec extends BaseSpec {

  "The error model" should {

    val errorModel = ErrorModel(BAD_REQUEST,"Something went wrong")
    val errorJson: JsValue = Json.obj("status" -> BAD_REQUEST, "message" -> "Something went wrong")

    "Serialize to Json as expected" in {
      Json.toJson(errorModel) shouldBe errorJson
    }

    "Deserialize to the error model as expected" in {
      errorJson.as[ErrorModel] shouldBe errorModel
    }
  }

  "Unexpected json error" should {

    "have INTERNAL SERVER ERROR status" in {
      UnexpectedJsonError.status shouldBe INTERNAL_SERVER_ERROR
    }

    "have the correct error message 'The server you are connecting to returned unexpected JSON.'" in {
      UnexpectedJsonError.message shouldBe "The server you are connecting to returned unexpected JSON."
    }
  }

}
