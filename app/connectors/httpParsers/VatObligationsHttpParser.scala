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
import models.VatObligations
import models.errors.{ApiSingleError, ServerSideError, UnexpectedJsonFormat, UnexpectedStatusError}
import play.api.Logger
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND, OK}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

import scala.util.{Failure, Success, Try}

object VatObligationsHttpParser extends ResponseHttpParsers {

  implicit object VatObligationsReads extends HttpReads[HttpGetResult[VatObligations]] {
    override def read(method: String, url: String, response: HttpResponse): HttpGetResult[VatObligations] = {
      response.status match {
        case OK => Try(response.json.as[VatObligations]) match {
          case Success(model) => Right(model)
          case Failure(_) =>
            Logger.debug(s"[VatReturnObligationsReads][read] Could not parse JSON. Received: ${response.json}")
            Logger.warn("[VatReturnObligationsReads][read] Unexpected JSON received.")
            Left(UnexpectedJsonFormat)
        }
        case NOT_FOUND => Right(VatObligations(Seq.empty))
        case BAD_REQUEST => Logger.warn(s"[VatReturnObligationsReads][read] Unexpected response: $BAD_REQUEST")
          handleBadRequest(response.json)(ApiSingleError.apiSingleErrorReads)
        case status if status >= 500 && status < 600 =>
          Logger.warn(s"[VatReturnObligationsReads][read] Unexpected response: $status. Body: ${response.body}")
          Left(ServerSideError(response.status.toString, response.body))
        case status =>
          Logger.warn(s"[VatReturnObligationsReads][read] Unexpected response: $status. Body: ${response.body}")
          Left(UnexpectedStatusError(status.toString, response.body))
      }
    }
  }
}
