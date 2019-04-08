/*
 * Copyright 2019 HM Revenue & Customs
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

package models

import base.BaseSpec
import play.api.libs.json.{JsValue, Json}

class CustomerDetailsSpec extends BaseSpec {

  def asJson(
              firstName: Option[String],
              lastName: Option[String],
              tradingName: Option[String],
              organisationName: Option[String],
              hasFlatRateScheme: Boolean = false
            ): JsValue = {
    firstName.fold(Json.obj())(firstNameResult => Json.obj("firstName" -> firstNameResult)) ++
      lastName.fold(Json.obj())(lastNameResult => Json.obj("lastName" -> lastNameResult)) ++
      tradingName.fold(Json.obj())(tradingNameResult => Json.obj("tradingName" -> tradingNameResult)) ++
      organisationName.fold(Json.obj())(organisationNameResult => Json.obj("organisationName" -> organisationNameResult)) ++
      Json.obj("hasFlatRateScheme" -> hasFlatRateScheme)
  }

  private def optionalFirstName: Seq[Option[String]] = Seq(None, Some("Shagura"))
  private def optionalSecondName: Seq[Option[String]] = Seq(None, Some("Magala"))
  private def optionalTradingName: Seq[Option[String]] = Seq(None, Some("Gore Magala"))
  private def optionalOrganisationName: Seq[Option[String]] = Seq(None, Some("Fatalis"))
  private def flatRateSchemeOptions: Seq[Boolean] = Seq(true, false)

  "CustomerDetails" should {
    "correctly parse" when {
      (for {
        firstName <- optionalFirstName
        lastName <- optionalSecondName
        tradingName <- optionalTradingName
        orgName <- optionalOrganisationName
        flatRateScheme <- flatRateSchemeOptions
      } yield (firstName, lastName, tradingName, orgName, flatRateScheme)).foreach {
        case (firstName, lastName, tradingName, orgName, flatRateScheme) =>
          s"provided with Json containing $firstName, $lastName, $tradingName, $orgName, $flatRateScheme" in {
            val expectedResult = CustomerDetails(firstName, lastName, tradingName, orgName, flatRateScheme)
            asJson(firstName, lastName, tradingName, orgName, flatRateScheme).as[CustomerDetails] shouldBe expectedResult
          }

          s"provided with a model containing $firstName, $lastName, $tradingName, $orgName, $flatRateScheme" in {
            val expectedResult = asJson(firstName, lastName, tradingName, orgName, flatRateScheme)
            Json.toJson(CustomerDetails(firstName, lastName, tradingName, orgName, flatRateScheme)) shouldBe expectedResult
          }
      }
    }
  }
}
