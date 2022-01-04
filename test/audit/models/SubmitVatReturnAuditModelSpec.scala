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

package audit.models

import java.time.LocalDate

import base.BaseSpec
import models.SubmitVatReturnModel
import play.api.libs.json.Json

class SubmitVatReturnAuditModelSpec extends BaseSpec {

  val transactionName = "submit-vat-return"
  val auditEvent = "SubmitVATReturn"

  "The SubmitVatReturnAuditModelSpec" should {

    val model: SubmitVatReturnModel = SubmitVatReturnModel(
      BigDecimal(1),
      BigDecimal(1),
      BigDecimal(1),
      BigDecimal(1),
      BigDecimal(1),
      BigDecimal(1),
      BigDecimal(1),
      BigDecimal(1),
      BigDecimal(1),
      true,
      LocalDate.parse("2019-01-31"),
      LocalDate.parse("2019-04-30"),
      LocalDate.parse("2019-05-31")
    )

    val periodKey = "19AA"

    lazy val testSubmitVatReturnAuditModelSpec = SubmitVatReturnAuditModel(agentUser, model, periodKey)

    s"Have the correct transaction name of '$transactionName'" in {
      testSubmitVatReturnAuditModelSpec.transactionName shouldBe transactionName
    }

    s"Have the correct audit event type of '$auditEvent'" in {
      testSubmitVatReturnAuditModelSpec.auditType shouldBe auditEvent
    }

    "Have the correct details for the audit event" in {
      testSubmitVatReturnAuditModelSpec.detail shouldBe Json.obj(
        "isAgent" -> true,
        "agentReferenceNumber" -> arn,
        "vrn" -> vrn,
        "periodDateFrom" -> model.start,
        "periodDateTo" -> model.end,
        "dueDate" -> model.due,
        "periodKey" -> periodKey,
        "vatDueSales" -> model.box1,
        "vatDueAcquisitions" -> model.box2,
        "vatDueTotal" -> model.box3,
        "vatReclaimedCurrPeriod" -> model.box4,
        "vatDueNet" -> model.box5,
        "totalValueSalesExVAT" -> model.box6,
        "totalValuePurchasesExVAT" -> model.box7,
        "totalValueGoodsSuppliedExVAT" -> model.box8,
        "totalAllAcquisitionsExVAT" -> model.box9
      )
    }
  }

}
