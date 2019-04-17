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

import auth.AuthKeys
import auth.AuthKeys.{delegatedAuthRule, vatEnrolmentId, vatIdentifierId}
import config.{AppConfig, ErrorHandler}
import javax.inject.Inject
import models.auth.User
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import services.EnrolmentsAuthService
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.Retrievals._
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

class AuthPredicate @Inject()(authService: EnrolmentsAuthService,
                              errorHandler: ErrorHandler,
                              val messagesApi: MessagesApi,
                              implicit val ec: ExecutionContext,
                              implicit val appConfig: AppConfig) extends FrontendController
                                                                 with I18nSupport
                                                                 with ActionBuilder[User]
                                                                 with ActionFunction[Request, User] {

  override def invokeBlock[A](request: Request[A], block: User[A] => Future[Result]): Future[Result] = {

    implicit val req: Request[A] = request

    authService
      .authorised()
      .retrieve(affinityGroup and allEnrolments) {
        case Some(Agent) ~ _ => authoriseAsAgent(block)
        case Some(_) ~ enrolments => authoriseAsNonAgent(enrolments, block)
        case None ~ _ =>
          Logger.warn("[AuthPredicate][invokeBlock] - Missing affinity group")
          Future.successful(errorHandler.showInternalServerError)
      } recover {
        case _: NoActiveSession =>
          Logger.debug(s"[AuthPredicate][invokeBlock] - No active session. Redirecting to ${appConfig.signInUrl}")
          Redirect(appConfig.signInUrl)
        case _: AuthorisationException =>
          Logger.warn("[AuthPredicate][invokeBlock] - Unauthorised exception when retrieving affinity and all enrolments")
          errorHandler.showInternalServerError
      }
  }

  private def authoriseAsNonAgent[A](enrolments: Enrolments, block: User[A] => Future[Result])
                                    (implicit request: Request[A]): Future[Result] = {
    enrolments.enrolments.collectFirst {
      case Enrolment(AuthKeys.vatEnrolmentId, EnrolmentIdentifier(_, vrn) :: _, _, _) => vrn
    } match {
      case Some(vrn) => block(User(vrn))
      case None =>
        Logger.debug("[AuthPredicate][authoriseAsNonAgent] - Non-agent with no HMRC-MTD-VAT enrolment. Rendering unauthorised view.")
        Future.successful(Forbidden(views.html.errors.unauthorised_non_agent()))
    }
  }

  private def authoriseAsAgent[A](block: User[A] => Future[Result])
                                 (implicit request: Request[A]): Future[Result] = {

    val agentDelegatedAuthorityRule: String => Enrolment = vrn =>
      Enrolment(vatEnrolmentId)
        .withIdentifier(vatIdentifierId, vrn)
        .withDelegatedAuthRule(delegatedAuthRule)

    request.session.get(AuthKeys.agentSessionVrn) match {
      case Some(vrn) =>
        authService
          .authorised(agentDelegatedAuthorityRule(vrn))
          .retrieve(allEnrolments) {
            enrolments =>
              enrolments.enrolments.collectFirst {
                case Enrolment(AuthKeys.agentEnrolmentId, EnrolmentIdentifier(_, arn) :: _, _, _) => arn
              } match {
                case Some(arn) => block(User(vrn, Some(arn)))
                case None =>
                  Logger.debug("[AuthPredicate][authoriseAsAgent] - Agent with no HMRC-AS-AGENT enrolment. Rendering unauthorised view.")
                  Future.successful(Forbidden(views.html.errors.unauthorised_agent()))
              }
          } recover {
            case _: NoActiveSession =>
              Logger.debug(s"AuthoriseAsAgentWithClient][authoriseAsAgent] - No active session. Redirecting to ${appConfig.signInUrl}")
              Redirect(appConfig.signInUrl)
            case _: AuthorisationException =>
              Logger.debug(s"[AuthoriseAsAgentWithClient][authoriseAsAgent] - Agent does not have delegated authority for Client. " +
                s"Redirecting to ${appConfig.agentClientUnauthorisedUrl}")
              Redirect(appConfig.agentClientUnauthorisedUrl)
          }

      case None =>
        Logger.debug(s"[AuthPredicate][authoriseAsAgent] - No Client VRN in session. Redirecting to ${appConfig.agentClientLookupStartUrl}")
        Future.successful(Redirect(appConfig.agentClientLookupStartUrl))
    }
  }
}
