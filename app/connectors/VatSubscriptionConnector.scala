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
import javax.inject.Inject
import models.{CustomerDetails, ErrorModel, FailedToParseCustomerDetails}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

class VatSubscriptionConnector @Inject()(wSClient: WSClient, appConfig: AppConfig) {
  private lazy val endpoint: String = "customer-details"
  private lazy val urlToCall: String => String = vrn => appConfig.vatSubscriptionUrl(vrn, endpoint)

  def getCustomerDetails(vrn: String)(implicit ec: ExecutionContext): Future[Either[ErrorModel, CustomerDetails]] = {
    wSClient.url(urlToCall(vrn)).get().map { response =>
      response.json.asOpt[CustomerDetails] match {
        case Some(cd: CustomerDetails) => Right(cd)
        case None => Left(FailedToParseCustomerDetails)
      }
    }
  }
}
