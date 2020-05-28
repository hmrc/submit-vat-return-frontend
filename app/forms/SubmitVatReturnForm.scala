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

package forms

import forms.Constraints._
import javax.inject.Singleton
import models.NineBoxModel
import play.api.data.Forms._
import play.api.data.validation.Constraint
import play.api.data.{Form, FormError, Mapping}
import play.api.i18n.Messages

@Singleton
case class SubmitVatReturnForm (implicit messages: Messages){

  val minDecimalValue: BigDecimal   = -9999999999999.99
  val minNoDecimalValue: BigDecimal = -9999999999999.00
  val maxDecimalValue: BigDecimal   = 9999999999999.99
  val maxNoDecimalValue: BigDecimal = 9999999999999.00
  val minBox5Value: BigDecimal      = 0.00
  val maxBox5Value: BigDecimal      = 99999999999.99

  private def toBigDecimal(boxId : Int): String => BigDecimal = (text: String) => BigDecimal.apply(text)
  private def fromBigDecimal(boxId : Int): BigDecimal => String = (bd: BigDecimal) => bd.toString()
  private def validNumber(boxId : Int): Constraint[String] = validBigDecimal(messages("submit_form.error.emptyError", boxId),  messages("submit_form.error.formatCheckError", boxId))

  private def box1To4Validation(boxId: Int): (Mapping[BigDecimal]) = {
    text.verifying(validNumber(boxId))
      .transform(toBigDecimal(boxId), fromBigDecimal(boxId))
      .verifying(
        max(maxDecimalValue, messages("submit_form.error.tooManyCharacters", boxId)),
        min(minDecimalValue, messages("submit_form.error.tooManyCharacters", boxId)),
        twoDecimalPlaces(messages("submit_form.error.tooManyCharacters", boxId))
      )
  }

  private def box6To9Validation(boxId: Int): (Mapping[BigDecimal]) = {
    text.verifying(validNumber(boxId))
      .transform(toBigDecimal(boxId), fromBigDecimal(boxId))
      .verifying(
        max(maxNoDecimalValue, messages("submit_form.error.tooManyCharactersNoDecimal", boxId)),
        min(minNoDecimalValue, messages("submit_form.error.tooManyCharactersNoDecimal", boxId)),
        noDecimalPlaces(messages("submit_form.error.tooManyCharactersNoDecimal", boxId))
      )
  }

  val nineBoxForm: Form[NineBoxModel] = Form(
    mapping(
      "box1" -> box1To4Validation(1),
      "box2" -> box1To4Validation(2),
      "box4" -> box1To4Validation(4),
      "box6" -> box6To9Validation(6),
      "box7" -> box6To9Validation(7),
      "box8" -> box6To9Validation(8),
      "box9" -> box6To9Validation(9)
    )(NineBoxModel.apply)(NineBoxModel.unapply)
  )

}
