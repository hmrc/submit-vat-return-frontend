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
import assets.messages.{ConfirmSubmissionMessages => viewMessages, CommonMessages}
import models.{ConfirmSubmissionViewModel, CustomerDetails, VatObligation, VatReturnDetails}

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
  }

  def boxElement(box: String, column: Int): String = {
    s"$box > div:nth-child($column)"
  }

  val vatObligation: VatObligation = VatObligation(
    start = LocalDate.parse("2019-01-12"),
    end = LocalDate.parse("2019-04-12"),
    due = LocalDate.parse("2019-05-12"),
    periodKey = "17AA"
  )

  val customerDetails: CustomerDetails = CustomerDetails(
    None,
    None,
    None,
    organisationName = Some("ABC Trading")
  )

  val vatReturn: VatReturnDetails = VatReturnDetails(
    boxOne = 1000.00,
    boxTwo = 1000.00,
    boxThree = 1000.00,
    boxFour = 1000.00,
    boxFive = 1000.00,
    boxSix = 1000.00,
    boxSeven = 1000.00,
    boxEight = 1000.00,
    boxNine = 1000.00
  )


  "Confirm Submission View" when {

    "the user is on the flat rate scheme" should {

      "displays the correct information" should {

        val viewModel: ConfirmSubmissionViewModel = ConfirmSubmissionViewModel(
          vatObligation,
          hasFlatRateScheme = true,
          vatReturn,
          userName = customerDetails.clientName.getOrElse(""),
          vatReturnTotal = 10000.00
        )

        lazy val view = views.html.confirm_submission(viewModel)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        s"the title is displayed as ${viewMessages.title}" in {
          document.title shouldBe viewMessages.title
        }

        "have a back link which" should {

          s"have the correct content of ${CommonMessages.backLink}" in {
            elementText(Selectors.backLink) shouldBe CommonMessages.backLink
          }

          s"have the redirect url as ${controllers.routes.HelloWorldController.helloWorld()}" in {
            element(Selectors.backLink).attr("href") shouldBe controllers.routes.HelloWorldController.helloWorld().url
          }
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
          elementText("h2") shouldBe viewModel.userName
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
            "£1,000",
            "£1,000",
            "£1,000",
            "£1,000",
            "£1,000",
            "£1,000",
            "£1,000",
            "£1,000",
            "£1,000"
          )
          expectedInformation.indices.foreach(i => elementText(boxElement(Selectors.boxes(i), 3)) shouldBe expectedInformation(i))
        }

        s"the return total heading is shown as ${viewMessages.returnTotal} ${viewModel.vatReturnTotal}" in {
          elementText(Selectors.returnTotalHeading) shouldBe s"${viewMessages.returnTotal} £10,000"
        }

        s"the return due date is shown as ${viewMessages.returnDueDate}" in {
          elementText(Selectors.returnDueDate) shouldBe s"${viewMessages.returnDueDate} 12 May 2019"
        }

        "have the change return details link which" should {

          s"the redirect url to URL NEEDED" in {
            element(Selectors.changeReturnLink).attr("href") shouldBe "/TODO"
          }

          s"display the correct content as ${viewMessages.changeReturnLink}" in {
            elementText(Selectors.changeReturnLink) shouldBe viewMessages.changeReturnLink
          }
        }

        s"displays the ${viewMessages.nowSubmitReturnHeading} heading" in {
          elementText(Selectors.submitVatReturnHeading) shouldBe viewMessages.nowSubmitReturnHeading
        }

        "display the legal declaration paragraph" in {
          elementText(Selectors.submitReturnInformation) shouldBe viewMessages.submitReturnInformation
        }

        s"display the ${viewMessages.submitButton} button" in {
          elementText(Selectors.submitButton) shouldBe viewMessages.submitButton
        }
      }
    }

    "the user is not the flat rate scheme" should {

      val viewModel: ConfirmSubmissionViewModel = ConfirmSubmissionViewModel(
        vatObligation,
        hasFlatRateScheme = false,
        vatReturn,
        userName = customerDetails.clientName.getOrElse(""),
        vatReturnTotal = 10000.00
      )

      lazy val view = views.html.confirm_submission(viewModel)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      s"box 6 description displays as ${viewMessages.box6DescriptionNoFRS}" in {
        elementText(boxElement(Selectors.boxes(5), 2)) shouldBe viewMessages.box6DescriptionNoFRS
      }

    }

  }

}
