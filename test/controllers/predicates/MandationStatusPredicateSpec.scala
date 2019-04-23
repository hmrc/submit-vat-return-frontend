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
import mocks.MockPredicate
import mocks.service.MockMandationStatusService
import models.MandationStatus
import models.errors.BadRequestError
import play.api.mvc.Results.Redirect
import play.api.http.Status.BAD_REQUEST

class MandationStatusPredicateSpec extends MockPredicate with MockMandationStatusService {

  "Mandation status predicate is called" when {

    "a supported mandation status is retrieved (NON MTDfB)" should {

      lazy val result = {
        setupMockMandationStatus(Right(MandationStatus(nonMTDfB)))
        await(mockMandationStatusPredicate.refine(fakeRequest))
      }

      "allow the request to pass through the predicate" in {

        result shouldBe Right(fakeRequest)

      }
    }

    "a non supported mandation status is retrieved" should {

      lazy val result = {
        setupMockMandationStatus(Right(MandationStatus("NON SUPPORTED MANDATION STATUS")))
        await(mockMandationStatusPredicate.refine(fakeRequest)).left.get
      }

      //TODO: redirect back to other page once discussed with design
      "block the request and redirect back to the HelloWorld route" in {
        result shouldBe Redirect(controllers.routes.HelloWorldController.helloWorld())
      }
    }

    "an error is retrieved" should {

      lazy val result = {
        setupMockMandationStatus(Left(BadRequestError(BAD_REQUEST.toString, "Error response")))
        await(mockMandationStatusPredicate.refine(fakeRequest)).left.get
      }

      //TODO: redirect back to other page once discussed with design
      "block the request and redirect back to the HelloWorld route" in {
        result shouldBe Redirect(controllers.routes.HelloWorldController.helloWorld())
      }
    }
  }
}
