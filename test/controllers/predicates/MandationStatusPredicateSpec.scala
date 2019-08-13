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

package controllers.predicates

import common.{MandationStatuses, SessionKeys}
import mocks.MockMandationPredicate
import models.MandationStatus
import models.auth.User
import models.errors.BadRequestError
import org.jsoup.Jsoup
import play.api.http.Status.{BAD_REQUEST, SEE_OTHER}
import play.api.mvc.AnyContentAsEmpty
import play.mvc.Http.Status
import play.api.test.Helpers._
import base.BaseSpec
import play.api.test.FakeRequest
import assets.messages.MtdMandationMessages

class MandationStatusPredicateSpec extends BaseSpec with MockMandationPredicate {

  "Mandation status predicate is called" when {

    "the mandation status is in session" should {

      "a supported mandation status is retrieved (NON MTDfB)" should {

        lazy val userWithSession: User[AnyContentAsEmpty.type] =
          User[AnyContentAsEmpty.type]("123456789")(fakeRequest.withSession(SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB))

        lazy val result = {
          await(mockMandationStatusPredicate.refine(userWithSession))
        }

        "allow the request to pass through the predicate" in {
          result shouldBe Right(userWithSession)
        }
      }

      "a non supported mandation status is retrieved" should {

        lazy val userWithSession: User[AnyContentAsEmpty.type] =
          User[AnyContentAsEmpty.type]("123456789")(fakeRequest.withSession(SessionKeys.mandationStatus -> "unsupportedMandationStatus"))

        lazy val result = {
          await(mockMandationStatusPredicate.refine(userWithSession)).left.get
        }

        "return a forbidden" in {
          status(result) shouldBe Status.FORBIDDEN
        }

        "show unsupported mandation status error view" in {
          Jsoup.parse(bodyOf(result)).title shouldBe MtdMandationMessages.title
        }
      }

    }

    "the mandation status is not in session" should {

      lazy val fakeRequestWithoutSession: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")

      "a mandation status is successfully retrieved" should {

        lazy val userWithoutSession: User[AnyContentAsEmpty.type] = User[AnyContentAsEmpty.type]("123456789")(fakeRequestWithoutSession)

        lazy val result = {
          setupMockMandationStatus(Right(MandationStatus(MandationStatuses.nonMTDfB)))
          await(mockMandationStatusPredicate.refine(userWithoutSession)).left.get
        }

        "return a 303" in {
          status(result) shouldBe SEE_OTHER
        }

        s"redirect to ${userWithoutSession.uri}" in {
          redirectLocation(result) shouldBe Some(userWithoutSession.uri)
        }

        "add the mandation status in session" in {
          result.session.get(SessionKeys.mandationStatus) shouldBe Some(MandationStatuses.nonMTDfB)
        }
      }

      "a mandation status is unsuccessfully retrieved" should {

        lazy val userWithoutSession: User[AnyContentAsEmpty.type] = User[AnyContentAsEmpty.type]("123456789")(fakeRequestWithoutSession)

        lazy val result = {
          setupMockMandationStatus(Left(BadRequestError(BAD_REQUEST.toString, "Error response")))
          await(mockMandationStatusPredicate.refine(userWithoutSession)).left.get
        }

        "return an internal server error" in {
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }

        "display the internal sever error page" in {
          Jsoup.parse(bodyOf(result)).title shouldBe "Sorry, we are experiencing technical difficulties - 500"
        }

      }
    }
  }
}
