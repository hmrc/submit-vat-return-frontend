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

package services

import base.BaseSpec
import common.MandationStatuses.nonMTDfB
import connectors.VatSubscriptionConnector
import connectors.httpParsers.ResponseHttpParsers.HttpResult
import models.MandationStatus
import models.errors.ErrorModel
import uk.gov.hmrc.http.HeaderCarrier
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import play.api.http.Status._

import scala.concurrent.{ExecutionContext, Future}

class MandationStatusServiceSpec extends BaseSpec {

  val mockConnector: VatSubscriptionConnector = mock[VatSubscriptionConnector]
  val service = new MandationStatusService(mockConnector)

  "getMandationStatus" should {
    "return a Mandation Status" when {
      "a successful response is returned from the connector" in {
        val expectedResult = Right(MandationStatus(nonMTDfB))

        (mockConnector.getCustomerMandationStatus(_: String)(_: HeaderCarrier, _: ExecutionContext))
          .expects(*, *, *)
          .returning(Future.successful(expectedResult))

        val result: HttpResult[MandationStatus] = await(service.getMandationStatus("101202303"))
        result shouldBe expectedResult
      }
    }
    "return an error" when {
      "an error is returned from the connector" in {
        val expectedResult = Left(ErrorModel(INTERNAL_SERVER_ERROR, ""))

        (mockConnector.getCustomerMandationStatus(_: String)(_: HeaderCarrier, _: ExecutionContext))
          .expects(*, *, *)
          .returning(Future.successful(expectedResult))

        val result: HttpResult[MandationStatus] = await(service.getMandationStatus("101202303"))
        result shouldBe expectedResult
      }
    }
  }

}
