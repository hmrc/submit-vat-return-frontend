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
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status.OK
import play.api.libs.json.{JsObject, Json}

object VatSubscriptionStub extends BaseISpec {

  val vatSubscriptionSuccessJson: JsObject = Json.obj(
    "firstName" -> "Rath",
    "lastName" -> "Alos",
    "tradingName" -> "Blue Rathalos",
    "organisationName" -> "Silver Rathalos",
    "hasFlatRateScheme" -> true
  )

  val vatSubscriptionInvalidJson: JsObject = Json.obj(
    "monster" -> "Rathalos",
    "bestWeapon" -> "Swaxe"
  )

  def stubResponse(status: Int, body: JsObject): StubMapping = {
    stubGet("/vat-subscription/999999999/customer-details", Json.stringify(body), OK)
  }
}
