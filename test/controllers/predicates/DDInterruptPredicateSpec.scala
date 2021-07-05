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

package controllers.predicates

import common.SessionKeys
import controllers.Assets.Redirect
import mocks.{MockAuth, MockDDInterruptPredicate}
import models.auth.User

class DDInterruptPredicateSpec extends MockAuth with MockDDInterruptPredicate {

  "The DDInterruptPredicate" should {

    "allow the request to pass through when the user has the DD session value" in {
      val userWithSession =
        User("999999999", arn = Some("XARN1234567"))(fakeRequest.withSession(SessionKeys.viewedDDInterrupt -> "true"))
      await(mockDDInterruptPredicate.refine(userWithSession)) shouldBe Right(userWithSession)
    }

    "redirect the request to the DDInterruptController when the user does not have the DD session value" in {
      await(mockDDInterruptPredicate.refine(user)) shouldBe
        Left(Redirect(s"${mockAppConfig.directDebitInterruptUrl}?redirectUrl=${mockAppConfig.platformHost}"))
    }
  }

}
