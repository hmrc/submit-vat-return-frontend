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

package controllers

import java.time.LocalDate

import base.BaseSpec
import connectors.httpParsers.ResponseHttpParsers.HttpGetResult
import mocks.{MockVatObligationsService, MockVatSubscriptionService}
import models.{CustomerDetails, VatObligation, VatObligations}
import models.errors.UnexpectedJsonFormat
import play.api.http.Status
import play.api.test.Helpers._

import scala.concurrent.Future

class SubmitFormControllerSpec extends BaseSpec with MockVatSubscriptionService with MockVatObligationsService {

  object TestSubmitFormController extends SubmitFormController(
    messagesApi,
    mockVatSubscriptionService,
    mockVatObligationsService,
    errorHandler,
    mockAppConfig
  )

  "SubmitFormController .show" when {

    "a successful response is received from the service" should {

      val customerInformation: CustomerDetails = CustomerDetails(
        Some("Test"), Some("User"), Some("ABC Solutions"), Some("ABCL"), hasFlatRateScheme = true
      )

      val obligations: VatObligations = VatObligations(Seq(VatObligation(LocalDate.parse("2019-01-12"), LocalDate.parse("2019-04-12"), LocalDate.parse("2019-05-12"), "18AA")))

      val vatSubscriptionResponse: Future[HttpGetResult[CustomerDetails]] = Future.successful(Right(customerInformation))
      val vatObligationsResponse: Future[HttpGetResult[VatObligations]] = Future.successful(Right(obligations))

      lazy val result = TestSubmitFormController.show("18AA")(fakeRequest)

      "return 200" in {
        setupVatSubscriptionService(vatSubscriptionResponse)
        setupVatObligationsService(vatObligationsResponse)
        status(result) shouldBe Status.OK
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
      }

    }

    "an error response is returned from the service" should {

      "return an internal server status" in {

        val vatSubscriptionErrorResponse: Future[HttpGetResult[CustomerDetails]] = Future.successful(Left(UnexpectedJsonFormat))
        val vatObligationsErrorResponse: Future[HttpGetResult[VatObligations]] = Future.successful(Left(UnexpectedJsonFormat))

        setupVatSubscriptionService(vatSubscriptionErrorResponse)
        setupVatObligationsService(vatObligationsErrorResponse)

        lazy val result = TestSubmitFormController.show("18AA")(fakeRequest)
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }
    }
  }
}