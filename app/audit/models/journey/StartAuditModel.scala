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

package audit.models.journey

import audit.models.ExtendedAuditModel
import play.api.libs.json.{Format, JsValue, Json}
import utils.JsonObjectSugar

case class StartAuditModel(vrn: String,
                           periodKey: String,
                           startDate: String,
                           endDate: String,
                           agentReferenceNumber: Option[String]) extends ExtendedAuditModel with JsonObjectSugar {

  override val transactionName: String = "journey-start"
  override val auditType: String = "SubmitVATReturnJourneyStart"
  override val detail: JsValue = jsonObjNoNulls(
    "vrn" -> vrn,
    "periodKey" -> periodKey,
    "periodStartDate" -> startDate,
    "periodEndDate" -> endDate,
    "agentReferenceNumber" -> agentReferenceNumber,
    "isAgent" -> agentReferenceNumber.isDefined
  )
}

object StartAuditModel {
  implicit val format: Format[StartAuditModel] = Json.format[StartAuditModel]
}
