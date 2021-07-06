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
import mocks.{MockAuth, MockDDInterruptPredicate}
import play.api.mvc.{AnyContent, Request, Result}
import play.mvc.Http.Status
import play.api.test.Helpers._
import play.api.test.FakeRequest
import play.api.mvc.Results.Ok
import scala.concurrent.Future
class DDInterruptPredicateSpec extends MockAuth with MockDDInterruptPredicate {

  def target(request: Request[AnyContent]): Future[Result] = mockDDInterruptPredicate.interruptCheck({
    _ => Ok("Welcome")
  })(request)

  ".interruptCheck" when {

    "the user has the DDInterrupt viewed value of true in session" should {

      "allow the user to pass through the predicate" in {

        val result = target(ddRequest)
        status(result) shouldBe Status.OK
      }
    }

    "the user has no interrupt value in session" should {

      lazy val result = target(FakeRequest("GET","/homepage"))

      "the user should be redirected" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "check the redirect location" in {
        redirectLocation(result) shouldBe Some(s"${mockAppConfig.directDebitInterruptUrl}?redirectUrl=${mockAppConfig.platformHost}/homepage")
      }
    }
  }
}
