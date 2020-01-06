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

package stubs

import java.time.LocalDate

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status.OK
import play.api.libs.json.{JsObject, Json}
import stubs.VatSubscriptionStub.stubGet

object VatObligationsStub {

  def vatObligationsSuccessJson(endDate: LocalDate = LocalDate.now().minusDays(1)): JsObject = Json.obj(
    "obligations" -> Json.arr(
      Json.obj(
        "start" -> LocalDate.now(),
        "end" -> endDate,
        "due" -> LocalDate.now(),
        "periodKey" -> "18AA"
      ),
      Json.obj(
        "start" -> LocalDate.now(),
        "end" -> LocalDate.now(),
        "due" -> LocalDate.now(),
        "periodKey" -> "17AA"
      )
    )
  )

  def stubResponse(status: Int, body: JsObject): StubMapping = {
    stubGet("/vat-obligations/999999999/obligations?status=O", Json.stringify(body), OK)
  }
}
