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
import common.{MandationStatuses, SessionKeys}
import assets.CustomerDetailsTestAssets._
import common.MandationStatuses.nonMTDfB
import connectors.httpParsers.ResponseHttpParsers.HttpGetResult
import mocks.service.MockVatSubscriptionService
import mocks.{MockAuth, MockMandationPredicate}
import models.auth.User
import models.errors.UnexpectedJsonFormat
import models.{CustomerDetails, MandationStatus, SubmitVatReturnModel}
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.Helpers._

import scala.concurrent.Future

class ConfirmSubmissionControllerSpec extends BaseSpec with MockAuth with MockMandationPredicate with MockVatSubscriptionService {

  object TestConfirmSubmissionController extends ConfirmSubmissionController(
    messagesApi,
    mockMandationStatusPredicate,
    errorHandler,
    mockVatSubscriptionService,
    mockAuthPredicate,
    mockAppConfig
  )

  "ConfirmSubmissionController .show" when {

    "user is authorised" when  {

      val nineBoxModel: String = Json.stringify(Json.toJson(
        SubmitVatReturnModel(
          1000.00,
          1000.00,
          1000.00,
          1000.00,
          1000.00,
          1000.00,
          1000.00,
          1000.00,
          1000.00,
          flatRateScheme = true,
          LocalDate.now(),
          LocalDate.now(),
          LocalDate.now()
        )
      ))

      "there is session data" when {

        "a successful response is returned from the vat subscription service" should {

          val vatSubscriptionResponse: Future[HttpGetResult[CustomerDetails]] = Future.successful(Right(customerDetailsWithFRS))

          lazy val requestWithSessionData: User[AnyContentAsEmpty.type] =
            User[AnyContentAsEmpty.type]("123456789")(fakeRequest.withSession(
              SessionKeys.viewModel -> nineBoxModel,
              SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB
              )
            )

          lazy val result: Future[Result] = {
            setupVatSubscriptionService(vatSubscriptionResponse)
            TestConfirmSubmissionController.show("18AA")(requestWithSessionData)
          }

          "return 200" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            status(result) shouldBe Status.OK
          }

          "return HTML" in {
            contentType(result) shouldBe Some("text/html")
          }

          "the user name should be displayed" in {
            Jsoup.parse(bodyOf(result)).select("#content > article > section > h2").text() shouldBe "ABC Solutions"
          }
        }

        "an error response is returned from the vat subscription service" should {

          val vatSubscriptionResponse: Future[HttpGetResult[CustomerDetails]] = Future.successful(Left(UnexpectedJsonFormat))

          lazy val requestWithSessionData: User[AnyContentAsEmpty.type] =
            User[AnyContentAsEmpty.type]("123456789")(fakeRequest.withSession(
              SessionKeys.viewModel -> nineBoxModel,
              SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB
            )
            )

          lazy val result: Future[Result] = {
            setupVatSubscriptionService(vatSubscriptionResponse)
            TestConfirmSubmissionController.show("18AA")(requestWithSessionData)
          }

          "return 200" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            status(result) shouldBe Status.OK
          }

          "return HTML" in {
            contentType(result) shouldBe Some("text/html")
          }

          "the user name should not be displayed" in {
            Jsoup.parse(bodyOf(result)).select("#content > article > div > h2").text() shouldBe ""
          }
        }
      }

      "there is no session data" should {

        lazy val result: Future[Result] = {
          TestConfirmSubmissionController.show("18AA")(fakeRequest.withSession(
            SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB)
          )
        }

        "return 303" in {
          mockAuthorise(mtdVatAuthorisedResponse)
          status(result) shouldBe Status.SEE_OTHER
        }

        s"redirect to ${controllers.routes.SubmitFormController.show("18AA").url}" in {
          redirectLocation(result) shouldBe Some(controllers.routes.SubmitFormController.show("18AA").url)
        }
      }
    }
    authControllerChecks(TestConfirmSubmissionController.show("18AA"), fakeRequest)
  }

  "ConfirmSubmissionController .submit" when {
    "user is authorised" should {

      lazy val result: Future[Result] = TestConfirmSubmissionController.submit("18AA")(fakeRequest.withSession(
        SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB)
      )

      "return 303" in {
        mockAuthorise(mtdVatAuthorisedResponse)
        status(result) shouldBe Status.SEE_OTHER
      }

      s"redirect url should be ${controllers.routes.ConfirmationController.show()}" in {
        redirectLocation(result) shouldBe Some(controllers.routes.ConfirmationController.show().url)
      }
    }
    authControllerChecks(TestConfirmSubmissionController.submit("18AA"), fakeRequest)
  }
}
