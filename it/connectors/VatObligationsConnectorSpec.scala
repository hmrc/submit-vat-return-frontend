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

package connectors

import java.time.LocalDate

import play.api.http.Status._
import base.BaseISpec
import models.errors.UnknownError
import models.{VatObligation, VatObligations}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

class VatObligationsConnectorSpec extends BaseISpec {

  val connector: VatObligationsConnector = new VatObligationsConnector(httpClient, appConfig)
  val startDate: LocalDate = LocalDate.now()
  val endDate: LocalDate = LocalDate.now()
  val dueDate: LocalDate = LocalDate.now()

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "Calling .getObligations" when {


    "response is 200" should {

      "return a sequence of obligations" in {
        val expectedResults = VatObligations(Seq (
          VatObligation(startDate,endDate,dueDate,"#001")
        ))

        stubGet("/vat-obligations/vrn/obligations?status=O",Json.toJson(expectedResults).toString())

        val result = await(connector.getObligations("vrn"))
        result shouldBe Right(expectedResults)

      }
    }

    "response is 400" should {

      "return a BadRequestError" in {
        val expectedResults = UnknownError
        stubGet("/vat-obligations/vrn/obligations?status=O","{}",BAD_REQUEST)

        val result = await(connector.getObligations("vrn"))
        result shouldBe Left(expectedResults)

      }
    }
  }
}
