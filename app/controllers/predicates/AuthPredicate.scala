/*
 * Copyright 2023 HM Revenue & Customs
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
import common.SessionKeys
import config.{AppConfig, ErrorHandler}

import javax.inject.{Inject, Singleton}
import models.auth.User
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.{DateService, EnrolmentsAuthService, VatSubscriptionService}
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.LoggingUtil
import views.html.errors._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthPredicate @Inject()(authService: EnrolmentsAuthService,
                              vatSubscriptionService: VatSubscriptionService,
                              dateService: DateService,
                              errorHandler: ErrorHandler,
                              mcc: MessagesControllerComponents,
                              unauthorisedAgent: UnauthorisedAgent,
                              unauthorisedNonAgent: UnauthorisedNonAgent,
                              userInsolventError: UserInsolventError)
                             (implicit val executionContext: ExecutionContext,
                              implicit val appConfig: AppConfig) extends FrontendController(mcc)
                                                                 with I18nSupport
                                                                 with ActionBuilder[User, AnyContent]
                                                                 with ActionFunction[Request, User]
                                                                 with LoggingUtil {

  override val parser: BodyParser[AnyContent] = mcc.parsers.defaultBodyParser

  override def invokeBlock[A](request: Request[A], block: User[A] => Future[Result]): Future[Result] = {

    implicit val req: Request[A] = request

    val result: Future[Result] = authService
      .authorised()
      .retrieve(affinityGroup and allEnrolments) {
        case Some(Agent) ~ enrolments =>
          if (enrolments.enrolments.exists(_.key == AuthKeys.agentEnrolmentId)){
            authoriseAsAgent(block)
          } else {
            debug("[AuthPredicate][invokeBlock] - Agent does not have correct agent enrolment ID")
            Future.successful(Forbidden(unauthorisedAgent()))
          }

        case Some(_) ~ enrolments => authoriseAsNonAgent(enrolments, block)
        case None ~ _ =>
          errorLog("[AuthPredicate][invokeBlock] - Missing affinity group")
          errorHandler.showInternalServerError
      }
    result.recoverWith {
      case _: NoActiveSession =>
        warnLog(s"[AuthPredicate][invokeBlock] - No active session. Redirecting to ${appConfig.signInUrl}")
        Future.successful(Redirect(appConfig.signInUrl))

      case _: AuthorisationException =>
        errorLog("[AuthPredicate][invokeBlock] - Unauthorised exception")
        errorHandler.showInternalServerError
    }
  }

  private def authoriseAsNonAgent[A](enrolments: Enrolments, block: User[A] => Future[Result])
                                    (implicit request: Request[A]): Future[Result] = {
    enrolments.enrolments.collectFirst {
      case Enrolment(AuthKeys.vatEnrolmentId, Seq(EnrolmentIdentifier(_, vrn)), AuthKeys.activated, _) => vrn
    } match {
      case Some(vrn) =>
        val user = User(vrn)
        (request.session.get(SessionKeys.insolventWithoutAccessKey), request.session.get(SessionKeys.futureInsolvencyBlock)) match {
          case (Some("true"), _) => Future.successful(Forbidden(userInsolventError()(user,request2Messages,appConfig)))
          case (Some("false"), Some("true")) => errorHandler.showInternalServerError
          case (Some("false"), Some("false")) => block(user)
          case _ => insolvencySubscriptionCall(user, block)
        }
      case None =>
        errorLog("[AuthPredicate][authoriseAsNonAgent] - Non-agent with no HMRC-MTD-VAT enrolment. Rendering unauthorised view.")
        Future.successful(Forbidden(unauthorisedNonAgent()))
    }
  }

  private def insolvencySubscriptionCall[A](user: User[A], block: User[A] => Future[Result])(implicit request: Request[A]) = {
    vatSubscriptionService.getCustomerDetails(user.vrn).flatMap {
      case Right(details) =>
        (details.isInsolventWithoutAccess, details.insolvencyDateFutureUserBlocked(dateService.now())) match {
          case (true, futureDateBlock) =>
            debug("[AuthPredicate][insolvencySubscriptionCall] - User is insolvent and not continuing to trade")
            Future.successful(
              Forbidden(userInsolventError()(user, request2Messages, appConfig)).addingToSession(
                SessionKeys.insolventWithoutAccessKey -> "true",
                SessionKeys.futureInsolvencyBlock -> s"$futureDateBlock")
            )
          case (_, true) =>
            errorLog("[AuthPredicate][insolvencySubscriptionCall] - User has a future insolvencyDate, throwing ISE")
            errorHandler.showInternalServerError.map(_.addingToSession(
                SessionKeys.insolventWithoutAccessKey -> "false",
                SessionKeys.futureInsolvencyBlock -> "true")
            )
          case _ =>
            infoLog("[AuthPredicate][insolvencySubscriptionCall] - Authenticated as principal")
            block(user).map(_.addingToSession(
                SessionKeys.insolventWithoutAccessKey -> "false",
                SessionKeys.futureInsolvencyBlock -> "false")
            )
        }
      case _ =>
        errorLog("[AuthPredicate][insolvencySubscriptionCall] - Failure obtaining insolvency status from Customer Info API")
        errorHandler.showInternalServerError
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
                case Enrolment(AuthKeys.agentEnrolmentId, Seq(EnrolmentIdentifier(_, arn)), AuthKeys.activated, _) => arn
              } match {
                case Some(arn) => block(User(vrn, Some(arn)))
                case None =>
                  warnLog("[AuthPredicate][authoriseAsAgent] - Agent with no valid arn. Rendering unauthorised view.")
                  Future.successful(Forbidden(unauthorisedAgent()))
              }
          } recover {
            case _: NoActiveSession =>
              warnLog(s"AuthoriseAsAgentWithClient][authoriseAsAgent] - No active session. Redirecting to ${appConfig.signInUrl}")
              Redirect(appConfig.signInUrl)
            case _: AuthorisationException =>
              warnLog(s"[AuthoriseAsAgentWithClient][authoriseAsAgent] - Agent does not have delegated authority for Client. " +
                s"Redirecting to ${appConfig.agentClientUnauthorisedUrl(request.uri)}")
              Redirect(appConfig.agentClientUnauthorisedUrl(request.uri))
          }

      case None =>
        warnLog(s"[AuthPredicate][authoriseAsAgent] - No Client VRN in session. Redirecting to ${appConfig.agentClientLookupStartUrl}")
        Future.successful(Redirect(appConfig.agentClientLookupStartUrl(request.uri)))
    }
  }
}
