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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import assets.messages.{ConfirmSubmissionMessages => viewMessages}
import models._
import assets.CustomerDetailsTestAssets._
import models.auth.User
import play.api.mvc.AnyContentAsEmpty

class ConfirmSubmissionViewSpec extends ViewBaseSpec {

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

    val breadcrumbOne = "div.breadcrumbs li:nth-of-type(1)"
    val breadcrumbOneLink = "div.breadcrumbs li:nth-of-type(1) a"
    val breadcrumbTwo = "div.breadcrumbs li:nth-of-type(2)"
    val breadcrumbTwoLink = "div.breadcrumbs li:nth-of-type(2) a"
    val breadcrumbCurrentPage = "div.breadcrumbs li:nth-of-type(3)"
    val declarationHeader = "#content > article > section > div:nth-child(7) > h3"
    val nonAgentDeclarationText = "#content > article > section > div.notice.form-group > strong"
    val agentDeclarationText = "#content > article > section > p"
    val noticeImage = "#content > article > section > div.notice.form-group > i"
    val noticeText = "#content > article > section > div.notice.form-group > i > span"
  }

  def boxElement(box: String, column: Int): String = {
    s"$box > div:nth-child($column)"
  }

  val periodKey = "17AA"

  val vatObligation: VatObligation = VatObligation(
    start = LocalDate.parse("2019-01-12"),
    end = LocalDate.parse("2019-04-12"),
    due = LocalDate.parse("2019-05-12"),
    periodKey = "17AA"
  )

  def vatReturn(hasFlatRateScheme: Boolean): SubmitVatReturnModel = SubmitVatReturnModel(
    box1 = 1000.01,
    box2 = 1000.02,
    box3 = 1000.03,
    box4 = 1000.04,
    box5 = 1000.05,
    box6 = 1000.06,
    box7 = 1000.07,
    box8 = 1000.08,
    box9 = 1000.09,
    flatRateScheme = hasFlatRateScheme,
    start = LocalDate.parse("2019-01-12"),
    end = LocalDate.parse("2019-04-12"),
    due = LocalDate.parse("2019-05-12")
  )

  "Confirm Submission View" when {

    "user is non-agent and on the flat rate scheme" should {

      "displays the correct information" should {

        val viewModel: ConfirmSubmissionViewModel = ConfirmSubmissionViewModel(
          vatReturn(true),
          periodKey,
          userName = customerDetailsWithFRS.clientName
        )

        lazy val view = views.html.confirm_submission(viewModel, isAgent = false)(fakeRequest, messages, mockAppConfig, user)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        s"the title is displayed as ${viewMessages.title}" in {
          document.title shouldBe viewMessages.title
        }

        "render breadcrumbs" which {

          "has the 'Your VAT details' title" in {
            elementText(Selectors.breadcrumbOne) shouldBe "Your VAT details"
          }

          "and links to the VAT Overview page" in {
            element(Selectors.breadcrumbOneLink).attr("href") shouldBe mockAppConfig.vatSummaryUrl
          }


          "has the 'Submit VAT Return' title" in {
            elementText(Selectors.breadcrumbTwo) shouldBe "Submit VAT Return"
          }

          "and links to the Return deadlines page" in {
            element(Selectors.breadcrumbTwoLink).attr("href") shouldBe mockAppConfig.returnDeadlinesUrl
          }
        }


        "has the correct current page title" in {
          elementText(Selectors.breadcrumbCurrentPage) shouldBe "Submit 12 January to 12 April 2019 return"
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
            "£1,000.01",
            "£1,000.02",
            "£1,000.03",
            "£1,000.04",
            "£1,000.05",
            "£1,000.06",
            "£1,000.07",
            "£1,000.08",
            "£1,000.09"
          )
          expectedInformation.indices.foreach(i => elementText(boxElement(Selectors.boxes(i), 3)) shouldBe expectedInformation(i))
        }

        s"the return total heading is shown as ${viewMessages.returnTotal} ${viewModel.returnDetail.box5}" in {
          elementText(Selectors.returnTotalHeading) shouldBe s"${viewMessages.returnTotal} £1,000.05"
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
      }
    }

    "the user is not on the flat rate scheme" should {

      val viewModel: ConfirmSubmissionViewModel = ConfirmSubmissionViewModel(
        vatReturn(false),
        periodKey,
        userName = customerDetailsWithFRS.clientName
      )

      lazy val view = views.html.confirm_submission(viewModel, isAgent = false)(fakeRequest, messages, mockAppConfig, user)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      s"box 6 description displays as ${viewMessages.box6DescriptionNoFRS}" in {
        elementText(boxElement(Selectors.boxes(5), 2)) shouldBe viewMessages.box6DescriptionNoFRS
      }

    }

    "user is an agent" should {

      val viewModel: ConfirmSubmissionViewModel = ConfirmSubmissionViewModel(
        vatReturn(false),
        periodKey,
        userName = customerDetailsModel.clientName
      )

      lazy val view = views.html.confirm_submission(viewModel, isAgent = true)(fakeRequest, messages, mockAppConfig, user)
      lazy implicit val document: Document = Jsoup.parse(view.body)

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
