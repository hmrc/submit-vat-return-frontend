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

package audit.models

import java.time.LocalDate

import base.BaseSpec
import play.api.libs.json.Json

class NrsErrorAuditModelSpec extends BaseSpec {

  val transactionName = "submit-vat-to-nrs"
  val auditEvent = "SubmitVATToNRSError"

  "NrsErrorAuditModel" when {

    "user is not an agent" should {

      val model = NrsErrorAuditModel(
        vrn = "123456789",
        isAgent = false,
        agentReferenceNumber = None,
        periodDateFrom = LocalDate.parse("2019-01-01"),
        periodDateTo = LocalDate.parse("2019-01-02"),
        dueDate = LocalDate.parse("2019-01-03"),
        status = "500"
      )

      s"have the correct transaction name of '$transactionName'" in {
        model.transactionName shouldBe transactionName
      }

      s"have the correct audit event type of '$auditEvent'" in {
        model.auditType shouldBe auditEvent
      }

      "have the correct detail for the audit event" in {
        model.detail shouldBe Json.obj(
          "vrn" -> "123456789",
          "isAgent" -> false,
          "periodDateFrom" -> "2019-01-01",
          "periodDateTo" -> "2019-01-02",
          "dueDate" -> "2019-01-03",
          "status" -> "500"
        )
      }
    }

    "user is an agent" should {

      val model = NrsErrorAuditModel(
        vrn = "123456789",
        isAgent = true,
        agentReferenceNumber = Some("XARN1234567"),
        periodDateFrom = LocalDate.parse("2019-01-01"),
        periodDateTo = LocalDate.parse("2019-01-02"),
        dueDate = LocalDate.parse("2019-01-03"),
        status = "500"
      )

      "have the correct detail for the audit event" in {
        model.detail shouldBe Json.obj(
          "vrn" -> "123456789",
          "isAgent" -> true,
          "agentReferenceNumber" -> "XARN1234567",
          "periodDateFrom" -> "2019-01-01",
          "periodDateTo" -> "2019-01-02",
          "dueDate" -> "2019-01-03",
          "status" -> "500"
        )
      }
    }
  }
}
