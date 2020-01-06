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

package utils

import java.time.{Instant, LocalDateTime, ZoneId}

import models.nrs._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.auth.core.{Admin, ConfidenceLevel}

object NrsTestData {

  object IdentityDataTestData {

    import org.joda.time.LocalDate

    val expectedJson: JsValue = Json.parse(
      """{
        |  "internalId": "some-id",
        |  "externalId": "some-id",
        |  "agentCode": "TZRXXV",
        |  "credentials": {
        |    "providerId": "12345-credId",
        |    "providerType": "GovernmentGateway"
        |  },
        |  "confidenceLevel": 200,
        |  "nino": "DH00475D",
        |  "saUtr": "Utr",
        |  "name": {
        |    "name": "test",
        |    "lastName": "test"
        |  },
        |  "dateOfBirth": "1985-01-01",
        |  "email":"test@test.com",
        |  "agentInformation": {
        |    "agentId": "BDGL",
        |    "agentCode" : "TZRXXV",
        |    "agentFriendlyName" : "Bodgitt & Legget LLP"
        |  },
        |  "groupIdentifier" : "GroupId",
        |  "credentialRole": "Admin",
        |  "mdtpInformation" : {
        |    "deviceId" : "DeviceId",
        |    "sessionId": "SessionId"
        |  },
        |  "itmpName" : {
        |    "givenName": "test",
        |    "middleName": "test",
        |    "familyName": "test"
        |  },
        |  "itmpDateOfBirth" : "1985-01-01",
        |  "itmpAddress" : {
        |    "line1": "Line 1",
        |    "postCode": "NW94HD",
        |    "countryName": "United Kingdom",
        |    "countryCode": "UK"
        |  },
        |  "affinityGroup": "Agent",
        |  "credentialStrength": "strong",
        |  "loginTimes": {
        |    "currentLogin": "2016-11-27T09:00:00.000Z",
        |    "previousLogin": "2016-11-01T12:00:00.000Z"
        |  }
        |}
      """.stripMargin)

    val requestModel: IdentityData = IdentityData(
      internalId = Some("some-id"),
      externalId = Some("some-id"),
      agentCode = Some("TZRXXV"),
      credentials = Some(Credentials("12345-credId", "GovernmentGateway")),
      confidenceLevel = ConfidenceLevel.L200,
      nino = Some("DH00475D"),
      saUtr = Some("Utr"),
      name = Some(Name(Some("test"), Some("test"))),
      dateOfBirth = Some(LocalDate.parse("1985-01-01")),
      email = Some("test@test.com"),
      agentInformation = AgentInformation(agentCode = Some("TZRXXV"), agentFriendlyName = Some("Bodgitt & Legget LLP"), agentId = Some("BDGL")),
      groupIdentifier = Some("GroupId"),
      credentialRole = Some(Admin),
      mdtpInformation = Some(MdtpInformation("DeviceId", "SessionId")),
      itmpName = ItmpName(Some("test"), Some("test"), Some("test")),
      itmpDateOfBirth = Some(LocalDate.parse("1985-01-01")),
      itmpAddress = ItmpAddress(Some("Line 1"), None, None, None, None, Some("NW94HD"), Some("United Kingdom"), Some("UK")),
      affinityGroup = Some(Agent),
      credentialStrength = Some("strong"),
      loginTimes = IdentityLoginTimes(
        LocalDateTime.ofInstant(Instant.parse("2016-11-27T09:00:00.000Z"), ZoneId.of("UTC")),
        Some(LocalDateTime.ofInstant(Instant.parse("2016-11-01T12:00:00.000Z"), ZoneId.of("UTC")))
      )
    )
  }

  object FullRequestTestData {

    val expectedJson: JsValue = Json.parse(
      s"""
         | {
         |   "payload" : "XXX-base64-CheckYourAnswersHTML-XXX",
         |   "metadata" : {
         |     "businessId": "vat-ui",
         |     "notableEvent": "vat-return-ui",
         |     "payloadContentType": "text/html",
         |     "payloadSha256Checksum": "426a1c28<snip>d6d363",
         |     "userSubmissionTimestamp": "2018-04-07T12:13:25.156Z",
         |     "identityData": ${IdentityDataTestData.expectedJson},
         |     "userAuthToken": "Bearer AbCdEf123456...",
         |     "headerData": { "...":"..." },
         |     "searchKeys": {
         |       "vrn": "123456789",
         |       "periodKey": "18AA"
         |     },
         |     "receiptData": {
         |       "language": "en",
         |       "checkYourAnswersSections": [
         |         {
         |           "title": "VAT details",
         |           "data": [
         |             {
         |               "questionId":"fooVatDetails1",
         |               "question": "VAT taxable sales ...",
         |               "answer": "Yes"
         |             }
         |           ]
         |         }
         |      ],
         |      "declaration": {
         |        "declarationText": "I confirm the data ....",
         |        "declarationName": "John Smith",
         |        "declarationRole": "Finance Director",
         |        "declarationConsent": true
         |      }
         |    }
         |  }
         | }
      """.stripMargin
    )

    val requestModel: RequestModel = RequestModel(
      payload = "XXX-base64-CheckYourAnswersHTML-XXX",
      metadata = Metadata(
        payloadSha256Checksum = "426a1c28<snip>d6d363",
        userSubmissionTimestamp = LocalDateTime.ofInstant(Instant.parse("2018-04-07T12:13:25.156Z"), ZoneId.of("UTC")),
        identityData = IdentityDataTestData.requestModel,
        userAuthToken = "Bearer AbCdEf123456...",
        headerData = Map("..." -> "..."),
        searchKeys = SearchKeys("123456789", "18AA"),
        receiptData = ReceiptData(
          language = EN,
          checkYourAnswersSections = Seq(
            Answers(
              title = "VAT details",
              data = Seq(
                Answer(
                  questionId = "fooVatDetails1",
                  question = "VAT taxable sales ...",
                  answer = Some("Yes")
                )
              )
            )
          ),
          declaration = Declaration(
            declarationText = "I confirm the data ....",
            declarationName = "John Smith",
            declarationRole = Some("Finance Director"),
            declarationConsent = true
          )
        )
      )
    )
  }

}
