/*
 * Copyright 2022 HM Revenue & Customs
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

package connectors

import java.time.LocalDate
import base.BaseISpec
import models.errors.ErrorModel
import models.{VatObligation, VatObligations}
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

class VatObligationsConnectorSpec extends BaseISpec {

  val connector: VatObligationsConnector = new VatObligationsConnector(httpClient, appConfig)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "Calling .getObligations" when {

    "response is 200" should {

      "return a sequence of obligations" in {
        val expectedResults = VatObligations(Seq (
          VatObligation(LocalDate.now(), LocalDate.now(), LocalDate.now(), "#001")
        ))

        stubGet("/vat-obligations/999999999/obligations?status=O", Json.toJson(expectedResults).toString())

        val result = await(connector.getObligations("999999999"))
        result shouldBe Right(expectedResults)
      }
    }

    "response is 400" should {

      "return an error model" in {
        val expectedResults = ErrorModel(BAD_REQUEST, "{}")

        stubGet("/vat-obligations/999999999/obligations?status=O","{}",BAD_REQUEST)

        val result = await(connector.getObligations("999999999"))
        result shouldBe Left(expectedResults)
      }
    }
  }
}
