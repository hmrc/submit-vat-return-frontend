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

package views.templates

import forms.HonestyDeclarationForm
import play.api.data.Form
import play.twirl.api.Html
import views.html.templates.ErrorSummary

class ErrorSummarySpec extends TemplateBaseSpec {

  val injectedView: ErrorSummary = inject[ErrorSummary]
  val form: Form[Boolean] = HonestyDeclarationForm.honestyDeclarationForm
  val formWithError: Form[Boolean] = HonestyDeclarationForm.honestyDeclarationForm.bind(Map("checkbox" -> ""))

  "ErrorSummary" when {

    "the form has errors" should {

      "render the correct Html" in {

        val expectedMarkup = Html(
          s"""
             |<div class="flash error-summary error-summary--show" id="error-summary-display" role="alert" aria-labelledby="error-summary-heading" tabindex="-1">
             |  <h2 id="error-summary-heading" class="h3-heading">heading</h2>
             |  <ul class="js-error-summary-messages">
             |    <li class="error-summary-list"> <a href="#checkbox" id="checkbox-error-summary" data-focuses="checkbox"> error.boolean </a> </li>
             |  </ul>
             |</div>
           """.stripMargin
        )

        val result = injectedView("heading", formWithError)

        formatHtml(result) shouldBe formatHtml(expectedMarkup)
      }
    }

    "the form has no errors" should {

      "return nothing" in {

        val result = injectedView("heading", form)

        formatHtml(result) shouldBe formatHtml(Html(""))
      }
    }
  }
}
