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

import assets.CustomerDetailsTestAssets._
import assets.messages.{ConfirmSubmissionMessages => viewMessages}
import models._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.ConfirmSubmission

class ConfirmSubmissionViewSpec extends ViewBaseSpec {

  val confirmSubmissionView: ConfirmSubmission = inject[ConfirmSubmission]

  object Selectors {
    val boxes = List(
      "#box-one", "#box-two", "#box-three",
      "#box-four", "#box-five", "#box-six",
      "#box-seven", "#box-eight", "#box-nine"
    )
    val backLink = "#content > article > a"
    val returnTotalHeading = "#content > article > section > section > div > h3"
    val returnDueDate = "#content > article > section > section > div > p"
    val changeReturnLink = "#content > article > section > section > div > a"
    val submitVatReturnHeading = "#content > article > section > h3.bold-medium"
    val submitReturnInformation = "#content > article > section > p"
    val submitButton = "#content > article > section > form > button"

    val breadcrumbOne = "div.breadcrumbs li:nth-of-type(1)"
    val breadcrumbOneLink = "div.breadcrumbs li:nth-of-type(1) a"
    val breadcrumbTwo = "div.breadcrumbs li:nth-of-type(2)"
    val breadcrumbTwoLink = "div.breadcrumbs li:nth-of-type(2) a"
    val breadcrumbCurrentPage = "div.breadcrumbs li:nth-of-type(3)"
    val declarationHeader = "#content > article > section > div > h3"
    val nonAgentDeclarationText = "#content > article > section > div.notice.form-group > strong"
    val agentDeclarationText = "#content > article > section > p"
    val noticeImage = "#content > article > section > div.notice.form-group > i"
    val noticeText = "#content > article > section > div.notice.form-group > i > span"

    val warningHeader = "div.grid-row.panel.panel-border-wide > p:nth-child(1)"
    val listHeader = "div.grid-row.panel.panel-border-wide > p:nth-child(2)"
    val listItem1 = "div.grid-row.panel.panel-border-wide > ul > li:nth-child(1)"
    val listItem2 = "div.grid-row.panel.panel-border-wide > ul > li:nth-child(2)"
    val bottomText = "div.grid-row.panel.panel-border-wide > p:nth-child(4)"
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

        "the NI protocol feature switch is off" when {

          "the 9 box values are all valid" should {

            val viewModel: ConfirmSubmissionViewModel = ConfirmSubmissionViewModel(
              vatReturnValid(true),
              periodKey,
              userName = customerDetailsWithFRS.clientName
            )

            lazy val view = confirmSubmissionView(viewModel, isAgent = false, nIProtocolEnabled = false)(
              messages, mockAppConfig, user)
            lazy implicit val document: Document = Jsoup.parse(view.body)

            s"the title is displayed as ${viewMessages.title}" in {
              document.title shouldBe viewMessages.title
            }

            s"the back link is displayed with the correct href" in {
              elementText(Selectors.backLink) shouldBe viewMessages.back
              element(Selectors.backLink).attr("href") shouldBe
                controllers.routes.SubmitFormController.show(periodKey).url
            }

            s"the smaller ${viewMessages.returnDueDate} heading" in {
              elementText("#content > article > section > h1 > p") shouldBe s"${viewMessages.returnDueDate} 12 May 2019"
            }

            s"the heading is displayed as ${viewMessages.heading}" in {
              elementText("#content > article > section > h1 > span") shouldBe viewMessages.heading
            }

            "the obligation period is displayed" in {
              elementText("h1") contains "12 Jan to 12 Apr 2019"
            }

            s"the name is displayed as ${viewModel.userName}" in {
              elementText("h2") shouldBe viewModel.userName.getOrElse("")
            }

            s"the subheading vat detail is displayed as ${viewMessages.subHeadingVatDetails}" in {
              elementText("h3") shouldBe viewMessages.subHeadingVatDetails
            }

            "have the correct box numbers in the table" in {
              val expectedBoxes = Array(
                viewMessages.box1Heading,
                viewMessages.box2Heading,
                viewMessages.box3Heading,
                viewMessages.box4Heading,
                viewMessages.box5Heading,
                viewMessages.box6Heading,
                viewMessages.box7Heading,
                viewMessages.box8Heading,
                viewMessages.box9Heading
              )
              expectedBoxes.indices.foreach(i => elementText(boxElement(Selectors.boxes(i), 1)) shouldBe expectedBoxes(i))
            }

            "have the correct row descriptions in the table" in {
              val expectedDescriptions = Array(
                viewMessages.box1Description,
                viewMessages.box2Description,
                viewMessages.box3Description,
                viewMessages.box4Description,
                viewMessages.box5Description,
                viewMessages.box6DescriptionHasFRS,
                viewMessages.box7Description,
                viewMessages.box8Description,
                viewMessages.box9Description
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

            s"the return total heading is shown as ${viewMessages.returnTotal} ${viewModel.returnDetail.box5}" in {
              elementText(Selectors.returnTotalHeading) shouldBe s"${viewMessages.returnTotal} £12,000.02"
            }

            s"the return due date is shown as ${viewMessages.returnDueDate}" in {
              elementText(Selectors.returnDueDate) shouldBe s"${viewMessages.returnDueDate} 12 May 2019"
            }


            "have the change return details link which" should {

              s"the redirect url to ${controllers.routes.SubmitFormController.show(periodKey).url}" in {
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

            "display the correct declaration which" should {

              "display the warning notice which" should {

                "display the image" in {
                  element(Selectors.noticeImage).hasClass("icon icon-important") shouldBe true
                }

                "display the hidden text" in {
                  elementText(Selectors.noticeText) shouldBe viewMessages.warning
                }
              }

              "display the correct text" in {
                elementText(Selectors.nonAgentDeclarationText) shouldBe viewMessages.nonAgentDeclarationText
              }

              "display the text in bold" in {
                element(Selectors.nonAgentDeclarationText).hasClass("bold-small") shouldBe true
              }
            }

            s"display the ${viewMessages.submitButton} button" in {
              elementText(Selectors.submitButton) shouldBe viewMessages.submitButton
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

            lazy val view = confirmSubmissionView(viewModel, isAgent = false, nIProtocolEnabled = false)(
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

            lazy val view = confirmSubmissionView(viewModel, isAgent = false, nIProtocolEnabled = false)(
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

            lazy val view = confirmSubmissionView(viewModel, isAgent = false, nIProtocolEnabled = false)(
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

        "the NI protocol feature switch is on" should {

          val viewModel: ConfirmSubmissionViewModel = ConfirmSubmissionViewModel(
            vatReturnBoxInvalid(8000.01, 40000.01, hasFlatRateScheme = true),
            periodKey,
            userName = customerDetailsWithFRS.clientName
          )

          lazy val view = confirmSubmissionView(viewModel, isAgent = false, nIProtocolEnabled = true)(
            messages, mockAppConfig, user)
          lazy implicit val document: Document = Jsoup.parse(view.body)

          "have the correct row descriptions in the table" in {
            val expectedDescriptions = Array(
              viewMessages.box1Description,
              viewMessages.box2DescriptionNIProtocol,
              viewMessages.box3Description,
              viewMessages.box4Description,
              viewMessages.box5Description,
              viewMessages.box6DescriptionHasFRS,
              viewMessages.box7Description,
              viewMessages.box8DescriptionNIProtocol,
              viewMessages.box9DescriptionNIProtocol
            )
            expectedDescriptions.indices.foreach(i => elementText(boxElement(Selectors.boxes(i), 2)) shouldBe expectedDescriptions(i))
          }
        }
      }
    }

    "the user is not on the flat rate scheme" should {

      val viewModel: ConfirmSubmissionViewModel = ConfirmSubmissionViewModel(
        vatReturnValid(false),
        periodKey,
        userName = customerDetailsWithFRS.clientName
      )

      lazy val view = confirmSubmissionView(viewModel, isAgent = false, nIProtocolEnabled = false)(messages, mockAppConfig, user)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      s"box 6 description displays as ${viewMessages.box6DescriptionNoFRS}" in {
        elementText(boxElement(Selectors.boxes(5), 2)) shouldBe
          viewMessages.box6DescriptionNoFRS
      }

    }

    "user is an agent" should {

      val viewModel: ConfirmSubmissionViewModel = ConfirmSubmissionViewModel(
        vatReturnValid(false),
        periodKey,
        userName = customerDetailsModel.clientName
      )

      lazy val view = confirmSubmissionView(viewModel, isAgent = true, nIProtocolEnabled = false)(messages, mockAppConfig, agentUser)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      s"display the pageTitle as ${viewMessages.agentTitle}" in {
        document.title() shouldBe viewMessages.agentTitle
      }

      "not render breadcrumbs" in {
        document.select(Selectors.breadcrumbOne) shouldBe empty
      }

      "display the correct declaration which" should {

        "display the correct text" in {
          elementText(Selectors.agentDeclarationText) shouldBe viewMessages.agentDeclarationText
        }

        "not display the text in bold" in {
          element(Selectors.agentDeclarationText).hasClass("bold-small") shouldBe false
        }
      }
    }
  }
}
