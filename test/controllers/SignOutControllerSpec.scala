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
import play.api.mvc.Result
import play.api.test.Helpers._
import scala.concurrent.Future

class SignOutControllerSpec extends BaseSpec {

  private trait SignOutControllerTest {
    def target: SignOutController = {
      new SignOutController(messagesApi, mockAppConfig)
    }
  }

  "navigating to sign-out page" when {

    "show Exit Survey is true" should {
      "return 303 and navigate to the survey url" in new SignOutControllerTest {
        lazy val result: Future[Result] = target.signOut(feedbackOnSignOut = true)(fakeRequest)

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(mockAppConfig.signOutUrl)
      }
    }

    "show Exit Survey is false" should {
      "return 303 and navigate to sign out url" in new SignOutControllerTest {
        lazy val result: Future[Result] = target.signOut(feedbackOnSignOut = false)(fakeRequest)

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(mockAppConfig.signInUrl)
      }
    }

    "timeout is true" should {
      "return 303 and navigate to timeout url" in new SignOutControllerTest {
        lazy val result: Future[Result] = target.signOut(feedbackOnSignOut = true, timeout = true)(fakeRequest)

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(mockAppConfig.timeoutSignOutUrl)
      }
    }
  }

  "navigating to time-out" should {

    "return 401 unauthorised and render session timeout view" in new SignOutControllerTest {
      lazy val result: Future[Result] = target.timeout(fakeRequest)

      status(result) shouldBe UNAUTHORIZED
    }
  }
}
