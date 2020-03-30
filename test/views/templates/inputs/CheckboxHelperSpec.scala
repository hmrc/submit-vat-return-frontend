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

package views.templates.inputs

import forms.HonestyDeclarationForm
import play.api.data.Forms.{mapping, _}
import play.api.data.{Form, FormError}
import play.twirl.api.Html
import views.html.templates.inputs.CheckboxHelper
import views.templates.TemplateBaseSpec

class CheckboxHelperSpec extends TemplateBaseSpec {

  val checkboxHelper: CheckboxHelper = inject[CheckboxHelper]

  val fieldName = "fieldName"
  val question = "question"
  val hintText = "hintText"
  val errorMessage = "error message"

  val choices: Seq[(String, String)] = Seq(
    "value1" -> "display1",
    "value2" -> "display2",
    "value3" -> "display3",
    "value4" -> "display4",
    "value5" -> "display5"
  )

  case class TestModel(value1: Boolean,
                       value2: Boolean,
                       value3: Boolean,
                       value4: Boolean,
                       value5: Boolean)

  val testForm: Form[TestModel] = Form(mapping(
    "value1" -> boolean,
    "value2" -> boolean,
    "value3" -> boolean,
    "value4" -> boolean,
    "value5" -> boolean
  )(TestModel.apply)(TestModel.unapply))

  def generateExpectedCheckboxMarkup(name: String, display: String, checked: Boolean = false): String =
    s"""
       |  <div class="multiple-choice">
       |    <input type="checkbox" id="$name" name="$name" value="true"${if (checked) " checked" else ""}>
       |    <label for="$name">$display</label>
       |  </div>
      """.stripMargin

  "Calling the checkbox helper with no choice pre-selected" should {

    "render the choices as checkboxes" in {

      val expectedMarkup = Html(
        s"""
           |  <div>
           |    <fieldset>
           |
           |      <legend>
           |        <h1 id="page-heading" class="heading-large">
           |          $question
           |        </h1>
           |      </legend>
           |
           |      <div>
           |        ${generateExpectedCheckboxMarkup("value1", "display1")}
           |        ${generateExpectedCheckboxMarkup("value2", "display2")}
           |        ${generateExpectedCheckboxMarkup("value3", "display3")}
           |        ${generateExpectedCheckboxMarkup("value4", "display4")}
           |        ${generateExpectedCheckboxMarkup("value5", "display5")}
           |      </div>
           |
           |   </fieldset>
           |  </div>
        """.stripMargin
      )

      val markup = checkboxHelper(HonestyDeclarationForm.honestyDeclarationForm, choices, question)

      formatHtml(markup) shouldBe formatHtml(expectedMarkup)
    }
  }

  "Calling the checkbox helper with choices pre-selected" should {

    "render a list of checkboxes with pre-checked boxes" in {
      val expectedMarkup = Html(
        s"""
           |  <div>
           |     <fieldset>
           |
           |      <legend>
           |        <h1 id="page-heading" class="heading-large">
           |          $question
           |        </h1>
           |      </legend>
           |
           |      <div>
           |        ${generateExpectedCheckboxMarkup("value1", "display1", checked = true)}
           |        ${generateExpectedCheckboxMarkup("value2", "display2", checked = true)}
           |        ${generateExpectedCheckboxMarkup("value3", "display3")}
           |        ${generateExpectedCheckboxMarkup("value4", "display4")}
           |        ${generateExpectedCheckboxMarkup("value5", "display5", checked = true)}
           |       </div>
           |    </fieldset>
           |  </div>
        """.stripMargin
      )

      val data = Map(
        "value1" -> "true",
        "value2" -> "true",
        "value3" -> "false",
        "value4" -> "false",
        "value5" -> "true"
      )
      val form = testForm.bind(data)

      val markup = checkboxHelper(form, choices, question)
      formatHtml(markup) shouldBe formatHtml(expectedMarkup)
    }
  }

  "Calling the checkbox helper with an error" should {

    "render an error" in {
      val data = Map("foo" -> "bar")
      val form = testForm.bind(data).withError(FormError("err", errorMessage))
      val expectedMarkup = Html(
        s"""
           |  <div class="form-field--error">
           |    <fieldset>
           |
           |      <legend>
           |        <h1 id="page-heading" class="heading-large">
           |          $question
           |        </h1>
           |      </legend>
           |
           |      <span class="error-message">$errorMessage</span>
           |      <div>
           |        ${generateExpectedCheckboxMarkup("value1", "display1")}
           |        ${generateExpectedCheckboxMarkup("value2", "display2")}
           |        ${generateExpectedCheckboxMarkup("value3", "display3")}
           |        ${generateExpectedCheckboxMarkup("value4", "display4")}
           |        ${generateExpectedCheckboxMarkup("value5", "display5")}
           |      </div>
           |    </fieldset>
           |  </div>
        """.stripMargin
      )

      val markup = checkboxHelper(form, choices, question)
      formatHtml(markup) shouldBe formatHtml(expectedMarkup)
    }
  }

}
