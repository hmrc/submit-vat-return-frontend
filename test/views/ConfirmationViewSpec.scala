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

import assets.messages.{ConfirmationPageMessages => viewMessages}
import common.SessionKeys
import models.auth.User
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.AnyContentAsEmpty
import views.html.ConfirmationView

class ConfirmationViewSpec extends ViewBaseSpec {

  val confirmationView: ConfirmationView = inject[ConfirmationView]

  "Confirmation view" when {

    "year and period key are in session" should {

      val submitYear = "2019"
      val periodKey = "21BB"

      lazy val userWithSession: User[AnyContentAsEmpty.type] = User[AnyContentAsEmpty.type](vrn)(fakeRequest.withSession(
        SessionKeys.submissionYear -> submitYear,
        SessionKeys.inSessionPeriodKey -> periodKey)
      )

      lazy val view = confirmationView()(messages, mockAppConfig, userWithSession)

      lazy implicit val document: Document = Jsoup.parse(view.body)

      s"display the title as ${viewMessages.title}" in {
        document.title shouldBe viewMessages.title
      }

      s"display the h1 as ${viewMessages.heading}" in {
        elementText("h1") shouldBe viewMessages.heading
      }

      s"display the h2 as ${viewMessages.subHeading}" in {
        elementText("h2") shouldBe viewMessages.subHeading
      }

      s"display the paragraph text as ${viewMessages.paragraph}" in {
        elementText("#content article p") shouldBe viewMessages.paragraph
      }

      "display a View Return button" should {

        s"have the button text as ${viewMessages.button}" in {
          elementText("#view-vat-return-button") shouldBe viewMessages.button
        }

        "have the correct redirect link" in {
          element("#view-vat-return-button").attr("href") shouldBe
            mockAppConfig.viewSubmittedReturnUrl + s"/$submitYear/$periodKey"
        }
      }

      "display a Finish button" should {

        s"have the button text as ${viewMessages.button2}" in {
          elementText("#finish-button2") shouldBe viewMessages.button2
        }
      }
    }

    "year and period key are not in session" should {

      lazy val userWithSession: User[AnyContentAsEmpty.type] = User[AnyContentAsEmpty.type](vrn)(fakeRequest)

      lazy val view = confirmationView()(messages, mockAppConfig, userWithSession)

      lazy implicit val document: Document = Jsoup.parse(view.body)

      "display a View Return button" should {

        "have the correct redirect link" in {
          element("#view-vat-return-button").attr("href") shouldBe mockAppConfig.viewSubmittedReturnUrl
        }
      }
    }

    "user is an Agent" should {

      lazy val agent: User[AnyContentAsEmpty.type] = User[AnyContentAsEmpty.type]("999999999", Some("123456789"))
      lazy val feature_view = confirmationView()(messages, mockAppConfig, agent)

      lazy implicit val document: Document = Jsoup.parse(feature_view.body)

      "display the Agent Finish button" should {

        s"have the button text as ${viewMessages.agentFinishButton}" in {
          elementText("#finish-button2") shouldBe viewMessages.agentFinishButton
        }
      }
    }
  }
}
