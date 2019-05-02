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

import common.MandationStatuses.nonMTDfB
import mocks.MockMandationPredicate
import mocks.service.MockMandationStatusService
import models.MandationStatus
import models.auth.User
import models.errors.BadRequestError
import org.jsoup.Jsoup
import play.api.http.Status.BAD_REQUEST
import play.api.mvc.AnyContentAsEmpty
import play.mvc.Http.Status

class MandationStatusPredicateSpec extends MockMandationPredicate with MockMandationStatusService {

  val userWithSession: User[AnyContentAsEmpty.type] =
    User[AnyContentAsEmpty.type]("123456789")(fakeRequest)

  "Mandation status predicate is called" when {

    "a supported mandation status is retrieved (NON MTDfB)" should {

      lazy val result = {
        setupMockMandationStatus(Right(MandationStatus(nonMTDfB)))
        await(mockMandationStatusPredicate.refine(userWithSession))
      }

      "allow the request to pass through the predicate" in {
        result shouldBe Right(userWithSession)
      }
    }

    "a non supported mandation status is retrieved" should {

      lazy val result = {
        setupMockMandationStatus(Right(MandationStatus("NON SUPPORTED MANDATION STATUS")))
        await(mockMandationStatusPredicate.refine(userWithSession)).left.get
      }

      "return a forbidden" in {
        status(result) shouldBe Status.FORBIDDEN
      }

      //TODO: Update test once correct page is in play
      "show unsupported mandation status error view" in {
        Jsoup.parse(bodyOf(result)).title shouldBe "You can’t use this service yet"
      }
    }

    "an error is retrieved" should {

      lazy val result = {
        setupMockMandationStatus(Left(BadRequestError(BAD_REQUEST.toString, "Error response")))
        await(mockMandationStatusPredicate.refine(userWithSession)).left.get
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
