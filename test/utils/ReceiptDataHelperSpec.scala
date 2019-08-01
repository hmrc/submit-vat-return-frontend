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
    10, 25.55, 33, 74, 95.06, 1006, 1006.66, 889.9, 900.0, flatRateScheme, dateToUse, dateToUse, dateToUse
  )

  def expectedAnswers(frs: Boolean, language: Language): Seq[Answers] = {

    val box1Expected = if(language == EN) {
      "VAT you charged on sales and other supplies"
    } else {
      "I`M WELSH INIT BRUH"
    }

    val boxSixExpected = (frs, language) match {
      case (true, EN) => "Total value of sales and other supplies, including VAT"
      case (false, EN) => "Total value of sales and other supplies, excluding VAT"
      case (true, CY) => "Total value of sales and other supplies, including VAT"
      case (false, CY) => "Total value of sales and other supplies, excluding VAT"
    }

    Seq(Answers(
      "Your VAT Return",
      Seq(
        ("box1", box1Expected, Some("£10.00"), None),
        ("box2", "VAT you owe on goods purchased from EC countries and brought into the UK", Some("£25.55"), None),
        ("box3", "VAT you owe before deductions (this is the total of box 1 and 2)", Some("£33.00"), None),
        ("box4", "VAT you have claimed back", Some("£74.00"), None),
        ("box5", "Return total", Some("£95.06"), None),
        ("box6", boxSixExpected, Some("£1,006.00"), None),
        ("box7", "Total value of purchases and other expenses, excluding VAT", Some("£1,006.66"), None),
        ("box8", "Total value of supplied goods to EC countries and related costs (excluding VAT)", Some("£889.90"), None),
        ("box9", "Total value of goods purchased from EC countries and brought into the UK, as well as any related costs (excluding VAT)", Some("£900.00"), None)
      ).map((Answer.apply _).tupled(_))))
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
      "the user is an agent, with frs" in {
        val userToUse = agentUserWithCookie(EN.languageCode)
        mockOutboundCall(frs = true)

        val result = await(service.extractReceiptData(createReturnModel(true))(userToUse, hc, ec))

        result shouldBe Right(
          ReceiptData(EN, expectedAnswers(frs = true, EN), expectedDeclaration(true))
        )
      }
      "the user is an individual, without frs" in {
        val userToUse = userWithCookie(CY.languageCode)
        mockOutboundCall(frs = false)

        val result = await(service.extractReceiptData(createReturnModel(false))(userToUse, hc, ec))

        result shouldBe Right(
          ReceiptData(CY, expectedAnswers(frs = false, CY), expectedDeclaration(false))
        )
      }
      "the user has no language cookie" in {
        mockOutboundCall(frs = false)

        val result = await(service.extractReceiptData(createReturnModel(false))(user, hc, ec))

        result shouldBe Right(
          ReceiptData(EN, expectedAnswers(frs = false, EN), expectedDeclaration(false))
        )
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
