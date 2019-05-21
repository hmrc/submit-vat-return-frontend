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
import models.NineBoxModel
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{Form, FormError, Mapping}
import play.api.i18n.MessagesApi

@Singleton
class NineBoxForm @Inject()(implicit messagesApi: MessagesApi) {

  def sequentialMappings[T, O](applyConstraints: Seq[Mapping[T] => Mapping[T]], finalTransform: Mapping[T] => Mapping[O]): Mapping[T] => Mapping[O] = {
    originalMap =>
      finalTransform(
        applyConstraints.foldLeft(originalMap) { (passedForward, newConstraint) =>
          newConstraint(passedForward)
        }
      )
  }

  def nonEmpty: Mapping[String] => Mapping[String] = { input =>
    input.verifying(
      messagesApi("submit_form.error.emptyError"),
      value => value.nonEmpty
    )
  }

  def validCharacters: Mapping[String] => Mapping[String] = { input =>
    val regexCheck = "[-.0-9]+"

    input.verifying(
      messagesApi("submit_form.error.formatCheckError"),
      value => value.matches(regexCheck)
    )
  }

  private def splitAndCheckCharacterAmount(value: String): Boolean = {
    val splitArray = value.split('.')
    val headArray = splitArray.head
    val tailArray = splitArray.last
    if ((splitArray.length == 1 && splitArray.head.length <= 13) ||
      (splitArray.length == 2 && headArray.nonEmpty && tailArray.nonEmpty && headArray.length < 13 && tailArray.length < 2)) {
      true
    } else {
      false
    }
  }

  def characterAmountCheck: Mapping[String] => Mapping[String] = { input =>
    input.verifying(
      messagesApi("submit_form.error.emptyError"),
      value =>
        try {
          splitAndCheckCharacterAmount(value)
        } catch {
          case _: Throwable => false
        }
    )
  }

  def toBigDecimal: Mapping[String] => Mapping[BigDecimal] = { input =>
    input.transform[BigDecimal](BigDecimal(_), _.toString())
  }

  private val box3Format: Formatter[BigDecimal] = new Formatter[BigDecimal] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], BigDecimal] = {
      val regex: String = "[-.0-9]+"
      tryOrError({
        () =>
          val firstValue: BigDecimal = returnOrError(data.get("box1"))
          val secondValue: BigDecimal = returnOrError(data.get("box2"))
          data.get("box3") match {
            case Some(value) if value.isEmpty => Left(Seq(FormError(key, messagesApi("submit_form.error.emptyError"))))
            case Some(value) if !value.matches(regex) => Left(Seq(FormError(key, messagesApi("submit_form.error.formatCheckError"))))
            case Some(value) if !splitAndCheckCharacterAmount(value) => Left(Seq(FormError(key, messagesApi("submit_form.error.emptyError"))))
            case Some(value) if firstValue + secondValue == BigDecimal(value) => Right(BigDecimal(value))
            case _ => Left(Seq(FormError(key, messagesApi("submit_form.error.box3Error"))))
          }
      },
        {
          () => Left(Seq(FormError(key, messagesApi("submit_form.error.box3Error"))))
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
          if (value.isEmpty) Some(FormError(key, messagesApi("submit_form.error.negativeError"))) else None,
          if (!value.matches(regex)) Some(FormError(key, messagesApi("submit_form.error.formatCheckError"))) else None,
          if (value.contains("-")) Some(FormError(key, messagesApi("submit_form.error.negativeError"))) else None,
          if (!splitAndCheckCharacterAmount(value)) Some(FormError(key, messagesApi("submit_form.error.negativeError"))) else None
        ).flatten
        case _ => Seq(FormError(key, messagesApi("submit_form.error.box5Error")))
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
            Left(errors)
          } else {
            box5Value match {
              case Some(value) if (firstValue - secondValue).abs == BigDecimal(value) => Right(BigDecimal(value))
              case _ => Left(Seq(FormError(key, messagesApi("submit_form.error.box5Error"))))
            }
          }
        },
        {
          () => Left(Seq(FormError(key, messagesApi("submit_form.error.box5Error"))))
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

  val nineBoxForm = Form(
    mapping(
      "box1" -> sequentialMappings(Seq(nonEmpty, validCharacters, characterAmountCheck), toBigDecimal)(text),
      "box2" -> sequentialMappings(Seq(nonEmpty, validCharacters, characterAmountCheck), toBigDecimal)(text),
      "box3" -> of(box3Format),
      "box4" -> sequentialMappings(Seq(nonEmpty, validCharacters, characterAmountCheck), toBigDecimal)(text),
      "box5" -> of(box5Format),
      "box6" -> sequentialMappings(Seq(nonEmpty, validCharacters, characterAmountCheck), toBigDecimal)(text),
      "box7" -> sequentialMappings(Seq(nonEmpty, validCharacters, characterAmountCheck), toBigDecimal)(text),
      "box8" -> sequentialMappings(Seq(nonEmpty, validCharacters, characterAmountCheck), toBigDecimal)(text),
      "box9" -> sequentialMappings(Seq(nonEmpty, validCharacters, characterAmountCheck), toBigDecimal)(text)
    )(NineBoxModel.apply)(NineBoxModel.unapply)
  )

}
