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

import java.time.LocalDate

import base.BaseISpec
import models.nrs.identityData._
import models.{ConfirmSubmissionViewModel, SubmitVatReturnModel}
import models.nrs._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import utils.HashUtil

trait NrsAssets extends BaseISpec {

  lazy val date = LocalDate.parse("2019-12-25")

  lazy val submitModel: SubmitVatReturnModel =
    SubmitVatReturnModel(10.01, 10.02, 10.03, 10.04, 10.05, 10.06, 10.07, 10.08, 10.09, false, date, date, date)

  lazy val viewModel = ConfirmSubmissionViewModel(submitModel, "18AA", Some(" "))
  lazy val html = views.html.confirm_submission(viewModel, false)(FakeRequest("", ""), messages, appConfig, user)
  lazy val payloadHash = HashUtil.encode(html.body)
  lazy val sha256Checksum = HashUtil.getHash(html.body)

  lazy val businessId = "vat"

  lazy val notableEvent = "vat-return"

  lazy val payloadContentType = "text\\/html"

  lazy val timestamp = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z"


  lazy val credentials = IdentityCredentials("", "")
  lazy val confidenceLevel = 0
  lazy val name = IdentityName("", "")
  lazy val agentInformation = IdentityAgentInformation("", "", "")
  lazy val itmpName = IdentityItmpName("", "", "")
  lazy val itmpAddress = IdentityItmpAddress("", "", "", "")

  lazy val searchKeys = SearchKeys("999999999", "18AA")

  lazy val receiptData = ReceiptData(
    EN,
    Seq(),
    Declaration(
      "", "", None, false
    )
  )

  lazy val declaration = Declaration(
    "", "", None, false
  )

  lazy val jaason =
    """\{"payload":".*?","metadata":\{"businessId":"vat","notableEvent":"vat-return","payloadContentType":"text\/html","payloadSha256Checksum":".*?"
      |,"userSubmissionTimestamp":"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z","identityData":\{"credentials":\{"providerId":"","providerType":""\},
      |"confidenceLevel":0,"name":\{"name":"","lastName":""\},"agentInformation":\{"agentCode":"","agentFriendlyName":"","agentId":""\},"itmpName":\{
      |"givenName":"","middleName":"","familyName":""\},"itmpAddress":\{"line1":"","postCode":"","countryName":"","countryCode":""\},"loginTimes":\{
      |"currentLogin":"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z","previousLogin":"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z"\}\},"userAuthToken"
      |:"authToken","headerData":\{"Csrf-Token":"nocheck","X-Request-ID":"govuk-tax-.*?","X-Request-Timestamp":"\d*","Content-Length":"2","Accept":
      |"\*\/\*","Content-Type":"application\/json","Cookie":".*?","User-Agent":"AHC\/2\.0","Host":"localhost:19001"\},"searchKeys":\{"vrn":"999999999",
      |"periodKey":"18AA"\},"receiptData":\{"language":"en","checkYourAnswersSections":\[\],"declaration":\{"declarationText":"","declarationName":"",
      |"declarationConsent":false\}\}\}\}""".stripMargin.replace("\n", "")

}
