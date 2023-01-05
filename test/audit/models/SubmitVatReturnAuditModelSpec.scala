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
      BigDecimal(2),
      BigDecimal(3),
      BigDecimal(4),
      BigDecimal(5),
      BigDecimal(6),
      BigDecimal(7),
      BigDecimal(8),
      BigDecimal(9),
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
        "periodDateFrom" -> "2019-01-31",
        "periodDateTo" -> "2019-04-30",
        "dueDate" -> "2019-05-31",
        "periodKey" -> periodKey,
        "vatDueSales" -> 1,
        "vatDueAcquisitions" -> 2,
        "vatDueTotal" -> 3,
        "vatReclaimedCurrPeriod" -> 4,
        "vatDueNet" -> 5,
        "totalValueSalesExVAT" -> 6,
        "totalValuePurchasesExVAT" -> 7,
        "totalValueGoodsSuppliedExVAT" -> 8,
        "totalAllAcquisitionsExVAT" -> 9
      )
    }
  }

}
