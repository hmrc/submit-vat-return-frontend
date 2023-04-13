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

package controllers

import base.BaseSpec
import common.{MandationStatuses, SessionKeys}
import mocks.{MockAuth, MockMandationPredicate}
import play.api.http.Status
import play.api.test.Helpers._
import views.html.ConfirmationView

class ConfirmationControllerSpec extends BaseSpec with MockAuth with MockMandationPredicate {

  val view: ConfirmationView = inject[ConfirmationView]

  object TestConfirmationController extends ConfirmationController(
    mockMandationStatusPredicate,
    mockAuthPredicate,
    mcc,
    view
  )

  def viewAsString: String = view()(messages, mockAppConfig, user).toString

  "ConfirmationController .show" when {

    "the user is authorised" should {

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
}
