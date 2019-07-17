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
import models.NineBoxModel
import play.api.data.Forms._
import play.api.data.validation.Constraint
import play.api.data.{Form, FormError, Mapping}

@Singleton
object NineBoxForm {

  val minDecimalValue: BigDecimal   = -9999999999999.99
  val minNoDecimalValue: BigDecimal = -9999999999999.00
  val maxDecimalValue: BigDecimal   = 9999999999999.99
  val maxNoDecimalValue: BigDecimal = 9999999999999.00
  val minBox5Value: BigDecimal      = 0.00
  val maxBox5Value: BigDecimal      = 99999999999.99

  private def toBigDecimal: String => BigDecimal = (text: String) => BigDecimal.apply(text)
  private def fromBigDecimal: BigDecimal => String = (bd: BigDecimal) => bd.toString()
  private def validNumber: Constraint[String] = validBigDecimal("submit_form.error.emptyError", "submit_form.error.formatCheckError")

  private def box1To4Validation: (Mapping[BigDecimal]) = {
    text.verifying(validNumber)
      .transform(toBigDecimal, fromBigDecimal)
      .verifying(
        max(maxDecimalValue, "submit_form.error.tooManyCharacters"),
        min(minDecimalValue, "submit_form.error.tooManyCharacters"),
        twoDecimalPlaces("submit_form.error.tooManyCharacters")
      )
  }

  private def box5Validation: (Mapping[BigDecimal]) = {
    text.verifying(validNumber)
      .transform(toBigDecimal, fromBigDecimal)
      .verifying(
        max(maxBox5Value, "submit_form.error.negativeError"),
        min(minBox5Value, "submit_form.error.negativeError"),
        twoDecimalPlaces("submit_form.error.negativeError")
      )
  }

  private def box6To9Validation: (Mapping[BigDecimal]) = {
    text.verifying(validNumber)
      .transform(toBigDecimal, fromBigDecimal)
      .verifying(
        max(maxNoDecimalValue, "submit_form.error.tooManyCharactersNoDecimal"),
        min(minNoDecimalValue, "submit_form.error.tooManyCharactersNoDecimal"),
        noDecimalPlaces("submit_form.error.tooManyCharactersNoDecimal")
      )
  }

  private def validateBox3Calculation(box1: BigDecimal, box2: BigDecimal, box3: BigDecimal): Option[FormError] = {
    if(box1 + box2 == box3) None else Some(FormError("box3", "submit_form.error.box3Error"))
  }

  private def validateBox5Calculation(box3: BigDecimal, box4: BigDecimal, box5: BigDecimal): Option[FormError] = {
    if((box3 - box4).abs == box5) None else Some(FormError("box5", "submit_form.error.box5Error"))
  }

  val nineBoxForm: Form[NineBoxModel] = Form(
    mapping(
      "box1" -> box1To4Validation,
      "box2" -> box1To4Validation,
      "box3" -> box1To4Validation,
      "box4" -> box1To4Validation,
      "box5" -> box5Validation,
      "box6" -> box6To9Validation,
      "box7" -> box6To9Validation,
      "box8" -> box6To9Validation,
      "box9" -> box6To9Validation
    )(NineBoxModel.apply)(NineBoxModel.unapply)
  )

  def validateBoxCalculations(form: Form[NineBoxModel]): Form[NineBoxModel] = {
    if(form.hasErrors) {
      form
    } else {
      form.value match {
        case Some(model) =>
          val formErrors: Seq[FormError] = Seq(
            validateBox3Calculation(model.box1, model.box2, model.box3) ++
            validateBox5Calculation(model.box3, model.box4, model.box5)
          ).flatten

          form.copy(mapping = form.mapping, data = form.data, errors = form.errors ++ formErrors, value = form.value)
        case None => form
      }
    }
  }
}
