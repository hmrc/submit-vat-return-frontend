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

package testOnly.controllers

import auth.AuthKeys
import config.AppConfig

import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import testOnly.forms.StubAgentClientLookupForm
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import testOnly.views.html._
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl

class StubAgentClientLookupController @Inject()(mcc: MessagesControllerComponents,
                                                stubAgentClientLookup: StubAgentClientLookup,
                                                stubAgentClientLookupAgentAction: StubAgentClientLookupAgentAction,
                                                stubAgentClientUnauth: StubAgentClientUnauth,
                                                implicit val appConfig: AppConfig)
  extends FrontendController(mcc) with I18nSupport {

  def show(redirectUrl: Option[RedirectUrl]): Action[AnyContent] = Action { implicit request =>
    Ok(stubAgentClientLookup(StubAgentClientLookupForm.form, redirectUrl.map(_.unsafeValue)))
  }

  def showAgentAction: Action[AnyContent] = Action { implicit request =>
    Ok(stubAgentClientLookupAgentAction())
  }

  def unauthorised(redirectUrl: RedirectUrl): Action[AnyContent] = Action { implicit request =>
    Ok(stubAgentClientUnauth(redirectUrl.unsafeValue))
      .removingFromSession(AuthKeys.agentSessionVrn)
  }

  def post: Action[AnyContent] = Action { implicit request =>
    StubAgentClientLookupForm.form.bindFromRequest().fold(
      error => InternalServerError(s"Failed to bind model. Error: $error"),
      success => Redirect(success.redirectUrl)
        .addingToSession(
          AuthKeys.agentSessionVrn -> success.vrn)
    )
  }
}
