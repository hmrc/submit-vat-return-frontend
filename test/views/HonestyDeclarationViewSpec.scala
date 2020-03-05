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

package views

import forms.HonestyDeclarationForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class HonestyDeclarationViewSpec extends ViewBaseSpec {

  "Rendering the honesty declaration page" when {

    "the user is not an agent" should {

      lazy val view = views.html.honesty_declaration(
        "18AA",
        HonestyDeclarationForm.honestyDeclarationForm
      )(fakeRequest, messages, mockAppConfig, user)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the back link" in {
        element(".link-back").text() shouldBe messages("common.back")
        element(".link-back").attr("href") shouldBe mockAppConfig.returnDeadlinesUrl
      }

      s"have the title ${messages("honesty_declaration.title")}" in {
        element("#page-heading").text() shouldBe messages("honesty_declaration.title")
      }

      "has the checkbox unchecked" in {
        element("#checkbox").hasAttr("checked") shouldBe false
      }

      "have the correct declaration" in {
        element(".multiple-choice > label").text() shouldBe messages("honesty_declaration.statement")
      }

      "have a continue button" in {
        element(".button").attr("value") shouldBe messages("common.continue")
      }

      "have the correct action attributed to the form" in {
        element("form").attr("action") shouldBe s"${controllers.routes.HonestyDeclarationController.submit("18AA")}"
        element("form").attr("method") shouldBe "POST"
      }
    }

    "the user is an agent" should {

      lazy val view = views.html.honesty_declaration(
        "18AA",
        HonestyDeclarationForm.honestyDeclarationForm
      )(fakeRequest, messages, mockAppConfig, agentUser)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the back link" in {
        element(".link-back").text() shouldBe messages("common.back")
        element(".link-back").attr("href") shouldBe mockAppConfig.returnDeadlinesUrl
      }

      s"have the title ${messages("honesty_declaration.title")}" in {
        element("#page-heading").text() shouldBe messages("honesty_declaration.title")
      }

      "has the checkbox unchecked" in {
        element("#checkbox").hasAttr("checked") shouldBe false
      }

      "have the correct declaration" in {
        element(".multiple-choice > label").text() shouldBe messages("honesty_declaration.agentStatement")
      }

      "have a continue button" in {
        element(".button").attr("value") shouldBe messages("common.continue")
      }

      "have the correct action attributed to the form" in {
        element("form").attr("action") shouldBe s"${controllers.routes.HonestyDeclarationController.submit("18AA")}"
        element("form").attr("method") shouldBe "POST"
      }
    }

  }

}
