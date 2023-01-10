/*
 * Copyright 2023 HM Revenue & Customs
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
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.{equalToJson, postRequestedFor, urlEqualTo, verify, matching}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.{JsObject, JsValue, Json}

object VatReturnsStub extends BaseISpec {

  def vatReturnUri(vrn: String): String = s"/vat-returns/returns/vrn/$vrn"
  val nrsSubmissionUri: String = "/vat-returns/nrs/submission/" + vrn

  def stubResponse(uri: String)(status: Int, body: JsObject): StubMapping = {
    stubPost(uri, Json.stringify(body), status)
  }

  def verifyVatReturnSubmission(vrn: String, body: JsValue): Unit =
    verify(postRequestedFor(urlEqualTo(vatReturnUri(vrn)))
      .withRequestBody(equalToJson(body.toString()))
      .withHeader("OriginatorID", equalTo("VATUI"))
    )

  def verifyNrsSubmission(body: JsValue): Unit =
    verify(postRequestedFor(urlEqualTo(nrsSubmissionUri))
      .withRequestBody(equalToJson(body.toString()))
    )

  def nrsRegexMatcher(body: String): Unit = {
    verify(postRequestedFor(urlEqualTo(nrsSubmissionUri))
      .withRequestBody(matching(body)))
  }
}
