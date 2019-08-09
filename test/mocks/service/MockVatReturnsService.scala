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

package mocks.service

import connectors.httpParsers.ResponseHttpParsers.HttpGetResult
import models.auth.User
import models.nrs.{IdentityData, ReceiptData, SuccessModel}
import models.vatReturnSubmission.{SubmissionModel, SubmissionSuccessModel}
import org.scalamock.scalatest.MockFactory
import services.VatReturnsService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.{ExecutionContext, Future}

trait MockVatReturnsService extends UnitSpec with MockFactory {

  val mockVatReturnsService: VatReturnsService = mock[VatReturnsService]

  def mockVatReturnSubmission(response: Future[HttpGetResult[SubmissionSuccessModel]])(implicit hc: HeaderCarrier, ec: ExecutionContext): Unit = {
    (mockVatReturnsService.submitVatReturn(_: String, _: SubmissionModel)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returns(response)
  }

  def mockNrsSubmission[A](response: Future[HttpGetResult[SuccessModel]])(implicit hc: HeaderCarrier, ec: ExecutionContext): Unit = {
    (mockVatReturnsService.nrsSubmission(_: String, _: String, _: String, _: IdentityData, _: ReceiptData)(_: HeaderCarrier, _: ExecutionContext, _: User[A]))
      .expects(*, *, *, *, *, *, *, *)
      .returns(response)
  }
}
