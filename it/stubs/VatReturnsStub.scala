/*
 * Copyright 2018 HM Revenue & Customs
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

package stubs

import base.BaseISpec
import com.github.tomakehurst.wiremock.client.WireMock.{equalToJson, postRequestedFor, urlEqualTo, verify}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.{JsObject, JsValue, Json}

object VatReturnsStub extends BaseISpec {

  private def uri(vrn: String): String = s"/vat-returns/returns/vrn/$vrn"

  val successResponseJson: JsObject = Json.obj(
    "formBundleNumber" -> "12345"
  )

  val invalidResponseJson: JsObject = Json.obj(
    "blue" -> "green"
  )

  def stubResponse(vrn: String)(status: Int, body: JsObject): StubMapping = {
    stubPost(uri(vrn), Json.stringify(body), status)
  }

  def verifySubmission(vrn: String, body: JsValue): Unit =
    verify(postRequestedFor(urlEqualTo(uri(vrn))).withRequestBody(equalToJson(body.toString())))
}
