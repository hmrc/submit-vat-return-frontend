/*
 * Copyright 2022 HM Revenue & Customs
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

package models

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}

import play.api.libs.json._

import scala.util.Try

object RestFormats extends RestFormats

trait RestFormats {

  private val localDateRegex = """^(\d\d\d\d)-(\d\d)-(\d\d)$""".r

  implicit val dateTimeRead: Reads[LocalDateTime] = {
    case JsString(s) =>
      Try {
        JsSuccess(LocalDateTime.parse(s))
      }.getOrElse {
        JsError(s"Could not parse $s as a DateTime with format ${DateTimeFormatter.ISO_LOCAL_DATE_TIME.toString}")
      }
    case json => JsError(s"Expected value to be a string, was actually $json")
  }

  implicit val dateTimeWrite: Writes[LocalDateTime] = dateTime => JsString(dateTime.toString)

  implicit val localDateRead: Reads[LocalDate] = {
    case JsString(s@localDateRegex(y, m, d)) =>
      Try {
        JsSuccess(LocalDate.of(y.toInt, m.toInt, d.toInt))
      }.getOrElse {
        JsError(s"$s is not a valid date")
      }
    case JsString(s) => JsError(s"Cannot parse $s as a LocalDate")
    case json => JsError(s"Expected value to be a string, was actually $json")
  }

  implicit val localDateWrite: Writes[LocalDate] = date =>
    JsString("%04d-%02d-%02d".format(date.getYear, date.getMonth.getValue, date.getDayOfMonth))

  implicit val dateTimeFormats  = Format(dateTimeRead, dateTimeWrite)
  implicit val localDateFormats = Format(localDateRead, localDateWrite)

}
