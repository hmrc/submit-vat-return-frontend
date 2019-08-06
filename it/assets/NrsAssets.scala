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

import base.BaseISpec

trait NrsAssets extends BaseISpec {

  lazy val businessId = "vat"
  lazy val notableEvent = "vat-return"
  lazy val payloadContentType = "text\\/html"

  lazy val nrsFullSubmissionJson: String =
    """\{"payload":".*?","metadata":\{"businessId":"""" + businessId + """","notableEvent":"""" + notableEvent +
      """","payloadContentType":"""" + payloadContentType + """","payloadSha256Checksum":".*?"
      |,"userSubmissionTimestamp":"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z",
      |"identityData":\
      |{"internalId":".*?",
      |"externalId":".*?",
      |"agentCode":".*?",
      |"credentials":\
      |{"providerId":".*?","providerType":".*?"\},
      |"confidenceLevel":.*?,
      |"nino":".*?",
      |"saUtr":".*?",
      |"name":\{"name":".*?","lastName":".*?"\},
      |"dateOfBirth":"\d{4}-\d{2}-\d{2}",
      |"email":".*?",
      |"agentInformation":\{"agentId":".*?","agentCode":".*?","agentFriendlyName":".*?"\},
      |"groupIdentifier":".*?",
      |"credentialRole":".*?",
      |"mdtpInformation":\{"deviceId":".*?","sessionId":".*?"\},
      |"itmpName":\{"givenName":".*?","middleName":".*?","familyName":".*?"\},
      |"itmpDateOfBirth":"\d{4}-\d{2}-\d{2}",
      |"itmpAddress":\{"line1":".*?","postCode":".*?","countryName":".*?","countryCode":".*?"\},
      |"affinityGroup":".*?",
      |"credentialStrength":".*?",
      |"loginTimes":\{"currentLogin":"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z",
      |"previousLogin":"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z"\}\},
      |"userAuthToken":"Bearer 1234",
      |"headerData":\{"Csrf-Token":"nocheck","X-Request-ID":"govuk-tax-.*?","X-Request-Timestamp":"\d*","Content-Length":"2","Accept":
      |"\*\/\*","Content-Type":"application\/json","Cookie":".*?","User-Agent":"AHC\/2\.0","Host":"localhost:19001"\},"searchKeys":\{"vrn":"999999999",
      |"periodKey":"18AA"\},"receiptData":\{"language":"en","checkYourAnswersSections":\[\],"declaration":\{"declarationText":"","declarationName":"",
      |"declarationConsent":false\}\}\}\}""".stripMargin.replace("\n", "")

}