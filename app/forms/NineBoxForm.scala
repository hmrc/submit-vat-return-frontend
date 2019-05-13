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

import models.NineBoxModel
import play.api.data.Forms._
import play.api.data.{Form, Mapping}

object NineBoxForm {

  def sequentialMappings[T, O](applyConstraints: Seq[Mapping[T] => Mapping[T]], finalTransform: Mapping[T] => Mapping[O]): Mapping[T] => Mapping[O] = {
    originalMap =>
      finalTransform(
        applyConstraints.foldRight(originalMap) { (newConstraint, passedForward) =>
          newConstraint(passedForward)
        }
      )
  }

  def nonEmpty: Mapping[String] => Mapping[String] = { input =>
    input.verifying(
      "Enter a maximum of 13 decimal places for pounds.\nEnter a maximum of 2 decimal places for pence.\nYou can use a negative amount eg -13.2",
      value => value.nonEmpty
    )
  }

  def validCharacters: Mapping[String] => Mapping[String] = { input =>
    val regexCheck = "[0-9]{1,13}|[0-9]{1,13}\\.[0-9]{1,2}"

    input.verifying(
      "Enter a number in the format 0.00",
      value => value.matches(regexCheck)
    )
  }

  def nonNegative: Mapping[String] => Mapping[String] = { input =>
    input.verifying(
      "Enter a maximum of 13 decimal places for pounds.\nEnter a maximum of 2 decimal places for pence.\nDo not use a negative amount eg -13.2",
      value => !value.contains("-")
    )
  }

  def toBigDecimal: Mapping[String] => Mapping[BigDecimal] = { input =>
    input.transform[BigDecimal](BigDecimal(_), _.toString())
  }

  val nineBoxForm = Form(
    mapping(
      "box1" -> sequentialMappings(Seq(nonEmpty, validCharacters), toBigDecimal)(text),
      "box2" -> sequentialMappings(Seq(nonEmpty, validCharacters), toBigDecimal)(text),
      "box3" -> sequentialMappings(Seq(nonEmpty, validCharacters), toBigDecimal)(text),
      "box4" -> sequentialMappings(Seq(nonEmpty, validCharacters), toBigDecimal)(text),
      "box5" -> sequentialMappings(Seq(nonEmpty, validCharacters, nonNegative), toBigDecimal)(text),
      "box6" -> sequentialMappings(Seq(nonEmpty, validCharacters), toBigDecimal)(text),
      "box7" -> sequentialMappings(Seq(nonEmpty, validCharacters), toBigDecimal)(text),
      "box8" -> sequentialMappings(Seq(nonEmpty, validCharacters), toBigDecimal)(text),
      "box9" -> sequentialMappings(Seq(nonEmpty, validCharacters), toBigDecimal)(text)
    )(NineBoxModel.apply)(NineBoxModel.unapply)
  )

}
