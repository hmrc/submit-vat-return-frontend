/*
 * Copyright 2020 HM Revenue & Customs
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

import common.SessionKeys
import config.AppConfig
import javax.inject.Inject
import models.auth.User
import play.api.Logger
import play.api.mvc.Results._
import play.api.mvc.{ActionRefiner, Result}

import scala.concurrent.{ExecutionContext, Future}

class HonestyDeclarationAction @Inject()(implicit ec: ExecutionContext, appConfig: AppConfig) {

  def authoriseForPeriodKey(periodKey: String): ActionRefiner[User, User] = new ActionRefiner[User, User] {

    override protected def refine[A](request: User[A]): Future[Either[Result, User[A]]] = {

      request.session.get(SessionKeys.honestyDeclarationPeriodKey) match {
        case Some(sessionValue) if sessionValue.equals(periodKey) => Future(Right(request))
        case _ =>
          Logger.debug("[HonestyDeclarationAction][authoriseForPeriodKey] Declaration period key in session is missing or invalid for request")
          Future(Left(Redirect(appConfig.returnDeadlinesUrl)))
      }
    }
  }
}