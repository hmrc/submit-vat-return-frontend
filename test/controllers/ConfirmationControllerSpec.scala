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

package controllers

import auth.AuthKeys
import base.BaseSpec
import common.{MandationStatuses, SessionKeys}
import mocks.{MockAuth, MockMandationPredicate}
import mocks.service.{MockVatObligationsService, MockVatSubscriptionService}
import play.api.http.Status
import play.api.test.Helpers._
import views.html.ConfirmationView

import scala.concurrent.Future

class ConfirmationControllerSpec extends BaseSpec with MockVatSubscriptionService with MockVatObligationsService
  with MockAuth with MockMandationPredicate {

  val view: ConfirmationView = inject[ConfirmationView]

  object TestConfirmationController extends ConfirmationController(
    mockMandationStatusPredicate,
    mockAuthPredicate,
    mcc,
    view,
    mockAppConfig
  )

  def viewAsString: String = view()(messages, mockAppConfig, user).toString

  "ConfirmationController .show" when {

    "user is authorised" should {

      lazy val result = {
        mockAuthorise(mtdVatAuthorisedResponse)
        TestConfirmationController.show()(fakeRequest.withSession(SessionKeys.mandationStatus -> MandationStatuses.nonDigital))
      }

      "return a success response for .show" in {
        status(result) shouldBe Status.OK
      }

      "return HTML for .show" in {
        contentType(result) shouldBe Some("text/html")
      }

      "return the correct view" in {
        contentAsString(result) shouldBe viewAsString
      }
    }

    authControllerChecks(TestConfirmationController.show(), fakeRequest)
  }

  "ConfirmationController .submit as an agent" when {

    "user is authorised" should {

      lazy val result = {
        mockAuthoriseAsAgent(agentAuthorisedResponse, Future(agentServicesEnrolment))
        TestConfirmationController.submit()(fakeRequest
          .withSession(SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB)
          .withSession(SessionKeys.inSessionPeriodKey -> "19AA")
          .withSession(SessionKeys.submissionYear -> "2019")
          .withSession(AuthKeys.agentSessionVrn -> vrn)
        )
      }

      "return a see other response for .submit" in {
        status(result) shouldBe Status.SEE_OTHER
      }
      "return the redirect location" in {
        redirectLocation(result) shouldBe Some("/agent-action")
      }
    }

    authControllerChecks(TestConfirmationController.submit(), fakeRequest)
  }

  "ConfirmationController .submit as a non agent" when {

    "user is authorised" should {

      lazy val result = {
        mockAuthorise(mtdVatAuthorisedResponse)
        TestConfirmationController.submit()(fakeRequest.withSession(SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB))
      }

      "return a see other response for .submit" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      s"redirect to ${mockAppConfig.vatSummaryUrl}" in {
        redirectLocation(result) shouldBe Some(mockAppConfig.vatSummaryUrl)
      }
    }
    authControllerChecks(TestConfirmationController.submit(), fakeRequest)
  }

}
