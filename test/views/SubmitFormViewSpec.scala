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

package views

import java.time.LocalDate

import forms.SubmitVatReturnForm
import models.VatObligation
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import assets.messages.SubmitFormPageMessages._
import views.html.templates.formatters.dates.{displayDate, displayDateRange}

class SubmitFormViewSpec extends ViewBaseSpec {

  "Rendering the submit_form page" should {

    val nbr = new SubmitVatReturnForm()(messagesApi)

    val obligations: Seq[VatObligation] =
      Seq(VatObligation(LocalDate.parse("2019-01-12"), LocalDate.parse("2019-04-12"), LocalDate.parse("2019-05-12"), "18AA"))

    lazy val view = views.html.submit_form("18AA", Some("ABC Studios"), flatRateScheme = true, obligations, nbr.submitVatReturnForm, isAgent = false)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "render breadcrumbs" which {

      "has the 'Your VAT details' title" in {
        elementText("div.breadcrumbs li:nth-of-type(1)") shouldBe "Your VAT details"
      }

      "and links to the VAT Overview page" in {
        element("div.breadcrumbs li:nth-of-type(1) a").attr("href") shouldBe mockAppConfig.vatSummaryUrl
      }

      "has the 'Submit VAT Return' title" in {
        elementText("div.breadcrumbs li:nth-of-type(2)") shouldBe "Submit VAT Return"
      }

      "and links to the Return deadlines page" in {
        element("div.breadcrumbs li:nth-of-type(2) a").attr("href") shouldBe mockAppConfig.returnDeadlinesUrl
      }

      "has the correct current page title" in {
        elementText("div.breadcrumbs li:nth-of-type(3)") shouldBe "Submit 12 January to 12 April 2019 return"
      }
    }

    "have the correct title" in {
      elementText("h1 > span:nth-of-type(1)") shouldBe submitReturn
      elementText("h1 > span:nth-of-type(2)") shouldBe "12 Jan to 12 Apr 2019"
      elementText("h1 > span:nth-of-type(3)") shouldBe returnDue("12 May 2019")
    }

    "display the business name" in {
      elementText("h2") shouldBe "ABC Studios"
    }

    "state 'VAT details'" in {
      elementText("h3:nth-of-type(1)") shouldBe vatDetails
    }

    "have the correct box 1 row" in {
      elementText("#box-one > div:nth-of-type(1)") shouldBe box1
      elementText("#box-one > div:nth-of-type(2)") shouldBe box1Text
    }

    "have the correct box 2 row" in {
      elementText("#box-two > div:nth-of-type(1)") shouldBe box2
      elementText("#box-two > div:nth-of-type(2)") shouldBe box2Text
    }

    "have the correct box 3 row" in {
      elementText("#box-three > div:nth-of-type(1)") shouldBe box3
      elementText("#box-three > div:nth-of-type(2)") shouldBe box3Text
    }

    "have the correct box 4 row" in {
      elementText("#box-four > div:nth-of-type(1)") shouldBe box4
      elementText("#box-four > div:nth-of-type(2)") shouldBe box4Text
    }

    "have the correct box 5 row" in {
      elementText("#box-five > div:nth-of-type(1)") shouldBe box5
      elementText("#box-five > div:nth-of-type(2)") shouldBe box5Text
    }

    "state 'Additional information'" in {
      document.getElementById("additionalInfo").text shouldBe additionalInformation
    }

    "have the correct box 6 row" in {
      elementText("#box-six > div:nth-of-type(1)") shouldBe box6
      elementText("#box-six > div:nth-of-type(2)") shouldBe box6Text
    }

    "have the correct box 7 row" in {
      elementText("#box-seven > div:nth-of-type(1)") shouldBe box7
      elementText("#box-seven > div:nth-of-type(2)") shouldBe box7Text
    }

    "have the correct box 8 row" in {
      elementText("#box-eight > div:nth-of-type(1)") shouldBe box8
      elementText("#box-eight > div:nth-of-type(2)") shouldBe box8Text
    }

    "have the correct box 9 row" in {
      elementText("#box-nine > div:nth-of-type(1)") shouldBe box9
      elementText("#box-nine > div:nth-of-type(2)") shouldBe box9Text
    }

    "state that you can submit your return on the next screen" in {
      elementText("#content p") shouldBe nextScreen
    }

    "have the continue button" in {
      document.getElementById("continue").attr("value") shouldBe continue
    }
  }
}
