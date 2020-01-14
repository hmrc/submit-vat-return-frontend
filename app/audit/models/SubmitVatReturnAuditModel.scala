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

package audit.models

import models.SubmitVatReturnModel
import models.auth.User
import play.api.libs.json._
import utils.JsonObjectSugar

case class SubmitVatReturnAuditModel(user: User[_], submissionDetails: SubmitVatReturnModel, periodKey: String) extends ExtendedAuditModel {

  override val transactionName: String = "submit-vat-return"
  override val auditType: String = "SubmitVATReturn"
  override val detail: JsValue = Json.toJson(this)

}

object SubmitVatReturnAuditModel extends JsonObjectSugar {

  implicit val writes: Writes[SubmitVatReturnAuditModel] = Writes { model =>
    jsonObjNoNulls(
      "isAgent" -> model.user.arn.isDefined,
      "agentReferenceNumber" -> model.user.arn,
      "vrn" -> model.user.vrn,
      "periodDateFrom" -> model.submissionDetails.start,
      "periodDateTo" -> model.submissionDetails.end,
      "dueDate" -> model.submissionDetails.due,
      "periodKey" -> model.periodKey,
      "vatDueSales" -> model.submissionDetails.box1,
      "vatDueAcquisitions" -> model.submissionDetails.box2,
      "vatDueTotal" -> model.submissionDetails.box3,
      "vatReclaimedCurrPeriod" -> model.submissionDetails.box4,
      "vatDueNet" -> model.submissionDetails.box5,
      "totalValueSalesExVAT" -> model.submissionDetails.box6,
      "totalValuePurchasesExVAT" -> model.submissionDetails.box7,
      "totalValueGoodsSuppliedExVAT" -> model.submissionDetails.box8,
      "totalAllAcquisitionsExVAT" -> model.submissionDetails.box9
    )
  }
}
