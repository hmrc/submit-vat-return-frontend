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
import models.errors.{ErrorModel, UnexpectedJsonError}
import models.vatReturnSubmission.SubmissionSuccessModel
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.LoggerUtil

import scala.util.{Failure, Success, Try}

case class SubmitVatReturnHttpParser(vrn: String,
                                     periodKey: String) extends ResponseHttpParsers with LoggerUtil {

  implicit object SubmitVatReturnReads extends HttpReads[HttpResult[SubmissionSuccessModel]] {

    override def read(method: String, url: String, response: HttpResponse): HttpResult[SubmissionSuccessModel] = {
      response.status match {
        case OK => Try(response.json.as[SubmissionSuccessModel]) match {
          case Success(model) => Right(model)
          case Failure(exception) =>
            logger.debug(s"[SubmitVatReturnHttpParser][SubmitVatReturnReads]: Invalid Json returned. Exception: $exception")
            logger.warn("[SubmitVatReturnHttpParser][SubmitVatReturnReads]: Invalid Json returned")
            Left(UnexpectedJsonError)
        }
        case status =>
          logger.warn(s"[SubmitVatReturnHttpParser][SubmitVatReturnReads]: Unexpected response for VRN: $vrn with " +
            s"period key: $periodKey. Status: $status. Body: ${response.body}")
          Left(ErrorModel(status, "Received downstream error when submitting VAT return."))
      }
    }
  }
}
