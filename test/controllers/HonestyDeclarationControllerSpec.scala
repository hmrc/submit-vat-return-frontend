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

import base.BaseSpec
import common.{MandationStatuses, SessionKeys}
import mocks.{MockAuth, MockMandationPredicate}
import play.api.http.Status
import play.api.test.Helpers.contentType
import play.api.test.Helpers._


class HonestyDeclarationControllerSpec extends BaseSpec with MockAuth with MockMandationPredicate {

  object TestHonestyDeclarationController extends HonestyDeclarationController (
    messagesApi,
    mockMandationStatusPredicate,
    errorHandler,
    mockAuthPredicate,
    mockAppConfig
  )

  "HonestyDeclarationController .show" when {

    "user is authorised" should {

      lazy val result = {
        TestHonestyDeclarationController.show("18AA")(fakeRequest.withSession(SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB))
      }

      "return 200" in {
        mockAuthorise(mtdVatAuthorisedResponse)
        status(result) shouldBe Status.OK
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
      }
    }
  }

  "HonestyDeclarationController .submit" when {

    "user is authenticated" should {

      lazy val result = {
        TestHonestyDeclarationController.submit("18AA")(fakeRequest.withSession(SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB))
      }

      "status is SEE_OTHER" in {
        mockAuthorise(mtdVatAuthorisedResponse)
        status(result) shouldBe SEE_OTHER
      }

      s"redirect to ${controllers.routes.ConfirmationController.show()}" in {
        redirectLocation(result).get.contains(controllers.routes.SubmitFormController.show("18AA").url) shouldBe true
      }

    }

  }

}
