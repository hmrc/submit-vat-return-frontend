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

package controllers

import common.SessionKeys.{HonestyDeclaration => SessionKeys}
import config.{AppConfig, ErrorHandler}
import controllers.predicates.{AuthPredicate, MandationStatusPredicate}
import forms.HonestyDeclarationForm

import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.LoggingUtil
import views.html.HonestyDeclaration

import scala.concurrent.Future

@Singleton
class HonestyDeclarationController @Inject()(val mandationStatusCheck: MandationStatusPredicate,
                                             val errorHandler: ErrorHandler,
                                             authPredicate: AuthPredicate,
                                             mcc: MessagesControllerComponents,
                                             honestyDeclaration: HonestyDeclaration,
                                             implicit val appConfig: AppConfig) extends FrontendController(mcc) with I18nSupport with LoggingUtil{

  def show(periodKey: String): Action[AnyContent] = (authPredicate andThen mandationStatusCheck).async {
    implicit user =>
      Future.successful(Ok(honestyDeclaration(periodKey, HonestyDeclarationForm.honestyDeclarationForm))
        .removingFromSession(SessionKeys.key))
    }

  def submit(periodKey: String): Action[AnyContent] = (authPredicate andThen mandationStatusCheck).async { implicit user =>
    HonestyDeclarationForm.honestyDeclarationForm.bindFromRequest().fold(
      error => {
        errorLog(s"[HonestyDeclarationController][submit] - $error occured while binding honesty declaration form")
        Future.successful(BadRequest(honestyDeclaration(periodKey, error)))
      },
      _ =>
        Future.successful(Redirect(controllers.routes.SubmitFormController.show(periodKey))
          .addingToSession(SessionKeys.key -> s"${user.vrn}-$periodKey"))
    )
  }
}
