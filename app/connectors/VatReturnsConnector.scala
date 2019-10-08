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

package connectors

import config.AppConfig
import connectors.httpParsers.ResponseHttpParsers.HttpGetResult
import connectors.httpParsers.SubmitVatReturnHttpParser
import javax.inject.{Inject, Singleton}
import models.nrs.{RequestModel, SuccessModel}
import models.vatReturnSubmission.{SubmissionModel, SubmissionSuccessModel}
import play.api.libs.json.Writes
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VatReturnsConnector @Inject()(http: HttpClient,
                                    appConfig: AppConfig) {

  def submitVatReturn(vrn: String, model: SubmissionModel)
                     (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpGetResult[SubmissionSuccessModel]] = {

    val httpReads = SubmitVatReturnHttpParser(vrn, model.periodKey).SubmitVatReturnReads
    implicit val headerCarrier: HeaderCarrier = hc.withExtraHeaders("OriginatorID" -> "VATUI")

    http.POST[SubmissionModel, HttpGetResult[SubmissionSuccessModel]](appConfig.submitReturnUrl(vrn), model)(
      implicitly[Writes[SubmissionModel]],
      httpReads,
      headerCarrier,
      implicitly[ExecutionContext]
    )
  }

  def nrsSubmission(requestModel: RequestModel, vrn: String)
                   (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpGetResult[SuccessModel]] = {

    import connectors.httpParsers.NrsSubmissionHttpParser._

    http.POST[RequestModel, HttpGetResult[SuccessModel]](appConfig.submitNrsUrl + s"/$vrn", requestModel)
  }
}
