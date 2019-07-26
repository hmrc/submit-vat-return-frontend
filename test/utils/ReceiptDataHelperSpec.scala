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

import java.time.LocalDate

import base.BaseSpec
import connectors.VatSubscriptionConnector
import models.auth.User
import models.errors.BadRequestError
import models.nrs._
import models.{CustomerDetails, SubmitVatReturnModel}
import play.api.mvc.{AnyContentAsEmpty, Cookie}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class ReceiptDataHelperSpec extends BaseSpec {

  val mockConnector: VatSubscriptionConnector = mock[VatSubscriptionConnector]

  val service = new ReceiptDataHelper(mockConnector, messagesApi)

  val dateToUse: LocalDate = LocalDate.now()

  def createReturnModel(flatRateScheme: Boolean): SubmitVatReturnModel = SubmitVatReturnModel(
    1, 2, 3, 4, 5, 6, 7, 8, 9, flatRateScheme, dateToUse, dateToUse, dateToUse
  )

  def expectedAnswers(frs: Boolean): Seq[Answers] = {
    val boxSixExpected: String = if (frs) "Total value of sales and other supplies, including VAT" else
      "Total value of sales and other supplies, excluding VAT"
    Seq(Answers(
      "Nine box submission",
      Seq(
        ("box1", "VAT you charged on sales and other supplies", 1),
        ("box2", "VAT you owe on goods purchased from EC countries and brought into the UK", 2),
        ("box3", "VAT you owe before deductions (this is the total of box 1 and 2)", 3),
        ("box4", "VAT you have claimed back", 4),
        ("box5", "Return total", 5),
        ("box6", boxSixExpected, 6),
        ("box7", "Total value of purchases and other expenses, excluding VAT", 7),
        ("box8", "Total value of supplied goods to EC countries and related costs (excluding VAT)", 8),
        ("box9", "Total value of goods purchased from EC countries and brought into the UK, as well as any related costs (excluding VAT)", 9)
      ).map { case (questionId, question, answer) =>
        Answer(questionId, question, Some(answer.toString))
      }))
  }

  def expectedDeclaration(isAgent: Boolean): Declaration = {
    Declaration(
      if (isAgent) "I confirm that my client has received a copy of the information contained in this return and approved the information as being correct and complete to the best of their knowledge and belief."
      else "By submitting this return, you are making a legal declaration that the information is correct and complete to the best of your knowledge and belief. A false declaration can result in prosecution.",
      "Scarlett Flamberg", None, declarationConsent = true
    )
  }

  private def mockOutboundCall(frs: Boolean, noName: Boolean = false) = {
    (mockConnector.getCustomerDetails(_: String)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *)
      .returning(Future.successful(Right(CustomerDetails(
        if(noName) None else Some("Scarlett"), if(noName) None else Some("Flamberg"), None, None, frs
      ))))
  }

  private def mockFailedOutboundCall() = {
    (mockConnector.getCustomerDetails(_: String)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *)
      .returning(Future.successful(Left(BadRequestError("UH_OH", "WE'RE IN TROUBLE"))))
  }

  def userWithCookie(cookieString: String): User[AnyContentAsEmpty.type] = User[AnyContentAsEmpty.type](vrn)(fakeRequest
    .withCookies(Cookie(
      "PLAY_LANG", cookieString, None, "/", None, secure = false, httpOnly = true
    )))

  def agentUserWithCookie(cookieString: String): User[AnyContentAsEmpty.type] = User[AnyContentAsEmpty.type](vrn, Some(arn))(fakeRequestWithClientsVRN
    .withCookies(Cookie(
      "PLAY_LANG", cookieString, None, "/", None, secure = false, httpOnly = true
    )))

  "extractReceiptData" should {
    "return a receipt" when {
      (for {
        language <- Seq(EN, CY)
        flatRateScheme <- Seq(true, false)
        isAgent <- Seq(true, false)
      } yield {
        (language, flatRateScheme, isAgent)
      }).foreach { case (language, frs, isAgent) =>

        s"language is ${language.languageCode}, flatRateScheme is $frs and isAgent is $isAgent" in {
          val implicitUser: User[AnyContentAsEmpty.type] = if (isAgent) agentUserWithCookie(language.languageCode) else
            userWithCookie(language.languageCode)

          val expectedResult = ReceiptData(language, expectedAnswers(frs), expectedDeclaration(isAgent))
          mockOutboundCall(frs)

          val result = await(service.extractReceiptData(createReturnModel(frs))(user = implicitUser, hc, ec))

          result shouldBe Right(expectedResult)
        }

      }
    }
    "return an error" when {
      "there is an error from vat subscription" in {
        val implicitUser: User[AnyContentAsEmpty.type] = userWithCookie(EN.languageCode)

        val expectedResult = BadRequestError("UH_OH", "WE'RE IN TROUBLE")
        mockFailedOutboundCall()

        val result = await(service.extractReceiptData(createReturnModel(true))(user = implicitUser, hc, ec))

        result shouldBe Left(expectedResult)
      }
      "the user has no name" in {
        val implicitUser: User[AnyContentAsEmpty.type] = userWithCookie(EN.languageCode)

        val expectedResult = BadRequestError("UNEXPECTED_ERROR", "None.get")
        mockOutboundCall(frs = true, noName = true)

        val result = await(service.extractReceiptData(createReturnModel(true))(user = implicitUser, hc, ec))

        result shouldBe Left(expectedResult)
      }
    }
  }
}
