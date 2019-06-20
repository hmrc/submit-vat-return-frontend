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

package views.errors

import models.auth.User
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.AnyContentAsEmpty
import views.ViewBaseSpec
import assets.messages.IncorrectMandationMessages

class IncorrectMandationUserViewSpec extends ViewBaseSpec {

  "Rendering the incorrect mandation user view" should {

    object Selectors {
      val serviceName = ".header__menu__proposition-name"
      val pageHeading = "#content h1"
      val instructions = "#content p"
      val instructionsLink = "#content a"
    }

    val user = User[AnyContentAsEmpty.type]("999999999")(fakeRequest)
    lazy val view = views.html.errors.incorrect_mandation_user()(fakeRequest, mockAppConfig, messages, user = Some(user))
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct document title" in {
      document.title shouldBe IncorrectMandationMessages.title
    }

    "have a the correct page heading" in {
      elementText(Selectors.pageHeading) shouldBe IncorrectMandationMessages.heading
    }

    "have the correct instructions on the page" in {
      elementText(Selectors.instructions) shouldBe IncorrectMandationMessages.paragraph
    }

    s"have the correct link text of ${IncorrectMandationMessages.linkText}" in {
      elementText(Selectors.instructionsLink) shouldBe IncorrectMandationMessages.linkText
    }

    "have a link to the return deadlines page" in {
      element(Selectors.instructionsLink).attr("href") shouldBe "/return-deadlines"
    }
  }
}
