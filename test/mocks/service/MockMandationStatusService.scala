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

package mocks.service

import connectors.httpParsers.ResponseHttpParsers.HttpResult
import models.MandationStatus
import org.scalamock.scalatest.MockFactory
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import services.MandationStatusService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait MockMandationStatusService extends AnyWordSpecLike with Matchers with OptionValues with MockFactory {

  val mockMandationStatusService: MandationStatusService = mock[MandationStatusService]

  def setupMockMandationStatus(response: Future[HttpResult[MandationStatus]]): Unit = {
    (mockMandationStatusService.getMandationStatus(_: String)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *)
      .returns(response)
  }
}
