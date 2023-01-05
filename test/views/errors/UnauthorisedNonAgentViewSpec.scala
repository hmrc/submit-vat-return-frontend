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

package views.errors

import assets.messages.AuthMessages
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.errors.UnauthorisedNonAgent

class UnauthorisedNonAgentViewSpec extends ViewBaseSpec {

  val unauthorisedNonAgentView: UnauthorisedNonAgent = inject[UnauthorisedNonAgent]

  "Rendering the unauthorised page for non-agents" should {

    object Selectors {
      val pageHeading = "#content h1"
      val instructions = "#content p"
      val instructionsLink = "#content p > a"
      val button = "#content .govuk-button"
    }

    lazy val view = unauthorisedNonAgentView()(fakeRequest, messages, mockAppConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct document title" in {
      document.title shouldBe AuthMessages.unauthorisedTitle
    }

    "have a the correct page heading" in {
      elementText(Selectors.pageHeading) shouldBe AuthMessages.unauthorisedHeading
    }

    "have the correct instructions on the page" in {
      elementText(Selectors.instructions) shouldBe AuthMessages.signup
    }

    "have a link to GOV.UK guidance" in {
      element(Selectors.instructionsLink).attr("href") shouldBe "mtd-vat"
    }

    "have a Sign out button" in {
      elementText(Selectors.button) shouldBe "Sign out"
    }

    "have a link to sign out" in {
      element(Selectors.button).attr("href") shouldBe controllers.routes.SignOutController.signOut(false).url
    }
  }
}
