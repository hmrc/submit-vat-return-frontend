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


import javax.inject.{Inject, Singleton}
import models.SubmitVatReturnModel
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}
import play.api.i18n.MessagesApi

@Singleton
object SubmitVatReturnForm {

  private def splitAndCheckCharacterAmount(value: String): Boolean = {
    val splitArray = value.split('.')
    val headArray = splitArray.head
    val tailArray = splitArray.last
    if ((splitArray.length == 1 && headArray.nonEmpty && headArray.length <= 13) ||
      (splitArray.length == 2 && headArray.nonEmpty && tailArray.nonEmpty && headArray.length <= 13 && tailArray.length <= 2)) {
      true
    } else {
      false
    }
  }

  private val boxFormat: Formatter[BigDecimal] = new Formatter[BigDecimal] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], BigDecimal] = {
      val regex: String = "[-.0-9]+"
      tryOrError({
        () =>
          data.get(key) match {
            case Some(value) if value.isEmpty => Left(Seq(FormError(key, "submit_form.error.emptyError")))
            case Some(value) if !value.matches(regex) => Left(Seq(FormError(key, "submit_form.error.formatCheckError")))
            case Some(value) if !splitAndCheckCharacterAmount(value) => Left(Seq(FormError(key, "submit_form.error.tooManyCharacters")))
            case Some(value) => Right(BigDecimal(value))
            case _ => Left(Seq(FormError(key, "submit_form.error.emptyError")))
          }
      },
        {
          () => Left(Seq(FormError(key, "submit_form.error.emptyError")))
        })
    }

    override def unbind(key: String, value: BigDecimal): Map[String, String] = {
      Map(key -> value.toString)
    }
  }

  private val box3Format: Formatter[BigDecimal] = new Formatter[BigDecimal] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], BigDecimal] = {
      val regex: String = "[-.0-9]+"
      tryOrError({
        () =>
          val firstValue: BigDecimal = returnOrError(data.get("box1"))
          val secondValue: BigDecimal = returnOrError(data.get("box2"))
          data.get("box3") match {
            case Some(value) if value.isEmpty => Left(Seq(FormError(key, "submit_form.error.emptyError")))
            case Some(value) if !value.matches(regex) => Left(Seq(FormError(key, "submit_form.error.formatCheckError")))
            case Some(value) if !splitAndCheckCharacterAmount(value) => Left(Seq(FormError(key, "submit_form.error.tooManyCharacters")))
            case Some(value) if firstValue + secondValue == BigDecimal(value) => Right(BigDecimal(value))
            case _ => Left(Seq(FormError(key, "submit_form.error.box3Error")))
          }
      },
        {
          () => Left(Seq(FormError(key, "submit_form.error.box3Error")))
        })
    }

    override def unbind(key: String, value: BigDecimal): Map[String, String] = {
      Map(key -> value.toString)
    }
  }

  private val box5Format: Formatter[BigDecimal] = new Formatter[BigDecimal] {

    def checkValueForErrors(valueOption: Option[String], key: String): Seq[FormError] = {
      val regex: String = "[-.0-9]+"
      valueOption match {
        case Some(value) => Seq(
          if (value.isEmpty) Some(FormError(key, "submit_form.error.emptyError")) else None,
          if (!value.matches(regex)) Some(FormError(key, "submit_form.error.formatCheckError")) else None,
          if (value.contains("-")) Some(FormError(key, "submit_form.error.negativeError")) else None,
          if (!splitAndCheckCharacterAmount(value)) Some(FormError(key, "submit_form.error.negativeError")) else None
        ).flatten
      }
    }

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], BigDecimal] = {
      val regex: String = "[-.0-9]+"

      tryOrError(
        { () =>
          val firstValue: BigDecimal = returnOrError(data.get("box3"))
          val secondValue: BigDecimal = returnOrError(data.get("box4"))
          val box5Value = data.get("box5")
          val errors = checkValueForErrors(box5Value, key)

          if (errors.nonEmpty) {
            Left(Seq(errors.head))
          } else {
            box5Value match {
              case Some(value) if (firstValue - secondValue).abs == BigDecimal(value) => Right(BigDecimal(value))
              case _ => Left(Seq(FormError(key, "submit_form.error.box5Error")))
            }
          }
        },
        {
          () => Left(Seq(FormError(key, "submit_form.error.box5Error")))
        }
      )
    }

    override def unbind(key: String, value: BigDecimal): Map[String, String] = {
      Map(key -> value.toString)
    }
  }

  private def returnOrError: Option[String] => BigDecimal = {
    case Some(value) => BigDecimal(value)
    case _ => throw new Exception()
  }

  private def tryOrError[T](input: () => T, ifError: () => T): T = {
    try {
      input()
    } catch {
      case _: Throwable =>
        ifError()
    }
  }

  val submitVatReturnForm: Form[SubmitVatReturnModel] = Form(
    mapping(
      "box1" -> of(boxFormat),
      "box2" -> of(boxFormat),
      "box3" -> of(box3Format),
      "box4" -> of(boxFormat),
      "box5" -> of(box5Format),
      "box6" -> of(boxFormat),
      "box7" -> of(boxFormat),
      "box8" -> of(boxFormat),
      "box9" -> of(boxFormat),
      "flatRateScheme" -> boolean,
      "start" -> localDate,
      "end" -> localDate,
      "due" -> localDate
    )(SubmitVatReturnModel.apply)(SubmitVatReturnModel.unapply)
  )
}
