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

package connectors.httpParsers

import connectors.httpParsers.ResponseHttpParsers.HttpGetResponse
import models.{CustomerDetails, FailedToParseCustomerDetails}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import play.api.http.Status._

import scala.util.{Failure, Success, Try}

object CustomerDetailsHttpParser extends ResponseHttpParsers {
  implicit object CustomerDetailsReads extends HttpReads[HttpGetResponse[CustomerDetails]] {
    override def read(method: String, url: String, response: HttpResponse): HttpGetResponse[CustomerDetails] = {
      response.status match {
        case OK => Try {
          response.json.as[CustomerDetails]
        } match {
          case Success(parsedModel) => Right(parsedModel)
          case Failure(_) => Left(FailedToParseCustomerDetails)
        }
      }
    }
  }
}
