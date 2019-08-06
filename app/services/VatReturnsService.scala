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

import java.net.URLDecoder
import java.time.{LocalDateTime, ZoneOffset}
import connectors.VatReturnsConnector
import connectors.httpParsers.ResponseHttpParsers.HttpGetResult
import javax.inject.{Inject, Singleton}
import models.auth.User
import models.nrs._
import models.vatReturnSubmission.{SubmissionModel, SubmissionSuccessModel}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VatReturnsService @Inject()(vatReturnsConnector: VatReturnsConnector) {

  def submitVatReturn(vrn: String, model: SubmissionModel)
                     (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpGetResult[SubmissionSuccessModel]] = {
    vatReturnsConnector.submitVatReturn(vrn, model)
  }

  def nrsSubmission[A](periodKey: String,
                       payload: String,
                       payloadCheckSum: String,
                       identityData: IdentityData)
                   (implicit hc: HeaderCarrier, ec: ExecutionContext, user: User[A]): Future[HttpGetResult[SuccessModel]] = {

    val receiptDataModel = ReceiptData(
      EN,
      Seq(),
      Declaration("", "", None, declarationConsent = false)
    )

    //TODO: Change userAuthToken to do it properly
    val metaData = Metadata(
      payloadSha256Checksum = payloadCheckSum,
      userSubmissionTimestamp = LocalDateTime.now(ZoneOffset.UTC),
      identityData = identityData,
      searchKeys = searchKeys(user.vrn, periodKey),
      userAuthToken = user.headers.get("Authorization").get,
      headerData = user.headers.toMap.map { h => h._1 -> h._2.head },
      receiptData = receiptDataModel
    )

    val submissionModel = RequestModel(
      payload = payload,
      metadata = metaData
    )

    vatReturnsConnector.nrsSubmission(submissionModel, user.vrn)
  }

  private[services] def searchKeys(vrn: String, periodKey: String): SearchKeys = SearchKeys(
    vrn = vrn,
    periodKey = URLDecoder.decode(periodKey, "UTF-8")
  )
}
