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

import java.time.LocalDate

import config.{AppConfig, ErrorHandler}
import controllers.predicates.{AuthPredicate, MandationStatusPredicate}
import javax.inject.Inject
import models.{ConfirmSubmissionViewModel, CustomerDetails, VatObligation, VatReturnDetails}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.Future

class ConfirmSubmissionController @Inject()(val messagesApi: MessagesApi,
                                            val mandationStatusCheck: MandationStatusPredicate,
                                            val errorHandler: ErrorHandler,
                                            authPredicate: AuthPredicate,
                                            implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  val vatObligation: VatObligation = VatObligation(
    start = LocalDate.parse("2019-01-12"),
    end = LocalDate.parse("2019-04-12"),
    due = LocalDate.parse("2019-05-12"),
    periodKey = "17AA"
  )

  val customerDetails: CustomerDetails = CustomerDetails(
    None,
    None,
    None,
    organisationName = Some("ABC Trading"),
    hasFlatRateScheme = true
  )

  val vatReturn: VatReturnDetails = VatReturnDetails(
    boxOne = 1000.00,
    boxTwo = 1000.00,
    boxThree = 1000.00,
    boxFour = 1000.00,
    boxFive = 1000.00,
    boxSix = 1000.00,
    boxSeven = 1000.00,
    boxEight = 1000.00,
    boxNine = 1000.00
  )

  val viewModel: ConfirmSubmissionViewModel = ConfirmSubmissionViewModel(
    vatObligation,
    hasFlatRateScheme = true,
    vatReturn,
    userName = customerDetails.clientName.getOrElse(""),
    vatReturnTotal = 10000.00
  )

  def show(periodKey: String): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(views.html.confirm_submission(viewModel)))
  }

  def submit(periodKey: String): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Redirect(controllers.routes.ConfirmationController.show().url))
  }
}
