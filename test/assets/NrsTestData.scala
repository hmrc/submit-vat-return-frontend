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

package assets

import java.time.{Instant, LocalDateTime, ZoneId}

import models.nrs._
import org.joda.time.LocalDate
import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.auth.core.{Admin, ConfidenceLevel}

object NrsTestData {

  object AnswerTestData {

    object MockJson {

      val correctJsonSingleLineAnswer: JsObject = Json.obj(
        "questionId" -> "question id",
        "question" -> "question",
        "answer" -> "this is an answer"
      )

      val correctJsonMultiLineAnswer: JsObject = Json.obj(
        "questionId" -> "question id",
        "question" -> "question",
        "answers" -> Json.arr(
          "answer 1",
          "answer 2",
          "answer 3",
          "answer 4"
        )
      )
    }

    object Models {
      val correctModelSingleLineAnswer: Answer = Answer(
        "question id",
        "question",
        Some("this is an answer")
      )

      val correctModelMultiLineAnswer: Answer = Answer(
        "question id",
        "question",
        None,
        Some(Seq(
          "answer 1",
          "answer 2",
          "answer 3",
          "answer 4"
        ))
      )
    }

  }

  object AnswersTestData {

    val correctJson: JsObject = Json.obj(
      "title" -> "answer title",
      "data" -> Json.arr(
        AnswerTestData.MockJson.correctJsonSingleLineAnswer,
        Json.obj(
          "questionId" -> "question id",
          "question" -> "question",
          "answer" -> "answer here"
        )
      )
    )

    val correctModel: Answers = Answers(
      "answer title",
      Seq(
        AnswerTestData.Models.correctModelSingleLineAnswer,
        Answer(
          "question id",
          "question",
          Some("answer here")
        )
      )
    )
  }

  object DeclarationTestData {
    val correctJson: JsValue = Json.parse(
      """
        |{
        | "declarationText": "declaration text",
        | "declarationName": "declaration name",
        | "declarationRole": "declaration role",
        | "declarationConsent": true
        |}
      """.stripMargin)

    val correctModel: Declaration = Declaration(
      "declaration text",
      "declaration name",
      Some("declaration role"),
      declarationConsent = true
    )
  }

  object ReceiptTestData {
    val correctJson: JsObject = Json.obj(
      "language" -> "en",
      "checkYourAnswersSections" -> Json.arr(
        AnswersTestData.correctJson
      ),
      "declaration" -> DeclarationTestData.correctJson
    )

    val correctModel = ReceiptData(
      EN, Seq(AnswersTestData.correctModel), DeclarationTestData.correctModel
    )
  }

  object IdentityDataTestData {

    val correctJson: JsValue = Json.parse(
      """{
        |  "internalId": "some-id",
        |  "externalId": "some-id",
        |  "agentCode": "TZRXXV",
        |  "credentials": {"providerId": "12345-credId",
        |  "providerType": "GovernmentGateway"},
        |  "confidenceLevel": 200,
        |  "nino": "DH00475D",
        |  "saUtr": "Utr",
        |  "name": { "name": "test", "lastName": "test" },
        |  "dateOfBirth": "1985-01-01",
        |  "email":"test@test.com",
        |  "agentInformation": {
        |    "agentId": "BDGL",
        |    "agentCode" : "TZRXXV",
        |    "agentFriendlyName" : "Bodgitt & Legget LLP"
        |  },
        |  "groupIdentifier" : "GroupId",
        |  "credentialRole": "Admin",
        |  "mdtpInformation" : {"deviceId" : "DeviceId",
        |    "sessionId": "SessionId" },
        |  "itmpName" : {},
        |  "itmpAddress" : {},
        |  "affinityGroup": "Agent",
        |  "credentialStrength": "strong",
        |  "loginTimes": {
        |    "currentLogin": "2016-11-27T09:00:00.000Z",
        |    "previousLogin": "2016-11-01T12:00:00.000Z"
        |  }
        |}""".stripMargin)

    val correctModel: IdentityData = IdentityData(
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
      itmpName = ItmpName(None, None, None),
      itmpDateOfBirth = None,
      itmpAddress = ItmpAddress(None, None, None, None, None, None, None, None),
      affinityGroup = Some(Agent),
      credentialStrength = Some("strong"),
      loginTimes = IdentityLoginTimes(
        LocalDateTime.ofInstant(Instant.parse("2016-11-27T09:00:00.000Z"), ZoneId.of("UTC")),
        Some(LocalDateTime.ofInstant(Instant.parse("2016-11-01T12:00:00.000Z"), ZoneId.of("UTC")))
      )
    )
  }

  object MetadataTestData {
    val correctJson: JsValue = Json.parse(
      s"""
         |{
         |    "businessId": "vat-ui",
         |    "notableEvent": "vat-return-ui",
         |    "payloadContentType": "text/html",
         |    "payloadSha256Checksum": "426a1c28<snip>d6d363",
         |    "userSubmissionTimestamp": "2018-04-07T12:13:25.156Z",
         |    "identityData": ${IdentityDataTestData.correctJson},
         |    "userAuthToken": "Bearer AbCdEf123456...",
         |    "headerData": {
         |      "Gov-Client-Public-IP": "127.0.0.0",
         |      "Gov-Client-Public-Port": "12345",
         |      "Gov-Client-Device-ID": "beec798b-b366-47fa-b1f8-92cede14a1ce",
         |      "Gov-Client-User-ID": "alice_desktop",
         |      "Gov-Client-Timezone": "GMT+3",
         |      "Gov-Client-Local-IP": "10.1.2.3",
         |      "Gov-Client-Screen-Resolution": "1920x1080",
         |      "Gov-Client-Window-Size": "1256x803",
         |      "Gov-Client-Colour-Depth": "24"
         |    },
         |    "searchKeys": {
         |      "vrn": "123456789",
         |      "periodKey": "18AA"
         |    },
         |    "receiptData": {
         |      "language": "en",
         |      "checkYourAnswersSections": [
         |        {
         |          "title": "VAT details",
         |          "data": [
         |            {"questionId":"fooVatDetails1",
         |             "question": "VAT taxable sales ...",
         |             "answer": "Yes"},
         |            {"questionId":"fooVatDetails2",
         |             "question": "VAT start date",
         |             "answer": "The date the company is registered"},
         |            {"questionId":"fooVatDetails3",
         |             "question": "Other trading name",
         |             "answer": "company name"}
         |          ]
         |        },
         |        {
         |          "title": "Director details",
         |          "data": [
         |            {"questionId":"fooDirectorDetails1",
         |             "question": "Person registering the company for VAT",
         |             "answer": "person name"},
         |            {"questionId":"fooDirectorDetails2",
         |             "question": "Former name",
         |             "answer": "former name"},
         |            {"questionId":"fooDirectorDetails3",
         |             "question": "Date of birth",
         |             "answer": "1 January 2000"}
         |          ]
         |        },
         |        {
         |          "title": "Director addresses",
         |          "data": [
         |            {"questionId":"fooDirectorAddress1",
         |             "question": "Home address",
         |             "answers": [
         |              "address line 1",
         |              "address line 2",
         |              "town",
         |              "postcode"
         |            ]},
         |            {"questionId":"fooDirectorAddress2",
         |             "question": "Lived at current address for more than 3 years",
         |             "answer": "Yes"}
         |          ]
         |        }
         |      ],
         |      "declaration": {
         |        "declarationText": "I confirm the data ....",
         |        "declarationName": "John Smith",
         |        "declarationRole": "Finance Director",
         |        "declarationConsent": true
         |      }
         |    }
         |  }
      """.stripMargin)

    val correctModel: Metadata = Metadata(
      payloadSha256Checksum = "426a1c28<snip>d6d363",
      userSubmissionTimestamp = LocalDateTime.ofInstant(Instant.parse("2018-04-07T12:13:25.156Z"), ZoneId.of("UTC")),
      identityData = IdentityDataTestData.correctModel,
      userAuthToken = "Bearer AbCdEf123456...",
      headerData = Map(
        "Gov-Client-Public-IP" -> "127.0.0.0",
        "Gov-Client-Public-Port" -> "12345",
        "Gov-Client-Device-ID" -> "beec798b-b366-47fa-b1f8-92cede14a1ce",
        "Gov-Client-User-ID" -> "alice_desktop",
        "Gov-Client-Timezone" -> "GMT+3",
        "Gov-Client-Local-IP" -> "10.1.2.3",
        "Gov-Client-Screen-Resolution" -> "1920x1080",
        "Gov-Client-Window-Size" -> "1256x803",
        "Gov-Client-Colour-Depth" -> "24"
      ),
      searchKeys = SearchKeys("123456789", "18AA"),
      receiptData = ReceiptData(
        EN, Seq(
          Answers(
            "VAT details",
            Seq(
              Answer("fooVatDetails1", "VAT taxable sales ...", Some("Yes")),
              Answer("fooVatDetails2", "VAT start date", Some("The date the company is registered")),
              Answer("fooVatDetails3", "Other trading name", Some("company name"))
            )
          ),
          Answers(
            "Director details",
            Seq(
              Answer("fooDirectorDetails1", "Person registering the company for VAT", Some("person name")),
              Answer("fooDirectorDetails2", "Former name", Some("former name")),
              Answer("fooDirectorDetails3", "Date of birth", Some("1 January 2000"))
            )
          ),
          Answers(
            "Director addresses",
            Seq(
              Answer("fooDirectorAddress1", "Home address", None, Some(Seq(
                "address line 1",
                "address line 2",
                "town",
                "postcode"
              ))),
              Answer("fooDirectorAddress2", "Lived at current address for more than 3 years", Some("Yes"))
            )
          )
        ),
        Declaration(
          "I confirm the data ....",
          "John Smith",
          Some("Finance Director"),
          declarationConsent = true
        )
      )
    )
  }

  object FullRequestTestData {
    val correctJson: JsObject = Json.obj(
      "payload" -> "XXX-base64-CheckYourAnswersHTML-XXX",
      "metadata" -> MetadataTestData.correctJson
    )

    val correctModel: RequestModel = RequestModel(
      "XXX-base64-CheckYourAnswersHTML-XXX", MetadataTestData.correctModel
    )
  }

}
