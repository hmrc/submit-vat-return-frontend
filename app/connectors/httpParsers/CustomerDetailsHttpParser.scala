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

package connectors.httpParsers

import connectors.httpParsers.ResponseHttpParsers.HttpResult
import models.CustomerDetails
import models.errors.{ErrorModel, UnexpectedJsonError}
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.LoggingUtil

import scala.util.{Failure, Success, Try}

object CustomerDetailsHttpParser extends ResponseHttpParsers with LoggingUtil {
  implicit object CustomerDetailsReads extends HttpReads[HttpResult[CustomerDetails]] {
    override def read(method: String, url: String, response: HttpResponse): HttpResult[CustomerDetails] = {
      implicit val res: HttpResponse = response
      response.status match {
        case OK => Try {
          response.json.as[CustomerDetails]
        } match {
          case Success(parsedModel) => Right(parsedModel)
          case Failure(reason) =>
            debug(s"[CustomerDetailsHttpParser][CustomerDetailsReads]: Invalid Json - $reason")
            errorLogRes("[CustomerDetailsHttpParser][CustomerDetailsReads]: Invalid Json returned")
            Left(UnexpectedJsonError)
        }
        case status =>
          errorLogRes(s"[CustomerDetailsHttpParser][CustomerDetailsReads]: Unexpected Response, Status $status returned")
          Left(ErrorModel(status, "Received downstream error when retrieving customer details."))
      }
    }
  }
}
