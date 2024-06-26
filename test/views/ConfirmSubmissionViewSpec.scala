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

import assets.messages.{ConfirmSubmissionMessages => viewMessages}
import assets.CustomerDetailsTestAssets._
import assets.messages.SubmitFormPageMessages._
import models._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.ConfirmSubmission

class ConfirmSubmissionViewSpec extends ViewBaseSpec {

  val confirmSubmissionView: ConfirmSubmission = inject[ConfirmSubmission]

  object Selectors {
    val boxes: Seq[String] = List(
      "#box-one", "#box-two", "#box-three",
      "#box-four", "#box-five", "#box-six",
      "#box-seven", "#box-eight", "#box-nine"
    )
    val backLink = ".govuk-back-link"
    val returnDueDate = "#content > h1 > span.govuk-caption-m"
    val returnTotalHeading = "#return-total-heading"
    val changeReturnLink = "#change"
    val submitVatReturnHeading = "#content > article > section > h3.bold-medium"
    val submitReturnInformation = "#content > article > section > p"
    val submitButton = "#content > form > button"

    val declarationHeader = "#declaration-heading"
    val nonAgentDeclarationText = "#content > div > strong"
    val agentDeclarationText = "#agent-declaration"
    val noticeImage = "#content > div > span"
    val noticeText = "#content > div > strong > span"

    val warningHeader = "#content > div.govuk-inset-text > p:nth-child(1)"
    val listHeader = "#content > div.govuk-inset-text > p:nth-child(2)"
    val listItem1 = "#content > div.govuk-inset-text > ul > li:nth-child(1)"
    val listItem2 = "#content > div.govuk-inset-text > ul > li:nth-child(2)"
    val bottomText = "#content > div.govuk-inset-text > p:nth-child(4)"
  }

  def boxElement(box: String, column: Int): String = {
    if (column == 1) {
      s"$box > dt:nth-child(1)"
    }
    else {
      s"$box > dd:nth-of-type(${column - 1})"
    }
  }

  val periodKey = "17AA"

  val vatObligation: VatObligation = VatObligation(
    start = LocalDate.parse("2019-01-12"),
    end = LocalDate.parse("2019-04-12"),
    due = LocalDate.parse("2019-05-12"),
    periodKey = "17AA"
  )

  def vatReturnValid(hasFlatRateScheme: Boolean): SubmitVatReturnModel = SubmitVatReturnModel(
    box1 = 8000.01,
    box2 = 8000.02,
    box3 = 16000.03,
    box4 = 4000.01,
    box5 = 12000.02,
    box6 = 4000.06,
    box7 = 3000.07,
    box8 = 4000.08,
    box9 = 4000.09,
    flatRateScheme = hasFlatRateScheme,
    start = LocalDate.parse("2019-01-12"),
    end = LocalDate.parse("2019-04-12"),
    due = LocalDate.parse("2019-05-12")
  )

  def vatReturnBoxInvalid(box1Value: Double, box4Value: Double, hasFlatRateScheme: Boolean): SubmitVatReturnModel = SubmitVatReturnModel(
    box1 = box1Value,
    box2 = 8000.02,
    box3 = 16000.03,
    box4 = box4Value,
    box5 = 12000.02,
    box6 = 4000.06,
    box7 = 3000.07,
    box8 = 4000.08,
    box9 = 4000.09,
    flatRateScheme = hasFlatRateScheme,
    start = LocalDate.parse("2019-01-12"),
    end = LocalDate.parse("2019-04-12"),
    due = LocalDate.parse("2019-05-12")
  )

  "Confirm Submission View" when {

    "user is non-agent and on the flat rate scheme" should {

      "display the correct information" when {

        val viewModel: ConfirmSubmissionViewModel = ConfirmSubmissionViewModel(
          vatReturnValid(true),
          periodKey,
          userName = customerDetailsWithFRS.clientName
        )

        lazy val view = confirmSubmissionView(viewModel, isAgent = false)(
          messages, mockAppConfig, user)
        lazy implicit val document: Document = Jsoup.parse(view.body)
        lazy val viewAsString = document.toString

        s"display the title as ${viewMessages.principalTitle}" in {
          document.title shouldBe viewMessages.principalTitle
        }

        s"display the title as ${viewMessages.principalTitleNBS} with non breaking spaces" in {
          viewAsString contains  viewMessages.principalTitleNBS
        }

        s"display the back link with the correct href" in {
          elementText(Selectors.backLink) shouldBe viewMessages.back
          element(Selectors.backLink).attr("href") shouldBe
            controllers.routes.SubmitFormController.show(periodKey).url
        }

        s"display the heading as ${viewMessages.heading}" in {
          elementText("#content > h1 > span.govuk-caption-xl") shouldBe viewMessages.heading
        }

        "display the obligation period" in {
          elementText("h1") contains "12 Jan to 12 Apr 2019"
        }

        s"display the name as ${viewModel.userName}" in {
          elementText("h2") shouldBe viewModel.userName.getOrElse("")
        }

        s"display the subheading vat detail is ${viewMessages.subHeadingVatDetails}" in {
          elementText("h3") shouldBe viewMessages.subHeadingVatDetails
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

        "have the correct row information in the table" in {
          val expectedInformation = Array(
            "£8,000.01",
            "£8,000.02",
            "£16,000.03",
            "£4,000.01",
            "£12,000.02",
            "£4,000.06",
            "£3,000.07",
            "£4,000.08",
            "£4,000.09"
          )
          expectedInformation.indices.foreach(i => elementText(boxElement(Selectors.boxes(i), 3)) shouldBe expectedInformation(i))
        }

        s"show the return total heading as ${viewMessages.returnTotal} ${viewModel.returnDetail.box5}" in {
          elementText(Selectors.returnTotalHeading) shouldBe s"${viewMessages.returnTotal} £12,000.02"
        }

        "have the change return details link which" should {

          s"have the redirect url to ${controllers.routes.SubmitFormController.show(periodKey).url}" in {
            element(Selectors.changeReturnLink).attr("href") shouldBe
              controllers.routes.SubmitFormController.show(periodKey).url
          }

          s"display the correct content as ${viewMessages.changeReturnLink}" in {
            elementText(Selectors.changeReturnLink) shouldBe viewMessages.changeReturnLink
          }
        }

        "display the declaration header" in {
          elementText(Selectors.declarationHeader) shouldBe viewMessages.declarationHeading
        }

        "display the correct declaration" which {

          "displays the warning notice" which {

            "displays the image" in {
              element(Selectors.noticeImage).hasClass("govuk-warning-text__icon") shouldBe true
            }

            "displays the hidden text" in {
              elementText(Selectors.noticeText) shouldBe viewMessages.warning
            }
          }

          "displays the correct text" in {
            elementText(Selectors.nonAgentDeclarationText) shouldBe viewMessages.warningNonAgentDeclarationText
          }

          "displays the text in bold" in {
            element(Selectors.nonAgentDeclarationText).hasClass("govuk-warning-text__text") shouldBe true
          }
        }

        "display the submit button with the correct button text" in {
          elementText(Selectors.submitButton) shouldBe viewMessages.submitButton
        }

        "have the prevent double click attribute on the submit button" in {
          element(Selectors.submitButton).hasAttr("data-prevent-double-click") shouldBe true
        }

        "not display the warning" in {
          noElementsOf(Selectors.warningHeader)
        }

      }

      "the ratio of box 1 and box 6 is too great" should {

        val viewModel: ConfirmSubmissionViewModel = ConfirmSubmissionViewModel(
          vatReturnBoxInvalid(80000.01, 4000.01, hasFlatRateScheme = true),
          periodKey,
          userName = customerDetailsWithFRS.clientName
        )

        lazy val view = confirmSubmissionView(viewModel, isAgent = false)(
          messages, mockAppConfig, user)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "display the correct warning header" in {
          elementText(Selectors.warningHeader) shouldBe
            viewMessages.WarningMessages.headerSingleWarning(1, 6)
        }

        "display the correct list header" in {
          elementText(Selectors.listHeader) shouldBe
            viewMessages.WarningMessages.listHeading
        }

        "display the correct first item in the list" in {
          elementText(Selectors.listItem1) shouldBe
            viewMessages.WarningMessages.listCommonItem
        }

        "display the correct second item in the list" in {
          elementText(Selectors.listItem2) shouldBe
            viewMessages.WarningMessages.listItemSingleWarning(6)
        }

        "display the correct bottom text" in {
          elementText(Selectors.bottomText) shouldBe viewMessages.WarningMessages.listCommonBottomText
        }
      }

      "the ratio of box 4 and box 7 is too great" should {
        val viewModel: ConfirmSubmissionViewModel = ConfirmSubmissionViewModel(
          vatReturnBoxInvalid(8000.01, 40000.01, hasFlatRateScheme = true),
          periodKey,
          userName = customerDetailsWithFRS.clientName
        )

        lazy val view = confirmSubmissionView(viewModel, isAgent = false)(
          messages, mockAppConfig, user)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "display the correct warning header" in {
          elementText(Selectors.warningHeader) shouldBe
            viewMessages.WarningMessages.headerSingleWarning(4, 7)
        }

        "display the correct list header" in {
          elementText(Selectors.listHeader) shouldBe viewMessages.WarningMessages.listHeading
        }

        "display the correct first item in the list" in {
          elementText(Selectors.listItem1) shouldBe viewMessages.WarningMessages.listCommonItem
        }

        "display the correct second item in the list" in {
          elementText(Selectors.listItem2) shouldBe
            viewMessages.WarningMessages.listItemSingleWarning(7)
        }

        "display the correct bottom text" in {
          elementText(Selectors.bottomText) shouldBe viewMessages.WarningMessages.listCommonBottomText
        }
      }

      "the ratio of boxes 1 and 6, and 4 and 7, are too great" should {
        val viewModel: ConfirmSubmissionViewModel = ConfirmSubmissionViewModel(
          vatReturnBoxInvalid(80000.01, 40000.01, hasFlatRateScheme = true),
          periodKey,
          userName = customerDetailsWithFRS.clientName
        )

        lazy val view = confirmSubmissionView(viewModel, isAgent = false)(
          messages, mockAppConfig, user)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "display the correct warning header" in {
          elementText(Selectors.warningHeader) shouldBe viewMessages.WarningMessages.headerMultipleWarning
        }

        "display the correct list header" in {
          elementText(Selectors.listHeader) shouldBe viewMessages.WarningMessages.listHeading
        }

        "display the correct first item in the list" in {
          elementText(Selectors.listItem1) shouldBe viewMessages.WarningMessages.listCommonItem
        }

        "display the correct second item in the list" in {
          elementText(Selectors.listItem2) shouldBe viewMessages.WarningMessages.listItemMultipleWarning
        }

        "display the correct bottom text" in {
          elementText(Selectors.bottomText) shouldBe viewMessages.WarningMessages.listCommonBottomText
        }
      }
    }

    "the user is not on the flat rate scheme" should {

      val viewModel: ConfirmSubmissionViewModel = ConfirmSubmissionViewModel(
        vatReturnValid(false),
        periodKey,
        userName = customerDetailsWithFRS.clientName
      )

      lazy val view = confirmSubmissionView(viewModel, isAgent = false)(messages, mockAppConfig, user)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      s"display the box 6 description as $box6NonFlatRateSchemeText" in {
        elementText(boxElement(Selectors.boxes(5), 2)) shouldBe box6NonFlatRateSchemeText
      }
    }

    "the user is an agent" should {

      val viewModel: ConfirmSubmissionViewModel = ConfirmSubmissionViewModel(
        vatReturnValid(false),
        periodKey,
        userName = customerDetailsModel.clientName
      )

      lazy val view = confirmSubmissionView(viewModel, isAgent = true)(messages, mockAppConfig, agentUser)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      s"display the pageTitle as ${viewMessages.agentTitle}" in {
        document.title() shouldBe viewMessages.agentTitle
      }

      "display the correct declaration" which {

        "displays the correct text" in {
          elementText(Selectors.agentDeclarationText) shouldBe viewMessages.agentDeclarationText
        }

        "does not display the text in bold" in {
          element(Selectors.agentDeclarationText).hasClass("govuk-body") shouldBe true
        }
      }
    }
  }
}
