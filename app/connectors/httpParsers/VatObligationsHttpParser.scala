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
import models.VatObligations
import models.errors.{ErrorModel, UnexpectedJsonError}
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.LoggingUtil

import scala.util.{Failure, Success, Try}

object VatObligationsHttpParser extends ResponseHttpParsers with LoggingUtil {

  implicit object VatObligationsReads extends HttpReads[HttpResult[VatObligations]] {
    override def read(method: String, url: String, response: HttpResponse): HttpResult[VatObligations] = {
      implicit val res: HttpResponse = response
      response.status match {
        case OK => Try(response.json.as[VatObligations]) match {
          case Success(model) => Right(model)
          case Failure(_) =>
            debug(s"[VatReturnObligationsReads][read] Could not parse JSON. Received: ${response.json}")
            warnLogRes("[VatReturnObligationsReads][read] Unexpected JSON received.")
            Left(UnexpectedJsonError)
        }
        case NOT_FOUND =>
          errorLogRes(s"[VatObligationsReads][read] - Could not found the vat obligation")
          Right(VatObligations(Seq.empty))
        case status =>
          errorLogRes(s"[VatReturnObligationsReads][read] Unexpected response: $status. Body: ${response.body}")
          Left(ErrorModel(response.status, response.body))
      }
    }
  }
}
