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

import java.time.LocalDate
import base.BaseSpec
import connectors.VatObligationsConnector
import connectors.httpParsers.ResponseHttpParsers.HttpResult
import models.errors.ErrorModel
import models.{VatObligation, VatObligations}
import uk.gov.hmrc.http.HeaderCarrier
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import play.api.http.Status._

import scala.concurrent.{ExecutionContext, Future}

class VatObligationsServiceSpec extends BaseSpec {

  val mockConnector: VatObligationsConnector = mock[VatObligationsConnector]
  val service = new VatObligationsService(mockConnector)

  "getObligations" should {
    "return a VatObligations" when {
      "a VatObligations is returned from the connector" in {
        val expectedResult = Right(VatObligations(Seq(VatObligation(
          LocalDate.now(), LocalDate.now(), LocalDate.now(), "AA"
        ))))

        (mockConnector.getObligations(_: String)(_: HeaderCarrier, _: ExecutionContext))
          .expects(*, *, *)
          .returning(Future.successful(expectedResult))

        val result: HttpResult[VatObligations] = await(service.getObligations("101202303"))
        result shouldBe expectedResult
      }
    }
    "return an error" when {
      "an error is returned from the connector" in {
        val expectedResult = Left(ErrorModel(INTERNAL_SERVER_ERROR, ""))

        (mockConnector.getObligations(_: String)(_: HeaderCarrier, _: ExecutionContext))
          .expects(*, *, *)
          .returning(Future.successful(expectedResult))

        val result: HttpResult[VatObligations] = await(service.getObligations("101202303"))
        result shouldBe expectedResult
      }
    }
  }

}
