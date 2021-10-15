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

import common.SessionKeys.HonestyDeclaration

import javax.inject.Inject
import models.auth.User
import play.api.mvc.Results._
import play.api.mvc.{ActionRefiner, Result}
import utils.LoggerUtil

import scala.concurrent.{ExecutionContext, Future}

class HonestyDeclarationAction @Inject()(implicit val ec: ExecutionContext) extends LoggerUtil {

  def authoriseForPeriodKey(periodKey: String): ActionRefiner[User, User] = new ActionRefiner[User, User] {

    override protected def executionContext: ExecutionContext = ec

    override protected def refine[A](request: User[A]): Future[Either[Result, User[A]]] = {

      request.session.get(HonestyDeclaration.key) match {
        case Some(sessionValue) if sessionValue.equals(HonestyDeclaration.format(request.vrn, periodKey)) => Future(Right(request))
        case Some(_) =>
          logger.debug("[HonestyDeclarationAction][authoriseForPeriodKey] Honesty declaration invalid for request")
          Future(Left(Redirect(controllers.routes.HonestyDeclarationController.show(periodKey))
            .removingFromSession(HonestyDeclaration.key)(request)))
        case _ =>
          logger.debug("[HonestyDeclarationAction][authoriseForPeriodKey] Honesty declaration is missing from session")
          Future(Left(Redirect(controllers.routes.HonestyDeclarationController.show(periodKey))))
      }
    }
  }
}