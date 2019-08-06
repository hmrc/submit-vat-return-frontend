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

package stubs

import base.BaseISpec
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status.OK
import play.api.libs.json.{JsObject, JsValue, Json}

object AuthStub extends BaseISpec {

  val uri: String = "/auth/authorise"

  val mtdVatAuthResponse: JsObject = Json.obj(
    "affinityGroup" -> "Individual",
    "allEnrolments" -> Json.arr(
      Json.obj(
        "key" -> "HMRC-MTD-VAT",
        "identifiers" -> Json.arr(
          Json.obj(
            "key" -> "VRN",
            "value" -> "999999999"
          )
        )
      )
    )
  )

  val otherEnrolmentAuthResponse: JsObject = Json.obj(
    "affinityGroup" -> "Individual",
    "allEnrolments" -> Json.arr(
      Json.obj(
        "key" -> "OTHER",
        "identifiers" -> Json.arr(
          Json.obj(
            "key" -> "OTHER",
            "value" -> "12345"
          )
        )
      )
    )
  )
  
  val stupidlyLongAuthResponse: JsObject = Json.obj(
    "internalId" -> "someId",
    "externalId" -> "some-id",
    "agentCode" -> "TZRXXV",
    "credentials" -> Json.obj(
      "providerId" -> "12345-credId",
      "providerType" -> "GovernmentGateway"
    ),
    "confidenceLevel" -> 200,
    "nino" -> "DH00475D",
    "saUtr" -> "Utr",
    "name" -> Json.obj(
      "name" -> "Duanne",
      "lastName" -> "Kilometers"
    ),
    "dateOfBirth" -> "1985-01-01",
    "email" ->"test@test.com",
    "agentInformation" -> Json.obj(
      "agentId" -> "BDGL",
      "agentCode"  -> "TZRXXV",
      "agentFriendlyName"  -> "Bodgitt & Legget LLP"
    ),
    "groupIdentifier"  -> "GroupId",
    "credentialRole" -> "Admin",
    "mdtpInformation"  -> Json.obj(
      "deviceId"  -> "DeviceId",
      "sessionId" -> "SessionId"
    ),
    "itmpName"  -> Json.obj(
      "givenName" -> "test",
      "middleName" -> "test",
      "familyName" -> "test"
    ),
    "itmpDateOfBirth"  -> "1985-01-01",
    "itmpAddress"  -> Json.obj(
      "line1" -> "Line 1",
      "postCode" -> "NW94HD",
      "countryName" -> "United Kingdom",
      "countryCode" -> "UK"
    ),
    "affinityGroup" -> "Individual",
    "credentialStrength" -> "strong",
    "loginTimes" -> Json.obj(
      "currentLogin" -> "2016-11-27T09:00:00.000Z",
      "previousLogin" -> "2016-11-01T12:00:00.000Z"
    ),
    "allEnrolments" -> Json.arr(
      Json.obj(
        "key" -> "HMRC-MTD-VAT",
        "identifiers" -> Json.arr(
          Json.obj(
            "key" -> "VRN",
            "value" -> "999999999"
          )
        )
      )
    )
  )

  def stubResponse(status: Int, body: JsObject): StubMapping = {
    stubPost(uri, Json.stringify(body), OK)
  }

}
