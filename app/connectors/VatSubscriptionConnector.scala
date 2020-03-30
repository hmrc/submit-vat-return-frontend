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

package connectors

import config.AppConfig
import connectors.httpParsers.ResponseHttpParsers.HttpGetResult
import javax.inject.{Inject, Singleton}
import models.{CustomerDetails, MandationStatus}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VatSubscriptionConnector @Inject()(httpClient: HttpClient, appConfig: AppConfig) {

  private[connectors] def vatSubscriptionUrl(vrn: String,endpoint: String): String = appConfig.vatSubscriptionBaseUrl + s"/vat-subscription/$vrn/$endpoint"

  private lazy val urlToCall: (String, String) => String = (vrn, endpoint) => vatSubscriptionUrl(vrn, endpoint)

  def getCustomerDetails(vrn: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpGetResult[CustomerDetails]] = {

    import connectors.httpParsers.CustomerDetailsHttpParser.CustomerDetailsReads

    val endpoint: String = "customer-details"

    httpClient.GET[HttpGetResult[CustomerDetails]](urlToCall(vrn, endpoint))
  }

  def getCustomerMandationStatus(vrn: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpGetResult[MandationStatus]] = {

    import connectors.httpParsers.MandationStatusHttpParser.MandationStatusReads

    val endpoint: String = "mandation-status"

    httpClient.GET[HttpGetResult[MandationStatus]](urlToCall(vrn, endpoint))
  }
}
