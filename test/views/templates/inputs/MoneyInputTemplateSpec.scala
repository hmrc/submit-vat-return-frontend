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

package views.templates.inputs

import play.api.data.Form
import play.api.data.Forms._
import play.twirl.api.Html
import views.html.templates.inputs.MoneyInput
import views.templates.TemplateBaseSpec

class MoneyInputTemplateSpec extends TemplateBaseSpec {

  val moneyInput: MoneyInput = inject[MoneyInput]

  "Calling MoneyInput" when {

    case class Model(amount: Int)

    val form: Form[Model] = Form(
      mapping(
        "amount" -> number
      )(Model.apply)(Model.unapply)
    ).fill(Model(3))

    "field has no errors and no default arguments overridden" should {

      val template = moneyInput(
        field = form("amount"),
        label = "label1",
        decimalPlace = true
      )

      val expectedMarkup = formatHtml(Html(
        s"""
           |<div class="form-group "> <label for="amount" class="form-label visuallyhidden"> label1 </label>
           |<div class="hmrc-currency-input__wrapper"> <span class="hmrc-currency-input__unit" aria-hidden="true">£</span>
           |<input class="govuk-input govuk-input--width-10" id="amount" name="amount" value="3" type="text"
           |aria-describedby="amount" autocomplete="off" inputmode="decimal">
           |</div>
           |</div>
      """.stripMargin
      ))

      "render the correct markup" in {
        formatHtml(template) shouldBe expectedMarkup
      }
    }

    "field has an error" should {

      val template = moneyInput(
        field = form.withError("amount", "Error message")("amount"),
        label = "label1",
        decimalPlace = true
      )

      val expectedMarkup = formatHtml(Html(
        s"""
           |<div class="form-group form-field--error"> <label for="amount" class="form-label visuallyhidden"> label1 </label>
           |<div class="hmrc-currency-input__wrapper"> <span class="hmrc-currency-input__unit" aria-hidden="true">£</span>
           |<input class="govuk-input govuk-input--width-10 error-field-thin" id="amount" name="amount" value="3" type="text"
           |aria-describedby="amount" autocomplete="off" inputmode="decimal">
           |</div>
           |</div>
      """.stripMargin
      ))

      "render the correct markup" in {
        formatHtml(template) shouldBe expectedMarkup
      }
    }

    "hint text is supplied" should {

      val template = moneyInput(
        field = form("amount"),
        label = "label1",
        decimalPlace = true,
        hint = Some("Hint text")
      )

      val expectedMarkup = formatHtml(Html(
        s"""
           |<div class="form-group "> <label for="amount" class="form-label visuallyhidden"> label1 </label>
           |<span class="form-hint"> Hint text </span>
           |<div class="hmrc-currency-input__wrapper"> <span class="hmrc-currency-input__unit" aria-hidden="true">£</span>
           |<input class="govuk-input govuk-input--width-10" id="amount" name="amount" value="3" type="text"
           |aria-describedby="amount" autocomplete="off" inputmode="decimal">
           |</div>
           |</div>
      """.stripMargin
      ))

      "render the correct markup" in {
        formatHtml(template) shouldBe expectedMarkup
      }
    }

    "label is not hidden" should {

      val template = moneyInput(
        field = form("amount"),
        label = "label1",
        decimalPlace = true,
        labelHidden = false
      )

      val expectedMarkup = formatHtml(Html(
        s"""
           |<div class="form-group "> <label for="amount" class="form-label "> label1 </label>
           |<div class="hmrc-currency-input__wrapper"> <span class="hmrc-currency-input__unit" aria-hidden="true">£</span>
           |<input class="govuk-input govuk-input--width-10" id="amount" name="amount" value="3"
           |type="text" aria-describedby="amount" autocomplete="off" inputmode="decimal">
           |</div>
           |</div>
      """.stripMargin
      ))
      "render the correct markup" in {
        formatHtml(template) shouldBe expectedMarkup
      }
    }
  }
}
