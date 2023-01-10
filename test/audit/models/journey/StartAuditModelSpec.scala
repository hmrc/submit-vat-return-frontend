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

import base.BaseSpec
import play.api.libs.json.Json

class StartAuditModelSpec extends BaseSpec {

  val transactionName = "journey-start"
  val auditEvent = "SubmitVATReturnJourneyStart"

  "JourneyStartAuditModel" when {

    "user is not an agent" should {

      val model = StartAuditModel("123456789", "19AA", None)

      s"have the correct transaction name of '$transactionName'" in {
        model.transactionName shouldBe transactionName
      }

      s"have the correct audit event type of '$auditEvent'" in {
        model.auditType shouldBe auditEvent
      }

      "have the correct detail for the audit event" in {
        model.detail shouldBe Json.obj(
          "vrn" -> "123456789",
          "periodKey" -> "19AA"
        )
      }
    }

    "user is an agent" should {

      val model = StartAuditModel("123456789", "19AA", Some("XARN1234567"))

      "have the correct detail for the audit event" in {
        model.detail shouldBe Json.obj(
          "vrn" -> "123456789",
          "periodKey" -> "19AA",
          "agentReferenceNumber" -> "XARN1234567"
        )
      }
    }
  }
}
