/*
 * Copyright 2021 HM Revenue & Customs
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
import forms.HonestyDeclarationForm
import mocks.{MockAuth, MockDDInterruptPredicate, MockMandationPredicate}
import play.api.data.Form
import play.api.http.Status
import play.api.test.Helpers.contentType
import play.api.test.Helpers._
import views.html.HonestyDeclaration

class HonestyDeclarationControllerSpec extends BaseSpec with MockAuth with MockMandationPredicate with MockDDInterruptPredicate {

  val honestyDeclaration: HonestyDeclaration = inject[HonestyDeclaration]

  object TestHonestyDeclarationController extends HonestyDeclarationController(
    mockMandationStatusPredicate,
    errorHandler,
    mockAuthPredicate,
    mcc,
    honestyDeclaration,
    mockDDInterruptPredicate,
    mockAppConfig
  )

  def viewAsString(form: Form[Boolean]): String = honestyDeclaration("18AA", form)(messages, mockAppConfig, user).toString

  "HonestyDeclarationController .show" when {

    "user is authorised" should {

      lazy val result = {
        TestHonestyDeclarationController.show("18AA")(fakeRequest
          .withSession(
            SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB,
            SessionKeys.HonestyDeclaration.key -> "true",
            SessionKeys.viewedDDInterrupt -> "true"
          )
        )
      }

      "return 200" in {
        mockAuthorise(mtdVatAuthorisedResponse)
        status(result) shouldBe Status.OK
      }

      "remove the honesty declaration session key from session" in {
        await(result).session.get(SessionKeys.HonestyDeclaration.key) shouldBe None
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
      }

      "return the correct view" in {
        contentAsString(result) shouldBe viewAsString(HonestyDeclarationForm.honestyDeclarationForm)
      }
    }

    "user has no viewDDInterrupt in session" should {

      lazy val result = {
        TestHonestyDeclarationController.show("18BB")(fakeRequest
          .withSession(
            SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB,
            SessionKeys.HonestyDeclaration.key -> "true",
          )
        )
      }

      "return 303" in {
        mockAuthorise(mtdVatAuthorisedResponse)
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to the correct redirect location" in {
        redirectLocation(result) shouldBe Some(s"${mockAppConfig.directDebitInterruptUrl}?redirectUrl=${mockAppConfig.platformHost}")
      }
    }

    authControllerChecks(TestHonestyDeclarationController.show("18AA"), fakeRequest)
  }

  "HonestyDeclarationController .submit" when {

    "user is authenticated" when {

      "a valid form is submitted" should {

        lazy val result = {
          TestHonestyDeclarationController.submit("18AA")(fakeRequest.withFormUrlEncodedBody(
            "checkbox" -> "true"
          ).withSession(SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB))
        }

        "status is SEE_OTHER" in {
          mockAuthorise(mtdVatAuthorisedResponse)
          status(result) shouldBe SEE_OTHER
        }

        s"redirect to ${controllers.routes.SubmitFormController.show("18AA")}" in {
          redirectLocation(result).get.contains(controllers.routes.SubmitFormController.show("18AA").url) shouldBe true
        }

        "save the honesty declaration session key to session" in {
          await(result).session.get(SessionKeys.HonestyDeclaration.key) shouldBe Some(s"$vrn-18AA")
        }
      }

      "no form is submitted" should {

        lazy val result = {
          TestHonestyDeclarationController.submit("18AA")(fakeRequest.withSession(SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB))
        }

        "status is BAD_REQUEST" in {
          mockAuthorise(mtdVatAuthorisedResponse)
          status(result) shouldBe BAD_REQUEST
        }
      }
    }

    authControllerChecks(TestHonestyDeclarationController.submit("18AA"), fakeRequest)
  }

}
