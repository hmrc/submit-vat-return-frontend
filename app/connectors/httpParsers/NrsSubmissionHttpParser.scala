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

import connectors.httpParsers.ResponseHttpParsers.HttpGetResult
import models.errors.ErrorModel
import models.nrs.SuccessModel
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.LoggerUtil

import scala.util.{Failure, Success, Try}

object NrsSubmissionHttpParser extends ResponseHttpParsers with LoggerUtil {

  implicit object NrsSubmissionReads extends HttpReads[HttpGetResult[SuccessModel]] {

    override def read(method: String, url: String, response: HttpResponse): HttpGetResult[SuccessModel] = {
      response.status match {
        case ACCEPTED => Try(response.json.as[SuccessModel]) match {
          case Success(model) =>
            logger.debug(s"[NrsSubmissionHttpParser][NrsSubmissionReads]: Successful NRS submission. Submission id: ${model.nrSubmissionId}")
            Right(model)
          case Failure(_) =>
            logger.warn("[NrsSubmissionHttpParser][NrsSubmissionReads]: Successful NRS submission but nrSubmissionId not returned in body")
            Right(SuccessModel(""))
        }
        case BAD_REQUEST =>
          logger.debug(s"[NrsSubmissionHttpParser][NrsSubmissionReads]: Bad Request response when submitting to NRS. Body: ${response.body}")
          logger.warn("[NrsSubmissionHttpParser][NrsSubmissionReads]: Bad Request response when submitting to NRS.")
          Left(ErrorModel(BAD_REQUEST, "Bad Request response when submitting to NRS."))
        case status =>
          logger.warn(s"[NrsSubmissionHttpParser][NrsSubmissionReads]: Unexpected response. Status $status. Body: ${response.body}")
          Left(ErrorModel(status, "Received downstream error when submitting to NRS"))
      }
    }
  }
}
