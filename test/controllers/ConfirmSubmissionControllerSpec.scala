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
import models.SubmitVatReturnModel
import mocks.{MockAuth, MockMandationPredicate}
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class ConfirmSubmissionControllerSpec extends BaseSpec with MockAuth with MockMandationPredicate {

  object TestConfirmSubmissionController extends ConfirmSubmissionController(
    messagesApi,
    mockMandationStatusPredicate,
    errorHandler,
    mockAuthPredicate,
    mockAppConfig
  )

  "ConfirmSubmissionController .show" when {

    "user is authorised" should {

      "there is session data" should {
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

        lazy val requestWithSessionData: FakeRequest[AnyContentAsEmpty.type] = fakeRequest.withSession(
          SessionKeys.viewModel -> nineBoxModel,
          SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB
        )

        lazy val result: Future[Result] = {
          TestConfirmSubmissionController.show("18AA")(requestWithSessionData)
        }

        "return 200" in {
          mockAuthorise(mtdVatAuthorisedResponse)
          status(result) shouldBe Status.OK
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
        }

        "remove the session data" in {
          session(result).get(SessionKeys.viewModel) shouldBe None
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
  }

  authControllerChecks(TestConfirmSubmissionController.show("18AA"), fakeRequest)
}
