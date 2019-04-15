/*
 * Copyright 2018 HM Revenue & Customs
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
import auth.AuthKeys.{agentEnrolmentId, delegatedAuthRule, vatEnrolmentId, vatIdentifierId}
import config.AppConfig
import javax.inject.Inject
import models.auth.User
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.EnrolmentsAuthService
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core.retrieve.Retrievals._
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core.{AuthorisationException, Enrolment, NoActiveSession}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

class AuthPredicate @Inject()(authService: EnrolmentsAuthService,
                              appConfig: AppConfig) extends FrontendController
                                                    with I18nSupport
                                                    with ActionBuilder[User]
                                                    with ActionFunction[Request, User] {

  override def invokeBlock[A](request: Request[A], block: User[A] => Future[Result]): Future[Result] = {

    authService
      .authorised()
      .retrieve(affinityGroup and allEnrolments) {

        case Some(affinity) ~ enrolments =>

          if(affinity == Agent) {
            authoriseAsAgent(block)
          } else {

          }
      }
  }

  private def authoriseAsAgent(block: Request[_] => Future[Result])
                              (implicit request: Request[AnyContent], ec: ExecutionContext): Future[Result] = {

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
                case Enrolment(`agentEnrolmentId`, _ :: _, _, _) => block(request)
              } getOrElse Future.successful(Forbidden(views.html.errors.unauthorised()))
          } recover {
            case _: NoActiveSession =>
              Logger.debug("AuthoriseAsAgentWithClient][invokeBlock] - Agent does not have an active session, redirect to GG Sign In")
              Redirect(appConfig.signInUrl)
            case _: AuthorisationException =>
              Logger.warn(s"[AuthoriseAsAgentWithClient][invokeBlock] - Agent does not have delegated authority for Client")
              Redirect(appConfig.agentClientUnauthorisedUrl)
          }

      case None =>
        Logger.debug(s"[AuthPredicate][authoriseAsAgent] - No Client VRN in session, redirecting to VACLUF")
        Future.successful(Redirect(appConfig.agentClientLookupUrl))
    }
  }

  private def authExceptionAction: PartialFunction[Throwable, Result] = {
    case _: NoActiveSession =>
      Logger.debug(s"[AuthPredicate][authExceptionAction] - User has no active session")
      Redirect(appConfig.signInUrl)
    case ex: AuthorisationException => forbiddenAction(ex.reason)
  }
}
