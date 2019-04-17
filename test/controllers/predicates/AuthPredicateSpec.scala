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

import mocks.MockAuth
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.mvc.Results.Ok
import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core.{BearerTokenExpired, InsufficientEnrolments, UnsupportedCredentialRole}

import scala.concurrent.Future

class AuthPredicateSpec extends MockAuth {

  def target(): Action[AnyContent] = mockAuthPredicate.async {
    implicit user => Future.successful(Ok("hello"))
  }

  "Calling .invokeBlock" when {

    "user is Agent" when {

      "the session contains CLIENT_VRN" when {

        "agent has delegated authority for the VRN" when {

          "agent has HMRC-AS-AGENT enrolment" should {

            val authResponse = Future.successful(new ~(Some(Agent), agentServicesEnrolment))
            lazy val result = target()(FakeRequest().withSession("CLIENT_VRN" -> "999999999"))

            "allow the request through" in {
              mockAuthoriseAsAgent(authResponse, authResponse.b)

              status(result) shouldBe Status.OK
            }
          }

          "agent does not have HMRC-AS-AGENT enrolment" should {

            val authResponse = Future.successful(new ~(Some(Agent), otherEnrolment))
            lazy val result = target()(FakeRequest().withSession("CLIENT_VRN" -> "999999999"))

            "return 403" in {
              mockAuthoriseAsAgent(authResponse, authResponse.b)

              status(result) shouldBe Status.FORBIDDEN
            }

            "render Agent unauthorised view" in {
              Jsoup.parse(bodyOf(result)).title() shouldBe "You can’t use this service yet"
            }
          }
        }

        "agent does not have delegated authority for the VRN" when {

          val authResponse = Future.successful(new ~(Some(Agent), otherEnrolment))
          lazy val result = target()(FakeRequest().withSession("CLIENT_VRN" -> "999999999"))

          "return 303" in {
            mockAuthoriseAsAgent(authResponse, Future.failed(InsufficientEnrolments()))

            status(result) shouldBe Status.SEE_OTHER
          }

          s"redirect to ${mockAppConfig.agentClientUnauthorisedUrl}" in {
            redirectLocation(result) shouldBe Some(mockAppConfig.agentClientUnauthorisedUrl)
          }
        }

        "auth returns a NoActiveSession exception" should {

          val authResponse = Future.successful(new ~(Some(Agent), otherEnrolment))
          lazy val result = target()(FakeRequest().withSession("CLIENT_VRN" -> "999999999"))

          "return 303" in {
            mockAuthoriseAsAgent(authResponse, Future.failed(BearerTokenExpired()))

            status(result) shouldBe Status.SEE_OTHER
          }

          s"redirect to ${mockAppConfig.signInUrl}" in {
            redirectLocation(result) shouldBe Some(mockAppConfig.signInUrl)
          }
        }
      }

      "the session does not contain CLIENT_VRN" should {

        val authResponse = Future.successful(new ~(Some(Agent), agentServicesEnrolment))
        lazy val result = target()(FakeRequest())

        "return 303" in {
          mockAuthorise(authResponse)

          status(result) shouldBe Status.SEE_OTHER
        }

        s"redirect to ${mockAppConfig.agentClientLookupStartUrl}" in {
          redirectLocation(result) shouldBe Some(mockAppConfig.agentClientLookupStartUrl)
        }
      }
    }

    "user is non-Agent" when {

      "user has HMRC-MTD-VAT enrolment" should {

        val authResponse = Future.successful(new ~(Some(Individual), mtdVatEnrolment))
        lazy val result = target()(FakeRequest())

        "allow the request through" in {
          mockAuthorise(authResponse)

          status(result) shouldBe Status.OK
        }
      }

      "user does not have HMRC-MTD-VAT enrolment" should {

        val authResponse = Future.successful(new ~(Some(Individual), otherEnrolment))
        lazy val result = target()(FakeRequest())

        "return 403" in {
          mockAuthorise(authResponse)

          status(result) shouldBe Status.FORBIDDEN
        }

        "render the unauthorised view" in {
          Jsoup.parse(bodyOf(result)).title() shouldBe "You can’t use this service yet"
        }
      }
    }

    "affinity group is not returned" should {

      val authResponse = Future.successful(new ~(None, otherEnrolment))
      lazy val result = target()(FakeRequest())

      "return 500" in {
        mockAuthorise(authResponse)

        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }

      "render ISE page" in {
        Jsoup.parse(bodyOf(result)).title() shouldBe "Sorry, we are experiencing technical difficulties - 500"
      }
    }

    "auth returns a NoActiveSession exception" should {

      val authResponse = Future.failed(BearerTokenExpired())
      lazy val result = target()(FakeRequest())

      "return 303" in {
        mockAuthorise(authResponse)

        status(result) shouldBe Status.SEE_OTHER
      }

      s"redirect to ${mockAppConfig.signInUrl}" in {
        redirectLocation(result) shouldBe Some(mockAppConfig.signInUrl)
      }
    }

    "auth returns AuthorisationException" should {

      val authResponse = Future.failed(UnsupportedCredentialRole())
      lazy val result = target()(FakeRequest())

      "return 500" in {
        mockAuthorise(authResponse)

        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }

      "render ISE page" in {
        Jsoup.parse(bodyOf(result)).title() shouldBe "Sorry, we are experiencing technical difficulties - 500"
      }
    }
  }
}
