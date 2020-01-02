/*
 * Copyright 2020 HM Revenue & Customs
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

class UnauthorisedAgentViewSpec extends ViewBaseSpec {

  "Rendering the unauthorised page for agents" should {

    object Selectors {
      val serviceName = ".header__menu__proposition-name"
      val pageHeading = "#content h1"
      val instructions = "#content article p"
      val instructionsLink = "#content article p > a"
      val button = "#content .button"
    }

    lazy val view = views.html.errors.unauthorised_agent()(fakeRequest, messages, mockAppConfig, user = Some(user))
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct document title" in {
      document.title shouldBe AuthMessages.unauthorisedTitle
    }

    "have a the correct page heading" in {
      elementText(Selectors.pageHeading) shouldBe AuthMessages.unauthorisedHeading
    }

    "have the correct instructions on the page" in {
      elementText(Selectors.instructions) shouldBe AuthMessages.setupAccount
    }

    "have a link to GOV.UK guidance" in {
      element(Selectors.instructionsLink).attr("href") shouldBe "agent-services"
    }

    "have a Sign out button" in {
      elementText(Selectors.button) shouldBe "Sign out"
    }

    "have a link to sign out" in {
      element(Selectors.button).attr("href") shouldBe controllers.routes.SignOutController.signOut(false).url
    }
  }
}

