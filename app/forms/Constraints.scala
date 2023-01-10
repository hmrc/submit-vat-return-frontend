/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.data.validation.{Constraint, Invalid, Valid}
import scala.util.{Failure, Success, Try}
import utils.StripCharUtil._

object Constraints {

  def twoDecimalPlaces(message: String): Constraint[BigDecimal] = Constraint[BigDecimal]("twoDecimalPlaces") {
    case i if i.scale <= 2 => Valid
    case _ => Invalid(message)
  }

  def noDecimalPlaces(message: String): Constraint[BigDecimal] = Constraint[BigDecimal]("noDecimalPlaces") {
    case i if i.bigDecimal.stripTrailingZeros().scale <= 0 => Valid
    case _ => Invalid(message)
  }

  def max(maxValue: BigDecimal, message: String): Constraint[BigDecimal] = Constraint[BigDecimal]("max") {
    case i if i <= maxValue => Valid
    case _ => Invalid(message)
  }

  def min(minValue: BigDecimal, message: String): Constraint[BigDecimal] = Constraint[BigDecimal]("min") {
    case i if i >= minValue => Valid
    case _ => Invalid(message)
  }

  def validBigDecimal(emptyMessage: String, invalidMessage: String): Constraint[String] = Constraint[String]("validBigDecimal") { number =>
    if(number != "") {
      Try(BigDecimal(stripAll(number))) match {
        case Success(_) => Valid
        case Failure(_) => Invalid(invalidMessage)
      }
    } else {
      Invalid(emptyMessage)
    }
  }
}
