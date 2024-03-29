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

package models

import java.time.LocalDate

import assets.CustomerDetailsTestAssets._
import base.BaseSpec
import mocks.service.MockDateService
import play.api.libs.json.Json

class CustomerDetailsSpec extends BaseSpec with MockDateService {

  val exemptInsolvencyTypes: Seq[String] = customerDetailsModel.exemptInsolvencyTypes
  val blockedInsolvencyTypes: Seq[String] = customerDetailsModel.blockedInsolvencyTypes

  "CustomerDetails" should {

    "correctly read from JSON" when {

      "all fields are present" in {
        customerDetailsJson.as[CustomerDetails] shouldBe customerDetailsModel
      }

      "the minimum number of fields are present" in {
        customerDetailsJsonMin.as[CustomerDetails] shouldBe customerDetailsModelMin
      }
    }

    "correctly write to JSON" when {

      "all fields are present" in {
        Json.toJson(customerDetailsModel) shouldBe customerDetailsJson
      }

      "the minimum number of fields are present" in {
        Json.toJson(customerDetailsModelMin) shouldBe customerDetailsJsonMin
      }
    }
  }

  "calling .isInsolventWithoutAccess" when {

    "the user is insolvent and has an exempt insolvency type" should {

      "return false" in {
        exemptInsolvencyTypes.foreach { value =>
          customerDetailsInsolvent.copy(insolvencyType = Some(value)).isInsolventWithoutAccess shouldBe false
        }
      }
    }

    "the user is insolvent and has a blocked insolvency type" should {

      "return true" in {
        blockedInsolvencyTypes.foreach { value =>
          customerDetailsInsolvent.copy(insolvencyType = Some(value)).isInsolventWithoutAccess shouldBe true
        }
      }
    }

    "the user is insolvent and has an insolvency type with no associated rules" when {

      "the user is continuing to trade" should {

        "return false" in {
          customerDetailsInsolvent.copy(continueToTrade = Some(true)).isInsolventWithoutAccess shouldBe false
        }
      }

      "the user is not continuing to trade" should {

        "return true" in {
          customerDetailsInsolvent.isInsolventWithoutAccess shouldBe true
        }
      }
    }

    "the user is not insolvent" should {

      "return false" in {
        customerDetailsModel.isInsolventWithoutAccess shouldBe false
      }
    }
  }


  "calling .insolvencyDateFutureUserBlocked" should {

    val currentDate = LocalDate.parse("2018-01-01")

    "return true when the user is insolvent, continuing to trade, a non-exempt insolvency type, insolvency date in the future" in {
      mockCurrentDate(currentDate)
      customerDetailsInsolvencyModel.insolvencyDateFutureUserBlocked(currentDate) shouldBe true
    }

    "return false when the user is of an exempt insolvency type, regardless of other flags" in {
      exemptInsolvencyTypes.foreach { value =>
        mockCurrentDate(currentDate)
        customerDetailsInsolvencyModel.copy(insolvencyType = Some(value)).insolvencyDateFutureUserBlocked(currentDate) shouldBe false
      }
    }

    "return false when the insolvencyDate is in the past" in {
      mockCurrentDate(currentDate)
      customerDetailsInsolvencyModel.copy(insolvencyDate = Some("2017-12-31")).insolvencyDateFutureUserBlocked(currentDate) shouldBe false
    }

    "return false when the user has no insolvency criteria" in {
      mockCurrentDate(currentDate)
      customerDetailsModelMin.insolvencyDateFutureUserBlocked(currentDate) shouldBe false
    }
  }
}
