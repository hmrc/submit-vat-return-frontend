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

package mocks

import connectors.httpParsers.ResponseHttpParsers.HttpGetResult
import models.VatObligations
import org.scalamock.scalatest.MockFactory
import services.VatObligationsService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.{ExecutionContext, Future}

trait MockVatObligationsService extends UnitSpec with MockFactory {

  val mockVatObligationsService: VatObligationsService = mock[VatObligationsService]

  def setupVatObligationsService(response: Future[HttpGetResult[VatObligations]])(implicit hc: HeaderCarrier, ec: ExecutionContext): Unit = {
    (mockVatObligationsService.getObligations(_: String)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *)
      .returns(response)
  }

}
