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
import models.MandationStatus
import models.errors.{ErrorModel, UnexpectedJsonError}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import play.api.http.Status._
import utils.LoggingUtil

import scala.util.{Failure, Success, Try}

object MandationStatusHttpParser extends ResponseHttpParsers with LoggingUtil {

  implicit object MandationStatusReads extends HttpReads[HttpResult[MandationStatus]] {
    override def read(method: String, url: String, response: HttpResponse): HttpResult[MandationStatus] = {
      implicit val res: HttpResponse = response
      response.status match {
        case OK => Try {
          response.json.as[MandationStatus]
        } match {
          case Success(parsedModel) => Right(parsedModel)
          case Failure(reason) =>
            debug(s"[MandationStatusHttpParser][MandationStatusReads]: Invalid Json - $reason")
            errorLogRes("[MandationStatusHttpParser][MandationStatusReads]: Invalid Json returned")
            Left(UnexpectedJsonError)
        }
        case status =>
          errorLogRes(s"[MandationStatusHttpParser][MandationStatusReads] - an unexpected error occured with status $status was returned ")
          Left(ErrorModel(response.status, response.body))
      }
    }
  }

}
