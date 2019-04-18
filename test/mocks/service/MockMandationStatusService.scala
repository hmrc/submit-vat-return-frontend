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

import common.MandationStatuses.nonMTDfB
import connectors.httpParsers.ResponseHttpParsers.HttpGetResult
import models.MandationStatus
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import services.MandationStatusService
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

trait MockMandationStatusService extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  val mockMandationStatusService: MandationStatusService = mock[MandationStatusService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockMandationStatusService)
  }

  def setupMockMandationStatus(vrn: String)(response: HttpGetResult[MandationStatus]): OngoingStubbing[Future[HttpGetResult[MandationStatus]]] = {
    when(mockMandationStatusService.getMandationStatus(ArgumentMatchers.eq(vrn))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(response))
  }

  def setupMockMandationStatusSuccess(): OngoingStubbing[Future[HttpGetResult[MandationStatus]]] = {
    setupMockMandationStatus("968501689")(Right(MandationStatus(nonMTDfB)))
  }
}
