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


import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import assets.messages.{ConfirmSubmissionMessages => viewMessages}

class ConfirmSubmissionViewSpec extends ViewBaseSpec {

  object Selectors {
    val box1Heading = "#box-one > div.column-one-quarter.form-hint"
    val box1Description = "#box-one > div.column-one-half.form-hint"
    val box2Heading = "#box-two > div.column-one-quarter.form-hint"
    val box2Description = "#box-two > div.column-one-half.form-hint"
    val box3Heading = "#box-three > div.column-one-quarter.form-hint"
    val box3Description = "#box-three > div.column-one-half.form-hint"
    val box4Heading = "#box-four > div.column-one-quarter.form-hint"
    val box4Description = "#box-four > div.column-one-half.form-hint"
    val box5Heading = "#box-five > div:nth-child(1)"
    val box5Description = "#box-five > div.column-one-half"
    val box6Heading = "#box-six > div.column-one-quarter.form-hint"
    val box6Description = "#box-six > div:nth-child(2)"
    val box7Heading = "#box-seven > div.column-one-quarter.form-hint"
    val box7Description = "#box-seven > div.column-one-half.form-hint"
    val box8Heading = "#box-eight > div.column-one-quarter.form-hint"
    val box8Description = "#box-eight > div.column-one-half.form-hint"
    val box9Heading = "#box-nine > div.column-one-quarter.form-hint"
    val box9Description = "#box-nine > div.column-one-half.form-hint"
    val returnTotalHeading = "#content > article > section > section:nth-child(6) > div > h3"
    val returnDueDate = "#content > article > section > section:nth-child(6) > div > p"
    val changeReturnLink = "#content > article > section > section:nth-child(6) > div > a"
    val submitVatReturnHeading = "#content > article > section > h3.bold-medium"
    val submitReturnInformation = "#content > article > section > p"
    val submitButton = "#content > article > section > a"
  }


  "Confirm Submission View" when {

    "the user owes HMRC money" should {

      "displays the correct information" should {

        lazy val view = views.html.confirm_submission()
        lazy implicit val document: Document = Jsoup.parse(view.body)

        s"the title is displayed as ${viewMessages.title}" in {
          document.title shouldBe viewMessages.title
        }

        s"the smaller ${viewMessages.returnDueDate} heading" in {
          elementText("#content > article > section > h1 > p") shouldBe s"${viewMessages.returnDueDate} {0}"
        }

        s"the heading is displayed as ${viewMessages.heading}" in {
          elementText("#content > article > section > h1 > span") shouldBe viewMessages.heading
        }

        "the obligation period is displayed" in {
          elementText("h1") contains "OBLIGATION DATE"
        }

        "the name is displayed as TEST" in {
          elementText("h2") shouldBe "TEST"
        }

        s"the subheading vat detail is displayed as ${viewMessages.subHeadingVatDetails}" in {
          elementText("h3") shouldBe viewMessages.subHeadingVatDetails
        }

        s"the box 1 heading is displayed as ${viewMessages.box1Heading}" in {
          elementText(Selectors.box1Heading) shouldBe viewMessages.box1Heading
        }

        s"the box 1 detail is displayed as ${viewMessages.box1Description}" in {
          elementText(Selectors.box1Description) shouldBe viewMessages.box1Description
        }

        s"the box 2 heading is displayed as ${viewMessages.box1Heading}" in {
          elementText(Selectors.box1Heading) shouldBe viewMessages.box1Heading
        }

        s"the box 2 detail is displayed as ${viewMessages.box2Description}" in {
          elementText(Selectors.box2Description) shouldBe viewMessages.box2Description
        }

        s"the box 3 heading is displayed as ${viewMessages.box3Heading}" in {
          elementText(Selectors.box3Heading) shouldBe viewMessages.box3Heading
        }

        s"the box 3 detail is displayed as ${viewMessages.box3Description}" in {
          elementText(Selectors.box3Description) shouldBe viewMessages.box3Description
        }

        s"the box 4 heading is displayed as ${viewMessages.box4Heading}" in {
          elementText(Selectors.box4Heading) shouldBe viewMessages.box4Heading
        }

        s"the box 4 detail is displayed as ${viewMessages.box4Description}" in {
          elementText(Selectors.box4Description) shouldBe viewMessages.box4Description
        }

        s"the box 5 heading is displayed as ${viewMessages.box5Heading}" in {
          elementText(Selectors.box5Heading) shouldBe viewMessages.box5Heading
        }

        s"the box 5 detail is displayed as ${viewMessages.box5Description}" in {
          elementText(Selectors.box5Description) shouldBe viewMessages.box5Description
        }

        s"the box 6 heading is displayed as ${viewMessages.box6Heading}" in {
          elementText(Selectors.box6Heading) shouldBe viewMessages.box6Heading
        }

//        s"the box 6 detail is displayed as ${viewMessages.box6Description}" in {
//          elementText(Selectors.box6Description) shouldBe viewMessages.box6Description
//        }

        s"the box 7 heading is displayed as ${viewMessages.box7Heading}" in {
          elementText(Selectors.box7Heading) shouldBe viewMessages.box7Heading
        }

        s"the box 7 detail is displayed as ${viewMessages.box7Description}" in {
          elementText(Selectors.box7Description) shouldBe viewMessages.box7Description
        }

        s"the box 8 heading is displayed as ${viewMessages.box8Heading}" in {
          elementText(Selectors.box8Heading) shouldBe viewMessages.box8Heading
        }

        s"the box 8 detail is displayed as ${viewMessages.box8Description}" in {
          elementText(Selectors.box8Description) shouldBe viewMessages.box8Description
        }

        s"the box 9 heading is displayed as ${viewMessages.box9Heading}" in {
          elementText(Selectors.box9Heading) shouldBe viewMessages.box9Heading
        }

        s"the box 9 detail is displayed as ${viewMessages.box9Description}" in {
          elementText(Selectors.box9Description) shouldBe viewMessages.box9Description
        }

        s"the return total heading is shown as ${viewMessages.returnTotal}" in {
          elementText(Selectors.returnTotalHeading) shouldBe s"${viewMessages.returnTotal} {0}"
        }

        s"the return due date is shown as ${viewMessages.returnDueDate}" in {
          elementText(Selectors.returnDueDate) shouldBe s"${viewMessages.returnDueDate} {0}"
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

    "HMRC owes the user money" should {

    }

  }

}
