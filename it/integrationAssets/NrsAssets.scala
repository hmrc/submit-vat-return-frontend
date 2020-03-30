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

package integrationAssets

import base.BaseISpec

trait NrsAssets extends BaseISpec {

  val businessId = "vat-ui"
  val notableEvent = "vat-return-ui"
  val payloadContentType = "text\\/html"

  val nrsFullSubmissionJson: String =
    """\{"payload":".*?","metadata":\{"businessId":"""" + businessId + """","notableEvent":"""" + notableEvent +
      """","payloadContentType":"""" + payloadContentType +
      """","payloadSha256Checksum":".*?",
        |"userSubmissionTimestamp":"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z",
        |"identityData":\
        |{"internalId":".*?",
        |"externalId":".*?",
        |"agentCode":".*?",
        |"credentials":\{"providerId":".*?","providerType":".*?"\},
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
        |"loginTimes":\{"previousLogin":"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z",
        |"currentLogin":"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z"\}
        |\},
        |"userAuthToken":"Bearer 1234",
        |"headerData":\{"Csrf-Token":"nocheck","X-Request-ID":"govuk-tax-.*?","Timeout-Access":".*?",
        |"Tls-Session-Info":".*?","X-Request-Timestamp":"\d*","Content-Length":"2","Accept":"\*\/\*","Content-Type":"application\/json","Cookie":".*?","User-Agent":"AHC\/2\.0","Host":"localhost:19001",
        |"Raw-Request-URI":"\/vat-through-software\/submit-vat-return\/18AA\/confirm-submission","Remote-Address":".*?"\},
        |"searchKeys":\{"vrn":"999999999","periodKey":"18AA"\},
        |"receiptData":\{"language":"en","checkYourAnswersSections":\[\{"title":"VAT Return submission complete",
        |"data":\[
        |\{"questionId":"box1","question":".*?","answer":".*?"\},
        |\{"questionId":"box2","question":".*?","answer":".*?"\},
        |\{"questionId":"box3","question":".*?","answer":".*?"\},
        |\{"questionId":"box4","question":".*?","answer":".*?"\},
        |\{"questionId":"box5","question":".*?","answer":".*?"\},
        |\{"questionId":"box6","question":".*?","answer":".*?"\},
        |\{"questionId":"box7","question":".*?","answer":".*?"\},
        |\{"questionId":"box8","question":".*?","answer":".*?"\},
        |\{"questionId":"box9","question":".*?","answer":".*?"\}\]\}\],
        |"declaration":\{"declarationText":".*?","declarationName":".*?","declarationConsent":true\}\}\}\}""".stripMargin.replace("\n", "")
}
