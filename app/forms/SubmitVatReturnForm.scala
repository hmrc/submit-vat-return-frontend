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

package forms

import forms.Constraints._
import javax.inject.Singleton
import models.SubmitVatReturnModel
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraint

@Singleton
object SubmitVatReturnForm {

  private val minDecimalValue: BigDecimal   = -9999999999999.99
  private val minNoDecimalValue: BigDecimal = -9999999999999.00
  private val maxDecimalValue: BigDecimal   = 9999999999999.99
  private val maxNoDecimalValue: BigDecimal = 9999999999999.00
  private val minBox5Value: BigDecimal      = 0.00
  private val maxBox5Value: BigDecimal      = 99999999999.99

  private def toBigDecimal: String => BigDecimal = (text: String) => BigDecimal.apply(text)
  private def fromBigDecimal: BigDecimal => String = (bd: BigDecimal) => bd.toString()
  private def validNumber: Constraint[String] = validBigDecimal("submit_form.error.emptyError", "submit_form.error.formatCheckError")

  val submitVatReturnForm: Form[SubmitVatReturnModel] = Form(
    mapping(
      "box1" ->
        text.verifying(validNumber)
        .transform(toBigDecimal, fromBigDecimal)
        .verifying(
          max(maxDecimalValue, "submit_form.error.tooManyCharacters"),
          min(minDecimalValue, "submit_form.error.tooManyCharacters"),
          twoDecimalPlaces("submit_form.error.tooManyCharacters")
        ),
      "box2" ->
        text.verifying(validNumber)
        .transform(toBigDecimal, fromBigDecimal)
        .verifying(
          max(maxDecimalValue, "submit_form.error.tooManyCharacters"),
          min(minDecimalValue, "submit_form.error.tooManyCharacters"),
          twoDecimalPlaces("submit_form.error.tooManyCharacters")
        ),
      "box3" ->
        text.verifying(validNumber)
        .transform(toBigDecimal, fromBigDecimal)
        .verifying(
          max(maxDecimalValue, "submit_form.error.tooManyCharacters"),
          min(minDecimalValue, "submit_form.error.tooManyCharacters"),
          twoDecimalPlaces("submit_form.error.tooManyCharacters")
        ),
      "box4" ->
        text.verifying(validNumber)
        .transform(toBigDecimal, fromBigDecimal)
        .verifying(
          max(maxDecimalValue, "submit_form.error.tooManyCharacters"),
          min(minDecimalValue, "submit_form.error.tooManyCharacters"),
          twoDecimalPlaces("submit_form.error.tooManyCharacters")
        ),
      "box5" ->
        text.verifying(validNumber)
        .transform(toBigDecimal, fromBigDecimal)
        .verifying(
          max(maxBox5Value, "submit_form.error.negativeError"),
          min(minBox5Value, "submit_form.error.negativeError"),
          twoDecimalPlaces("submit_form.error.negativeError")
        ),
      "box6" ->
        text.verifying(validNumber)
        .transform(toBigDecimal, fromBigDecimal)
        .verifying(
          max(maxNoDecimalValue, "submit_form.error.tooManyCharactersNoDecimal"),
          min(minNoDecimalValue, "submit_form.error.tooManyCharactersNoDecimal"),
          noDecimalPlaces("submit_form.error.tooManyCharactersNoDecimal")
        ),
      "box7" ->
        text.verifying(validNumber)
        .transform(toBigDecimal, fromBigDecimal)
        .verifying(
          max(maxNoDecimalValue, "submit_form.error.tooManyCharactersNoDecimal"),
          min(minNoDecimalValue, "submit_form.error.tooManyCharactersNoDecimal"),
          noDecimalPlaces("submit_form.error.tooManyCharactersNoDecimal")
        ),
      "box8" ->
        text.verifying(validNumber)
        .transform(toBigDecimal, fromBigDecimal)
        .verifying(
          max(maxNoDecimalValue, "submit_form.error.tooManyCharactersNoDecimal"),
          min(minNoDecimalValue, "submit_form.error.tooManyCharactersNoDecimal"),
          noDecimalPlaces("submit_form.error.tooManyCharactersNoDecimal")
        ),
      "box9" ->
        text.verifying(validNumber)
        .transform(toBigDecimal, fromBigDecimal)
        .verifying(
          max(maxNoDecimalValue, "submit_form.error.tooManyCharactersNoDecimal"),
          min(minNoDecimalValue, "submit_form.error.tooManyCharactersNoDecimal"),
          noDecimalPlaces("submit_form.error.tooManyCharactersNoDecimal")
        ),
      "flatRateScheme" -> boolean,
      "start" -> localDate,
      "end" -> localDate,
      "due" -> localDate
    )(SubmitVatReturnModel.apply)(SubmitVatReturnModel.unapply)
  )
}
