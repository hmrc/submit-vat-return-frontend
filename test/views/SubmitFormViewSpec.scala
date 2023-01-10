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

package views

import java.time.LocalDate
import assets.messages.SubmitFormPageMessages._
import forms.SubmitVatReturnForm
import models.VatObligation
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.SubmitForm

class SubmitFormViewSpec extends ViewBaseSpec {

  val submitFormView: SubmitForm = inject[SubmitForm]

  object Selectors {
    val backLink = ".govuk-back-link"
    val heading = "h1"
    val entityName = "h2"
    val vatDetails = "h3:nth-of-type(1)"
    val additionalDetails = "#additionalInfo"
    val returnTotalHeading = "#content > article > section > section:nth-child(6) > div > h3"
    val returnDueDate = "p.govuk-body:nth-child(4)"
    val wholePounds = "#whole-pounds"
    val nextScreenSubmit = "#next-screen-submit"
    val submitButton = ".govuk-button"
    val form = "#content > form"
    val errorHeading = ".govuk-error-summary__title"
    def errorSummaryMessage(listItem: Int): String = s".govuk-error-summary__list > li:nth-child($listItem) > a"
    def boxLabel(boxNum: Int): String = s"div:nth-of-type($boxNum) > label"
    def boxCalculatedValue(boxNum: Int): String = s"div:nth-of-type($boxNum) > div > p"
    def boxDescription(boxNum: Int): String = s"#box$boxNum-hint"
    def boxInput(boxNum: Int): String = s"#box$boxNum"
    def boxErrorMessage(boxNum: Int): String = s"#box$boxNum-error"
  }

  val periodKey = "18AA"

  val obligation: VatObligation =
    VatObligation(LocalDate.parse("2019-01-12"), LocalDate.parse("2019-04-12"), LocalDate.parse("2019-05-12"), periodKey)

  val entityName = "ABC Studios"
  val inputBoxes: Seq[Int] = Seq(1, 2, 4, 6, 7, 8, 9)

  "The submit form page" when {

    "the user is on the flat rate scheme" should {

      lazy val view = submitFormView(
        periodKey,
        Some(entityName),
        flatRateScheme = true,
        obligation,
        SubmitVatReturnForm().nineBoxForm,
        isAgent = false,
      )(messages, mockAppConfig, user)

      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct title" in {
        document.title shouldBe title
      }

      "have the correct page heading" in {
        elementText(Selectors.heading) shouldBe heading
      }

      "have the entity name as a subheading" in {
        elementText(Selectors.entityName) shouldBe entityName
      }

      "have the correct subheading for the first five boxes" in {
        elementText(Selectors.vatDetails) shouldBe vatDetails
      }

      "have the return due date" in {
        elementText(Selectors.returnDueDate) shouldBe returnDue("12 May 2019")
      }

      "have the correct subheading for the last four boxes" in {
        elementText(Selectors.additionalDetails) shouldBe additionalInformation
      }

      "have an advice paragraph to only enter whole pounds for the last four boxes" in {
        elementText(Selectors.wholePounds) shouldBe wholePounds
      }

      "have the correct box labels" in {

        val expectedLabels = Seq(
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
        (1 to 9).foreach(i => elementText(Selectors.boxLabel(i)) shouldBe expectedLabels(i - 1))
      }

      "have the correct box descriptions" in {

        val expectedDescriptions = Seq(
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
        (1 to 9).foreach(i => elementText(Selectors.boxDescription(i)) shouldBe expectedDescriptions(i - 1))
      }

      "have input fields for boxes 1, 2, 4, 6, 7, 8 and 9" in {
        inputBoxes.foreach(i => element(Selectors.boxInput(i)))
      }

      "have set text instead of an input field for boxes 3 and 5" in {
        val autoCalcBoxes: Seq[Int] = Seq(3, 5)
        autoCalcBoxes.foreach(i => elementText(Selectors.boxCalculatedValue(i)) shouldBe calculatedValue)
      }

      "have a final paragraph to tell the user they can submit on the next screen" in {
        elementText(Selectors.nextScreenSubmit) shouldBe nextScreen
      }

      "have a button with the correct text" in {
        elementText(Selectors.submitButton) shouldBe continue
      }

      "have a form with the correct action" in {
        element(Selectors.form).attr("action") shouldBe controllers.routes.SubmitFormController.submit(periodKey).url
      }
    }

    "the user is not on the flat rate scheme" should {

      lazy val view = submitFormView(
        periodKey,
        Some(entityName),
        flatRateScheme = false,
        obligation,
        SubmitVatReturnForm().nineBoxForm,
        isAgent = false,
      )(messages, mockAppConfig, user)

      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct box description for box 6" in {
        elementText(Selectors.boxDescription(6)) shouldBe box6NonFlatRateSchemeText
      }
    }

    "there are errors in the form" should {

      lazy val view = submitFormView(
        periodKey,
        Some(entityName),
        flatRateScheme = true,
        obligation,
        SubmitVatReturnForm().nineBoxForm.bind(Map(
          "box1" -> "", "box2" -> "", "box4" -> "", "box6" -> "", "box7" -> "", "box8" -> "", "box9" -> ""
        )),
        isAgent = false,
      )(messages, mockAppConfig, user)

      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct title" in {
        document.title shouldBe s"$errorPrefix $title"
      }

      "have an error summary" which {

        "has the correct heading" in {
          elementText(Selectors.errorHeading) shouldBe errorHeading
        }

        "has the correct messages" in {
          val expectedErrorMessages: Seq[String] = inputBoxes.map(i => s"$enterNumberError box $i")
          (1 to 6).foreach(i => elementText(Selectors.errorSummaryMessage(i)) shouldBe expectedErrorMessages(i - 1))
        }

        "has the correct links to the input boxes" in {
          val expectedHrefs: Seq[String] = inputBoxes.map(i => s"#box$i")
          (1 to 6).foreach(i => element(Selectors.errorSummaryMessage(i)).attr("href") shouldBe expectedHrefs(i - 1))
        }
      }

      "have the correct error messages above each input box" in {
        inputBoxes.foreach(i => elementText(Selectors.boxErrorMessage(i)) shouldBe s"$errorPrefix $enterNumberError box $i")
      }
    }

    "the user is an agent" should {

      lazy val view = submitFormView(
        periodKey,
        Some(entityName),
        flatRateScheme = true,
        obligation,
        SubmitVatReturnForm().nineBoxForm,
        isAgent = false,
      )(messages, mockAppConfig, agentUser)

      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct title" in {
        document.title shouldBe agentTitle
      }
    }
  }
}
