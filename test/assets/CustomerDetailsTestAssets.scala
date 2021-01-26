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

package assets

import models.CustomerDetails
import play.api.libs.json.{JsObject, Json}

object CustomerDetailsTestAssets {

  val customerDetailsModel: CustomerDetails = CustomerDetails(
    firstName =  Some("Test"),
    lastName =  Some("User"),
    tradingName =  Some("ABC Solutions"),
    organisationName =  Some("ABCL"),
    isInsolvent = false,
    continueToTrade = Some(true),
    insolvencyType = Some("03"),
    insolvencyDate = Some("2018-01-01")
  )

  val customerDetailsModelMin: CustomerDetails = CustomerDetails(
    None, None, None, None, hasFlatRateScheme = false, isInsolvent = false, None, None, None
  )

  val customerDetailsWithFRS: CustomerDetails = CustomerDetails(
    firstName = Some("Test"),
    lastName = Some("User"),
    tradingName = Some("ABC Solutions"),
    organisationName = Some("ABCL"),
    hasFlatRateScheme = true,
    isInsolvent = false,
    None,
    insolvencyType = None,
    insolvencyDate = None
  )

  val customerDetailsInsolvencyModel: CustomerDetails = CustomerDetails(
    firstName =  Some("Test"),
    lastName =  Some("User"),
    tradingName =  Some("ABC Solutions"),
    organisationName =  Some("ABCL"),
    isInsolvent = true,
    continueToTrade = Some(true),
    insolvencyType = Some("03"),
    insolvencyDate = Some("2018-02-01")
  )

  val customerDetailsInsolvent: CustomerDetails = customerDetailsModel.copy(isInsolvent = true, continueToTrade = Some(false))

  val customerDetailsJson: JsObject = Json.obj(
    "firstName" -> "Test",
    "lastName" -> "User",
    "tradingName" -> "ABC Solutions",
    "organisationName" -> "ABCL",
    "hasFlatRateScheme" -> false,
    "isInsolvent" -> false,
    "continueToTrade" -> true,
    "insolvencyType" -> "03",
    "insolvencyDate" -> "2018-01-01"
  )

  val customerDetailsJsonMin: JsObject = Json.obj(
    "hasFlatRateScheme" -> false,
    "isInsolvent" -> false
  )

  val incorrectReturnJson: JsObject = Json.obj(
    "monster" -> "Rathalos",
    "bestWeapon" -> "Swaxe"
  )
}
