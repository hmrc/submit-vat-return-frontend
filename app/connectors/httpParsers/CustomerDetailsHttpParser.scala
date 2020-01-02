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

package connectors.httpParsers

import connectors.httpParsers.ResponseHttpParsers.HttpGetResult
import models.CustomerDetails
import models.errors.{ServerSideError, UnexpectedJsonFormat}
import play.api.Logger
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

import scala.util.{Failure, Success, Try}

object CustomerDetailsHttpParser extends ResponseHttpParsers {
  implicit object CustomerDetailsReads extends HttpReads[HttpGetResult[CustomerDetails]] {
    override def read(method: String, url: String, response: HttpResponse): HttpGetResult[CustomerDetails] = {
      response.status match {
        case OK => Try {
          response.json.as[CustomerDetails]
        } match {
          case Success(parsedModel) => Right(parsedModel)
          case Failure(reason) =>
            Logger.debug(s"[CustomerDetailsHttpParser][CustomerDetailsReads]: Invalid Json - $reason")
            Logger.warn("[CustomerDetailsHttpParser][CustomerDetailsReads]: Invalid Json returned")
            Left(UnexpectedJsonFormat)
        }
        case status =>
          Logger.warn(s"[CustomerDetailsHttpParser][CustomerDetailsReads]: Unexpected Response, Status $status returned")
          Left(ServerSideError(s"$status", "Received downstream error when retrieving customer details."))
      }
    }
  }
}
