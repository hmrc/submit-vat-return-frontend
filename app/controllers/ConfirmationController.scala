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

package controllers

import config.AppConfig
import controllers.predicates.{AuthPredicate, MandationStatusPredicate}
import javax.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.Future

@Singleton
class ConfirmationController @Inject()(val messagesApi: MessagesApi,
                                       val mandationStatusCheck: MandationStatusPredicate,
                                       authPredicate: AuthPredicate,
                                       implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  def show(): Action[AnyContent] = (authPredicate andThen mandationStatusCheck).async { implicit user =>
    Future.successful(Ok(views.html.confirmation_view()))
  }
}
