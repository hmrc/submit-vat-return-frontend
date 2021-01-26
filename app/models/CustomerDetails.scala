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

package models

import play.api.libs.json.{Json, OFormat}
import java.time.LocalDate

case class CustomerDetails(firstName: Option[String],
                           lastName: Option[String],
                           tradingName: Option[String],
                           organisationName: Option[String],
                           hasFlatRateScheme: Boolean = false,
                           isInsolvent: Boolean,
                           continueToTrade: Option[Boolean],
                           insolvencyType: Option[String],
                           insolvencyDate: Option[String]) {

  val isOrg: Boolean = organisationName.isDefined
  val isInd: Boolean = firstName.isDefined || lastName.isDefined
  val userName: Option[String] = {
    val name = s"${firstName.getOrElse("")} ${lastName.getOrElse("")}".trim
    if (name.isEmpty) None else Some(name)
  }
  val businessName: Option[String] = if (isOrg) organisationName else userName
  val clientName: Option[String] = if (tradingName.isDefined) tradingName else businessName

  val isInsolventWithoutAccess: Boolean = continueToTrade match {
    case Some(false) => isInsolvent
    case _ => false
  }

  def insolvencyDateFutureUserBlocked(today: LocalDate): Boolean =
    (isInsolvent, insolvencyType, insolvencyDate, continueToTrade) match {
      case (_, Some(inType), _, _) if Seq("07", "12", "13", "14").contains(inType) => false
      case (true, Some(_), Some(date), Some(true)) if LocalDate.parse(date).isAfter(today) => true
      case _ => false
    }
}


object CustomerDetails {
  implicit val formats: OFormat[CustomerDetails] = Json.format[CustomerDetails]
}
