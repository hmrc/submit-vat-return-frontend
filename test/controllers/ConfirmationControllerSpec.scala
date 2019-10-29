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
import common.{MandationStatuses, SessionKeys}
import mocks.{MockAuth, MockMandationPredicate}
import mocks.service.{MockVatObligationsService, MockVatSubscriptionService}
import play.api.mvc.AnyContentAsEmpty

class ConfirmationControllerSpec extends BaseSpec with MockVatSubscriptionService with MockVatObligationsService with MockAuth with MockMandationPredicate {

  object TestConfirmationController extends ConfirmationController(
    messagesApi,
    mockMandationStatusPredicate,
    mockAuthPredicate,
    mockAppConfig
  )

//  "SubmitFormController .show" when {
//
//    "user is authorised" should {
//
//      mockAuthorise(mtdVatAuthorisedResponse)
//
//      lazy val result = TestConfirmationController.show()(fakeRequest.withSession(SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB))
//
//      "return a success response" in {
//        status(result) shouldBe Status.OK
//      }
//
//      "return HTML" in {
//        contentType(result) shouldBe Some("text/html")
//      }
//    }
//  }
//
//  authControllerChecks(TestConfirmationController.show(), fakeRequest)

  "SubmitFormController .submit" when {

    "user is authorised" should {

      mockAuthorise(agentAuthorisedResponse)
      lazy val result = TestConfirmationController.submit()(fakeRequest.withSession(SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB))

      "return a success response" in {
        status(result) shouldBe Status.OK
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
      }
    }
  }

  authControllerChecks(TestConfirmationController.submit(), fakeRequest)

}
