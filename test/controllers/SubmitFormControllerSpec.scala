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

import base.BaseSpec
import connectors.httpParsers.ResponseHttpParsers.HttpGetResult
import models.CustomerDetails
import models.errors.UnexpectedJsonFormat
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.http.Status
import play.api.mvc.Result
import play.api.test.Helpers._
import services.VatSubscriptionService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class SubmitFormControllerSpec extends BaseSpec {

  private trait Test {

    val customerInformation: CustomerDetails = CustomerDetails(
      Some("Test"), Some("User"), Some("ABC Solutions"), Some("ABCL"), hasFlatRateScheme = true
    )

    val vatSubscriptionResponse: Future[HttpGetResult[CustomerDetails]] = Future.successful(Right(customerInformation))
    val vatSubscriptionErrorResponse: Future[HttpGetResult[CustomerDetails]] = Future.successful(Left(UnexpectedJsonFormat))
    val mockVatSubscriptionService: VatSubscriptionService = mock[VatSubscriptionService]

    def setupMocks(): Unit = {}

    implicit val hc: HeaderCarrier = HeaderCarrier()

    def target: SubmitFormController = {
      setupMocks()
      new SubmitFormController(
        messagesApi,
        mockVatSubscriptionService,
        mockErrorHandler,
        mockAppConfig
      )
    }
  }

  "SubmitFormController .show" when {

    "a successful response is received from the service" should {

      "return 200" in new Test {

        override def setupMocks(): Unit = {
          (mockVatSubscriptionService.getCustomerDetails(_: String)(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, *, *)
            .returns(vatSubscriptionResponse)
        }

        val result: Future[Result] = target.show("18AA")(fakeRequest)
        status(result) shouldBe Status.OK
      }

      "return HTML" in new Test {

        override def setupMocks(): Unit = {
          (mockVatSubscriptionService.getCustomerDetails(_: String)(_: HeaderCarrier, _: ExecutionContext))
            .expects(* , *, *)
            .returns(vatSubscriptionResponse)
        }

        val result: Future[Result] = target.show("18AA")(fakeRequest)
        contentType(result) shouldBe Some("text/html")
      }

    }

    "an error response is returned from the service" should {

      "return an internal server status" in new Test {

        override val vatSubscriptionResponse: Future[HttpGetResult[CustomerDetails]] = Future.successful(Left(UnexpectedJsonFormat))

        override def setupMocks(): Unit = {
          (mockVatSubscriptionService.getCustomerDetails(_: String)(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, *, *)
            .returns(vatSubscriptionErrorResponse)
        }

        val result: Future[Result] = target.show("18AA")(fakeRequest)
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }

      "return the error page" in new Test {
        override val vatSubscriptionResponse: Future[HttpGetResult[CustomerDetails]] = Future.successful(Left(UnexpectedJsonFormat))

        override def setupMocks(): Unit = {
          (mockVatSubscriptionService.getCustomerDetails(_: String)(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, *, *)
            .returns(vatSubscriptionErrorResponse)
        }

        val result: Future[Result] = target.show("18AA")(fakeRequest)
        lazy val document: Document = Jsoup.parse(bodyOf(result))
        document.select("h1").text shouldBe "Internal server error"
      }
    }
  }
}