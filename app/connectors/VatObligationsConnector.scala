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

package connectors

import config.AppConfig
import connectors.httpParsers.ResponseHttpParsers.HttpResult
import javax.inject.Inject
import models.VatObligations
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

class VatObligationsConnector @Inject()(http: HttpClient,
                                        appConfig: AppConfig) {

  private def vatObligationsUrl(vrn: String): String = appConfig.vatObligationsBaseUrl + s"/vat-obligations/$vrn/obligations"

  def getObligations(vrn: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResult[VatObligations]] = {

    import connectors.httpParsers.VatObligationsHttpParser.VatObligationsReads

    val queryParams: Seq[(String, String)] = Seq("status" -> "O")

    http.GET[HttpResult[VatObligations]](
      vatObligationsUrl(vrn),
      queryParams
    )
  }
}

