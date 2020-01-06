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

package mocks

import models.{CustomerDetails, SubmitVatReturnModel}
import models.auth.User
import models.errors.HttpError
import models.nrs.ReceiptData
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import utils.ReceiptDataHelper
import assets.NrsTestData.ReceiptTestData.{correctModel => TestReceiptDataModel}

import scala.concurrent.{ExecutionContext, Future}

trait MockReceiptDataService extends UnitSpec with MockFactory {

  val mockReceiptDataService: ReceiptDataHelper = mock[ReceiptDataHelper]

  def mockExtractReceiptData[A](response: Future[Either[HttpError, ReceiptData]])(implicit user: User[A], hc: HeaderCarrier, ec: ExecutionContext): Unit = {
    (mockReceiptDataService
      .extractReceiptData(_: SubmitVatReturnModel, _: Either[HttpError, CustomerDetails])(_: User[A], _: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *, *)
      .returns(response)
  }

  val successReceiptDataResponse: Future[Right[Nothing, ReceiptData]] = Future.successful(Right(TestReceiptDataModel))

}
