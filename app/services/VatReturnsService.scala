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

package services

import java.time.LocalDateTime
import connectors.VatReturnsConnector
import connectors.httpParsers.ResponseHttpParsers.HttpGetResult
import javax.inject.{Inject, Singleton}
import models.nrs._
import models.nrs.identityData._
import models.vatReturnSubmission.{SubmissionModel, SubmissionSuccessModel}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VatReturnsService @Inject()(vatReturnsConnector: VatReturnsConnector) {

  def submitVatReturn(vrn: String, model: SubmissionModel)
                     (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpGetResult[SubmissionSuccessModel]] = {
    vatReturnsConnector.submitVatReturn(vrn, model)
  }

  def nrsSubmission(payload: String,
                    payloadCheckSum: String)
                   (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpGetResult[SuccessModel]] = {

    //TODO: BTAT-6413
    val identityDataModel = IdentityData(
      credentials = IdentityCredentials("", ""),
      confidenceLevel = 0,
      name = IdentityName("", ""),
      agentInformation =  IdentityAgentInformation("", "", ""),
      itmpName = IdentityItmpName("", "", ""),
      itmpAddress = IdentityItmpAddress("", "", "", ""),
      loginTimes = IdentityLoginTimes(LocalDateTime.now(), LocalDateTime.now())
    )

    //TODO: BTAT-6416
    val receiptDataModel = ReceiptData(
      EN,
      Seq(),
      Declaration("", "", None, declarationConsent = false)
    )

    //TODO: BTAT-6414
    val headerData = Map.empty[String, String]

    //TODO: BTAT-6417
    val metaData = Metadata(
      businessId = "",
      notableEvent = "",
      payloadContentType = "",
      payloadSha256Checksum = payloadCheckSum,
      userSubmissionTimestamp = LocalDateTime.now(),
      identityData = identityDataModel,
      userAuthToken = "",
      headerData = headerData,
      searchKeys = SearchKeys("", ""),
      receiptData = receiptDataModel
    )

    val submissionModel = RequestModel(
      payload = payload,
      metadata = metaData
    )

    vatReturnsConnector.nrsSubmission(submissionModel)
  }
}
