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

package utils

import config.AppConfig
import javax.inject.{Inject, Singleton}
import models.auth.User
import models.errors.{HttpError, UnknownError}
import models.nrs.{Declaration, _}
import models.{CustomerDetails, SubmitVatReturnModel}
import play.api.i18n.{Messages, MessagesApi}
import play.api.{Logger, Play}
import uk.gov.hmrc.play.views.helpers.MoneyPounds

@Singleton
class ReceiptDataHelper @Inject()(implicit val messages: MessagesApi,
                                               appConfig: AppConfig) {

  def extractReceiptData(submitModel: SubmitVatReturnModel, customerDetails: Either[HttpError, CustomerDetails])
                        (implicit user: User[_]): Either[HttpError, ReceiptData] = {

    val language: Language = user.cookies.get(Play.langCookieName) match {
      case Some(cookieValue) => Language.fromString(cookieValue.value)
      case None => EN
    }
    
    extractDeclaration(submitModel, customerDetails, messages.preferred(user)) match {
      case Right(declaration) =>
        Right(ReceiptData(
          language,
          extractAnswers(submitModel)(messages.preferred(user)),
          declaration
        ))
      case Left(error) => Left(error)
    }
  }

  private def extractAnswers(submitModel: SubmitVatReturnModel)(implicit messages: Messages): Seq[Answers] = {
    val boxSixSearchKey = if (submitModel.flatRateScheme) "boxSixFlatRate" else "boxSixNoFlatRate"
    val niProtocolSuffix = if(appConfig.features.nineBoxNIProtocolContentEnabled()) ".NIProtocol" else ""
    val answerSeq = Seq(
      ("box1", messages(s"confirm_submission.boxOneDescription$niProtocolSuffix"), submitModel.box1),
      ("box2", messages(s"confirm_submission.boxTwoDescription$niProtocolSuffix"), submitModel.box2),
      ("box3", messages(s"confirm_submission.boxThreeDescription$niProtocolSuffix"), submitModel.box3),
      ("box4", messages(s"confirm_submission.boxFourDescription$niProtocolSuffix"), submitModel.box4),
      ("box5", messages(s"confirm_submission.boxFiveDescription$niProtocolSuffix"), submitModel.box5),
      ("box6", messages(s"confirm_submission.$boxSixSearchKey"), submitModel.box6),
      ("box7", messages(s"confirm_submission.boxSevenDescription$niProtocolSuffix"), submitModel.box7),
      ("box8", messages(s"confirm_submission.boxEightDescription$niProtocolSuffix"), submitModel.box8),
      ("box9", messages(s"confirm_submission.boxNineDescription$niProtocolSuffix"), submitModel.box9)
    ).map { case (questionId, question, answer) =>
      Answer(questionId, question, Some("£" + MoneyPounds(answer, 2).quantity))
    }

    Seq(Answers(
      messages("confirmation_view.title"),
      answerSeq
    ))
  }

  private def extractDeclaration(submitModel: SubmitVatReturnModel, customerDetails: Either[HttpError, CustomerDetails], messages: Messages)
                                (implicit user: User[_]): Either[HttpError, Declaration] = {

    val declarationAgentOrNonAgent = if (user.isAgent) "agentDeclaration" else "nonAgentDeclaration"

    customerDetails match {
      case Right(model) => model.clientName match {
        case Some(name) => Right(
          Declaration(
            messages(s"confirm_submission.$declarationAgentOrNonAgent"),
            name,
            None,
            declarationConsent = true
          )
        )
        case None =>
          Logger.warn("[ReceiptDataHelper][extractDeclaration] Client name missing")
          Left(UnknownError)
      }
      case Left(error) =>
        Logger.debug("[ReceiptDataHelper][extractDeclaration] Failed to retrieve customer details from vat-subscription\n" + error.message)
        Logger.warn("[ReceiptDataHelper][extractDeclaration] Failed to retrieve customer details from vat-subscription")
        Left(error)
    }
  }
}
