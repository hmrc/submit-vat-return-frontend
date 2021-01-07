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

package views

import java.time.LocalDate
import assets.messages.SubmitFormPageMessages._
import forms.SubmitVatReturnForm
import models.VatObligation
import models.nrs.CY
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Lang
import views.html.SubmitForm

class SubmitFormViewSpec extends ViewBaseSpec {

  val submitFormView: SubmitForm = inject[SubmitForm]

  object Selectors {
    val boxes = List(
      "#box-one", "#box-two", "#box-three",
      "#box-four", "#box-five", "#box-six",
      "#box-seven", "#box-eight", "#box-nine"
    )
    val backLink = "#content > article > a"
    val returnTotalHeading = "#content > article > section > section:nth-child(6) > div > h3"
    val returnDueDate = "#content > article > section > section:nth-child(6) > div > p"
    val changeReturnLink = "#content > article > section > section:nth-child(6) > div > a"
    val submitVatReturnHeading = "#content > article > section > h3.bold-medium"
    val submitReturnInformation = "#content > article > section > p"
    val submitButton = "#content > article > section > form > button"
  }

  def boxElement(box: String, column: Int): String = {
    s"$box > div:nth-child($column)"
  }

  val periodKey = "18AA"

  val obligation: VatObligation =
    VatObligation(LocalDate.parse("2019-01-12"), LocalDate.parse("2019-04-12"), LocalDate.parse("2019-05-12"), "18AA")

  "Rendering the submit_form page" when {

    "the user is on the flat rate scheme" should {

      lazy val view = submitFormView(
        periodKey,
        Some("ABC Studios"),
        flatRateScheme = true,
        obligation,
        SubmitVatReturnForm().nineBoxForm,
        isAgent = false,
        nIProtocolEnabled = false
      )(messages, mockAppConfig, user)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct hidden label for box 1" in {
        elementText("label[for=box1]") shouldBe "Box 1 VAT you charged on sales and other supplies amount"
      }

      "have the correct page title" in {
        document.title shouldBe "Your VAT Return - Business tax account - GOV.UK"
      }

      "have the correct title" in {
        elementText("h1 > span:nth-of-type(1)") shouldBe submitReturn
        elementText("h1 > span:nth-of-type(2)") shouldBe "12 Jan to 12 Apr 2019"
        elementText("h1 > span:nth-of-type(3)") shouldBe returnDue("12 May 2019")
      }

      s"the back link is displayed with the correct href" in {
        elementText(Selectors.backLink) shouldBe back
        element(Selectors.backLink).attr("href") shouldBe
          controllers.routes.HonestyDeclarationController.show(periodKey).url
      }

      "display the business name" in {
        elementText("h2") shouldBe "ABC Studios"
      }

      "state 'VAT details'" in {
        elementText("h3:nth-of-type(1)") shouldBe vatDetails
      }

      "have the correct box numbers in the table" in {
        val expectedBoxes = Array(
          box1,
          box2,
          box3,
          box4,
          box5,
          box6,
          box7,
          box8,
          box9
        )
        expectedBoxes.indices.foreach(i => elementText(boxElement(Selectors.boxes(i), 1)) shouldBe expectedBoxes(i))
      }

      "have the correct row descriptions in the table" in {
        val expectedDescriptions = Array(
          box1Text,
          box2Text,
          box3Text,
          box4Text,
          box5Text,
          box6FlatRateSchemeText,
          box7Text,
          box8Text,
          box9Text
        )
        expectedDescriptions.indices.foreach(i => elementText(boxElement(Selectors.boxes(i), 2)) shouldBe expectedDescriptions(i))
      }

      "state 'Additional information'" in {
        document.getElementById("additionalInfo").text shouldBe additionalInformation
      }

      "state that you can submit your return on the next screen" in {
        elementText("#content article p") shouldBe nextScreen
      }

      "have the continue button" in {
        document.getElementById("continue").attr("value") shouldBe continue
      }
    }

    "the user is not on the flat rate scheme" should {
      lazy val view = submitFormView(
        "18AA",
        Some("ABC Studios"),
        flatRateScheme = false,
        obligation,
        SubmitVatReturnForm().nineBoxForm,
        isAgent = false,
        nIProtocolEnabled = false
      )(messages, mockAppConfig, user)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "display the non flat rate scheme text ofr box 6" in {
        elementText("#box-six > div:nth-of-type(2)") shouldBe box6NonFlatRateSchemeText
      }
    }

    "rendering the submit_form page in welsh" should {
      val languageOption: Lang = Lang.apply(CY.languageCode)

      "have the language set as welsh" in {
        languageOption.language shouldBe "cy"
      }

      lazy val view = submitFormView(
        "18AA",
        Some("ABC Studios"),
        flatRateScheme = true,
        obligation,
        SubmitVatReturnForm().nineBoxForm,
        isAgent = false,
        nIProtocolEnabled = false
      )(welshMessages, mockAppConfig, user)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the welsh messages file" in {
        welshMessages.lang.toString shouldBe "Lang(cy)"
      }

      "have the correct hidden label for box 1" in {
        elementText("label[for=box1]") shouldBe "Swm Blwch 1 TAW a godwyd gennych ar werthiannau a chyflenwadau eraill"
      }
    }

    "page has errors" should {

      lazy val view = submitFormView(
        periodKey,
        Some("ABC Studios"),
        flatRateScheme = true,
        obligation,
        SubmitVatReturnForm().nineBoxForm.bind(Map("" -> "")),
        isAgent = false,
        nIProtocolEnabled = false
      )(messages, mockAppConfig, user)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      s"have the correct page title" in {
        document.title shouldBe "Error: Your VAT Return - Business tax account - GOV.UK"
      }

      "display an error summary" in {
        elementText("#error-summary-display h2") shouldBe "There is a problem"
      }
    }

    "the NI protocol feature switch is on" should {

      lazy val view = submitFormView(
        periodKey,
        Some("ABC Studios"),
        flatRateScheme = true,
        obligation,
        SubmitVatReturnForm().nineBoxForm,
        isAgent = false,
        nIProtocolEnabled = true
      )(messages, mockAppConfig, user)

      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct row descriptions in the table" in {

        val expectedDescriptions = Array(
          box1Text,
          box2TextNIProtocol,
          box3Text,
          box4Text,
          box5Text,
          box6FlatRateSchemeText,
          box7Text,
          box8TextNIProtocol,
          box9TextNIProtocol
        )
        expectedDescriptions.indices.foreach(i => elementText(boxElement(Selectors.boxes(i), 2)) shouldBe expectedDescriptions(i))
      }
    }
  }
}
