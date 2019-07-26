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

package utils

import connectors.VatSubscriptionConnector
import javax.inject.{Inject, Singleton}
import models.SubmitVatReturnModel
import models.auth.User
import models.errors.{BadRequestError, HttpError}
import models.nrs.{Declaration, _}
import play.api.{Logger, Play}
import play.api.i18n.MessagesApi
import play.api.mvc.AnyContent
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class ReceiptDataHelper @Inject()(
                                   vatSubscriptionConnector: VatSubscriptionConnector,
                                   implicit val messages: MessagesApi
                                 ) {

  def extractReceiptData(submitModel: SubmitVatReturnModel)
                        (implicit user: User[_], hc: HeaderCarrier, ec: ExecutionContext): Future[Either[HttpError, ReceiptData]] = {

    val language: Language = user.cookies.get(Play.langCookieName) match {
      case Some(cookieValue) => Language.fromString(cookieValue.value)
      case None => EN
    }

    extractDeclaration(submitModel).map {
      case Right(declaration) =>
        Right(ReceiptData(
          language,
          extractAnswers(submitModel),
          declaration
        ))
      case Left(error) => Left(error)
    }
  }

  private def extractAnswers(submitModel: SubmitVatReturnModel)(implicit user: User[_]): Seq[Answers] = {
    val boxSixSearchKey = if (submitModel.flatRateScheme) "boxSixFlatRate" else "boxSixNoFlatRate"

    val answerSeq = Seq(
      ("box1", messages("confirm_submission.boxOneDescription"), submitModel.box1),
      ("box2", messages("confirm_submission.boxTwoDescription"), submitModel.box2),
      ("box3", messages("confirm_submission.boxThreeDescription"), submitModel.box3),
      ("box4", messages("confirm_submission.boxFourDescription"), submitModel.box4),
      ("box5", messages("confirm_submission.boxFiveDescription"), submitModel.box5),
      ("box6", messages(s"confirm_submission.$boxSixSearchKey"), submitModel.box6),
      ("box7", messages("confirm_submission.boxSevenDescription"), submitModel.box7),
      ("box8", messages("confirm_submission.boxEightDescription"), submitModel.box8),
      ("box9", messages("confirm_submission.boxNineDescription"), submitModel.box9)
    ).map { case (questionId, question, answer) =>
      Answer(questionId, question, Some(answer.toString()))
    }

    Seq(Answers(
      "Nine box submission",
      answerSeq
    ))
  }

  private def extractDeclaration(submitModel: SubmitVatReturnModel)
                                (implicit user: User[_], hc: HeaderCarrier, ec: ExecutionContext): Future[Either[HttpError, Declaration]] = {

    val declarationAgentOrNonAgent = if (user.isAgent) "agentDeclaration" else "nonAgentDeclaration"

    vatSubscriptionConnector.getCustomerDetails(user.vrn).map {
      case Right(result) =>
        try {
          Right(Declaration(
            messages(s"confirm_submission.$declarationAgentOrNonAgent"),
            result.clientName.get,
            None,
            declarationConsent = true
          ))
        } catch {
          case t: Throwable =>
            Logger.debug(
              "[ReceiptDataHelper][extractDeclaration] An error has occurred", t
            )
            Left(BadRequestError(
              "UNEXPECTED_ERROR",
              t.getMessage
            ))
        }
      case Left(error) => Left(error)
    }
  }
}
