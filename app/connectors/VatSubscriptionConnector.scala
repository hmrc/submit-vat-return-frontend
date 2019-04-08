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
import connectors.httpParsers.ResponseHttpParsers.HttpGetResponse
import javax.inject.Inject
import models.{CustomerDetails, ErrorModel}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import connectors.httpParsers.CustomerDetailsHttpParser._

import scala.concurrent.{ExecutionContext, Future}

class VatSubscriptionConnector @Inject()(httpClient: HttpClient, appConfig: AppConfig) {
  private def vatSubscriptionUrl(vrn: String, endpoint: String): String = appConfig.baseUrl("vat-subscription") + s"/vat-subscription/$vrn/$endpoint"

  private lazy val endpoint: String = "customer-details"
  private lazy val urlToCall: String => String = vrn => vatSubscriptionUrl(vrn, endpoint)

  def getCustomerDetails(vrn: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[ErrorModel, CustomerDetails]] = {
    httpClient.GET[HttpGetResponse[CustomerDetails]](urlToCall(vrn))
  }
}
