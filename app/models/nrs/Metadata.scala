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

package models.nrs

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}

import play.api.libs.json._

case class Metadata(businessId: String = "vat-ui",
                    notableEvent: String = "vat-return-ui",
                    payloadContentType: String = "text/html",
                    payloadSha256Checksum: String,
                    userSubmissionTimestamp: LocalDateTime,
                    identityData: IdentityData,
                    userAuthToken: String,
                    headerData: Map[String, String],
                    searchKeys: SearchKeys,
                    receiptData: ReceiptData)

object Metadata {
  implicit val dateToString: Writes[LocalDateTime] = Writes { date =>
    Json.toJson(date.format(
      DateTimeFormatter
        .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .withZone(ZoneId.of("UTC"))
    ))
  }

  implicit val formats: OFormat[Metadata] = Json.format[Metadata]
}
