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
import javax.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import config.ErrorHandler
import services.{VatObligationsService, VatSubscriptionService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

@Singleton
class SubmitFormController @Inject()(val messagesApi: MessagesApi,
                                     val vatSubscriptionService: VatSubscriptionService,
                                     val vatObligationsService: VatObligationsService,
                                     val errorHandler: ErrorHandler,
                                     implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  def show(periodKey: String): Action[AnyContent] = Action.async { implicit request =>

    val customerInformationCall = vatSubscriptionService.getCustomerDetails("123456789")
    val obligationsCall = vatObligationsService.getObligations("123456789")

    for {
      customerInformation <- customerInformationCall
      obligations <- obligationsCall
    } yield {
      (customerInformation, obligations) match {
        case (Right(customerDetails), Right(obs)) => {
          Ok(views.html.submit_form(periodKey, customerDetails.clientName, customerDetails.hasFlatRateScheme, obs))
        }
        case (_, _ ) => errorHandler.showInternalServerError
      }
    }
  }
}
