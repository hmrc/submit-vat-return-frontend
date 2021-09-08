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
import assets.messages.SubmitFormPageMessages._
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
          vatReturnBoxInvalid(8000.01, 40000.01, hasFlatRateScheme = true),
          periodKey,
          userName = customerDetailsWithFRS.clientName
        )

        lazy val view = confirmSubmissionView(viewModel, isAgent = false)(
          messages, mockAppConfig, user)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the correct row descriptions in the table" in {
          val expectedDescriptions = Array(
            box1TextNIProtocol,
            box2TextNIProtocol,
            box3TextNIProtocol,
            box4TextNIProtocol,
            box5TextNIProtocol,
            box6FlatRateSchemeText,
            box7TextNIProtocol,
            box8TextNIProtocol,
            box9TextNIProtocol
          )
          expectedDescriptions.indices.foreach(i => elementText(boxElement(Selectors.boxes(i), 2)) shouldBe expectedDescriptions(i))
        }
      }
    }
  }
}
