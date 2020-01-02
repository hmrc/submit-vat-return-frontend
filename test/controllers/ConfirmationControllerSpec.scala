/*
 * Copyright 2020 HM Revenue & Customs
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

class ConfirmationControllerSpec extends BaseSpec with MockVatSubscriptionService with MockVatObligationsService with MockAuth with MockMandationPredicate {

  object TestConfirmationController extends ConfirmationController(
    messagesApi,
    mockMandationStatusPredicate,
    mockAuthPredicate,
    mockAppConfig
  )

  "SubmitFormController .show" when {

    "user is authorised" should {

      lazy val result = {
        mockAuthorise(mtdVatAuthorisedResponse)
        TestConfirmationController.show()(fakeRequest.withSession(SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB))
      }
      "return a success response for .show" in {
        status(result) shouldBe Status.OK
      }

      "return HTML for .show" in {
        contentType(result) shouldBe Some("text/html")
      }
    }
    authControllerChecks(TestConfirmationController.show(), fakeRequest)
  }

  "SubmitFormController .submit as an agent" when {

    "user is authorised" should {

      lazy val result = {
        mockAuthorise(agentAuthorisedResponse)
        TestConfirmationController.submit()(fakeRequest
          .withSession(SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB)
            .withSession(SessionKeys.inSessionPeriodKey -> "19AA")
            .withSession(SessionKeys.submissionYear -> "2019")
        )

      }

      "return a see other response for .submit" in {
        status(result) shouldBe Status.SEE_OTHER
      }
    }
    authControllerChecks(TestConfirmationController.submit(), fakeRequest)
  }

  "SubmitFormController .submit as a non agent" when {

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
