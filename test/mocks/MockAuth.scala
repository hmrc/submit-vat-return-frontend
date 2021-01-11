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

package mocks

import akka.util.Timeout
import assets.NrsTestData.IdentityDataTestData
import base.BaseSpec
import controllers.predicates.AuthPredicate
import mocks.service.MockVatSubscriptionService
import org.joda.time.DateTime
import play.api.http.Status
import play.api.mvc.{Action, AnyContent, Request}
import play.api.test.Helpers.redirectLocation
import services.EnrolmentsAuthService
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{LoginTimes, Retrieval, ~}
import uk.gov.hmrc.http.HeaderCarrier
import views.html.errors.{UnauthorisedAgent, UnauthorisedNonAgent}

import scala.concurrent.{ExecutionContext, Future}

trait MockAuth extends BaseSpec with MockVatSubscriptionService {

  lazy val mockAuthConnector: AuthConnector = mock[AuthConnector]
  lazy val mockEnrolmentsAuthService: EnrolmentsAuthService = new EnrolmentsAuthService(mockAuthConnector)

  val unauthorisedAgent: UnauthorisedAgent = inject[UnauthorisedAgent]
  val unauthorisedNonAgent: UnauthorisedNonAgent = inject[UnauthorisedNonAgent]

  lazy val mockAuthPredicate: AuthPredicate = new AuthPredicate(
    mockEnrolmentsAuthService,
    mockVatSubscriptionService,
    errorHandler,
    mcc,
    unauthorisedAgent,
    unauthorisedNonAgent
  )

  def mockAuthorise(authResponse: Future[~[Option[AffinityGroup], Enrolments]]): Unit = {
    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returns(authResponse)
  }

  def mockAuthoriseAsAgent(firstAuthResponse: Future[~[Option[AffinityGroup], Enrolments]],
                           secondAuthResponse: Future[Enrolments]): Unit = {
    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returns(firstAuthResponse)

    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returns(secondAuthResponse)
  }

  def authControllerChecks(action: Action[AnyContent], request: Request[AnyContent])(implicit timeout: Timeout): Unit = {

    "user is unauthenticated" should {

      lazy val result = action(request)

      "return 303" in {
        mockAuthorise(Future.failed(BearerTokenExpired()))
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to sign-in" in {
        redirectLocation(result) shouldBe Some(mockAppConfig.signInUrl)
      }
    }

    "user is unauthorised" should {

      lazy val result = action(request)

      "return 403" in {
        mockAuthorise(Future.successful(new ~(Some(Individual), otherEnrolment)))
        status(result) shouldBe Status.FORBIDDEN
      }
    }

    "the user is insolvent and not continuing to trade" should {

      lazy val result = action(insolventRequest)

      "return 403 (Forbidden)" in {
        mockAuthorise(Future.successful(new ~(Some(Individual), otherEnrolment)))
        status(result) shouldBe Status.FORBIDDEN
      }
    }
  }

  private def createEnrolment(key: String,
                              identifierKey: String,
                              identifierValue: String,
                              delegatedAuthRule: Option[String] = None) = Enrolments(
    Set(
      Enrolment(
        key,
        Seq(EnrolmentIdentifier(identifierKey, identifierValue)),
        "Activated",
        delegatedAuthRule
      )
    )
  )

  val agentServicesEnrolment: Enrolments = createEnrolment("HMRC-AS-AGENT", "AgentReferenceNumber", "XAIT1234567", Some("mtd-vat-auth"))
  val agentServicesEnrolmentWithoutDelegatedAuth: Enrolments = createEnrolment("HMRC-AS-AGENT", "AgentReferenceNumber", "XAIT1234567", None)
  val mtdVatEnrolment: Enrolments = createEnrolment("HMRC-MTD-VAT", "VRN", "999999999")
  val otherEnrolment: Enrolments = createEnrolment("OTHER-ENROLMENT", "BLAH", "12345")
  val mtdVatAuthorisedResponse: Future[~[Option[AffinityGroup], Enrolments]] = Future.successful(new ~(Some(Individual), mtdVatEnrolment))
  val agentAuthorisedResponse: Future[~[Option[AffinityGroup], Enrolments]] = Future.successful(new ~(Some(Agent), agentServicesEnrolment))

  def mockFullAuthResponse[A](authResponse: Future[A]): Unit = {
    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returns(authResponse)
  }

  val agentFullInformationResponse =
    new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(new ~(
      Some(AffinityGroup.Agent),
      IdentityDataTestData.correctModel.internalId),
      IdentityDataTestData.correctModel.externalId),
      IdentityDataTestData.correctModel.agentCode),
      IdentityDataTestData.correctModel.credentials),
      IdentityDataTestData.correctModel.confidenceLevel),
      IdentityDataTestData.correctModel.nino),
      IdentityDataTestData.correctModel.saUtr),
      IdentityDataTestData.correctModel.name),
      IdentityDataTestData.correctModel.dateOfBirth),
      IdentityDataTestData.correctModel.email),
      IdentityDataTestData.correctModel.agentInformation),
      IdentityDataTestData.correctModel.groupIdentifier),
      IdentityDataTestData.correctModel.credentialRole),
      IdentityDataTestData.correctModel.mdtpInformation),
      Some(IdentityDataTestData.correctModel.itmpName)),
      IdentityDataTestData.correctModel.itmpDateOfBirth),
      Some(IdentityDataTestData.correctModel.itmpAddress)),
      IdentityDataTestData.correctModel.credentialStrength),
      LoginTimes(new DateTime("2016-11-27T09:00:00.000Z"), Some(new DateTime("2016-11-01T12:00:00.000Z")))
    )
}
