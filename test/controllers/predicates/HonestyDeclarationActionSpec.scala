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

package controllers.predicates

import base.BaseSpec
import models.auth.User
import play.api.mvc.Result
import play.api.mvc.Results.Ok
import play.api.test.Helpers._
import scala.concurrent.Future

class HonestyDeclarationActionSpec extends BaseSpec {

  "HonestyDeclarationAction .authoriseForPeriodKey" when {

    val honestyDeclarationAction = new HonestyDeclarationAction()(ec)
    def block[A]: User[A] => Future[Result] = { _ => Future(Ok("Test")) }

    "mtdVatHonestyDeclaration is in session" when {

      "VRN and period key match those of request" should {

        lazy val request = User("123456789")(fakeRequest.withSession(
          "mtdVatHonestyDeclaration" -> "123456789-19AA"
        ))

        lazy val result = honestyDeclarationAction.authoriseForPeriodKey("19AA").invokeBlock(
          request,
          block
        )

        "return the original request" in {
          status(result) shouldBe 200
          contentAsString(result) shouldBe "Test"
        }
      }

      "VRN and period key do not match those of request" should {

        lazy val request = User("123456789")(fakeRequest.withSession(
          "mtdVatHonestyDeclaration" -> "123456789-19AB"
        ))

        lazy val result = honestyDeclarationAction.authoriseForPeriodKey("19AA").invokeBlock(
          request,
          block
        )

        "redirect to /return-deadlines" in {
          status(result) shouldBe 303
          redirectLocation(result) shouldBe Some(s"${controllers.routes.HonestyDeclarationController.show("19AA")}")
        }

        "remove value of current mtdVatHonestyDeclaration session key" in {
          await(result).session.get("mtdVatHonestyDeclaration") shouldBe None
        }
      }
    }

    "mtdVatHonestyDeclaration is not in session" should {

      lazy val request = User("123456789")(fakeRequest.withSession(
        "someOtherKey" -> "abcd"
      ))

      lazy val result = honestyDeclarationAction.authoriseForPeriodKey("19AA").invokeBlock(
        request,
        block
      )

      "redirect to /return-deadlines" in {
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some(s"${controllers.routes.HonestyDeclarationController.show("19AA")}")
      }
    }
  }
}
