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

import assets.messages.SubmitFormPageMessages._

import java.time.LocalDate
import base.BaseSpec
import models.auth.User
import models.errors.{BadRequestError, UnknownError}
import models.nrs._
import models.{CustomerDetails, SubmitVatReturnModel}
import play.api.mvc.{AnyContentAsEmpty, Cookie}


class ReceiptDataHelperSpec extends BaseSpec {

  def service(niProtocolEnabled: Boolean = false): ReceiptDataHelper = {
    mockAppConfig.features.nineBoxNIProtocolContentEnabled(niProtocolEnabled)
    new ReceiptDataHelper()(messagesApi, mockAppConfig)
  }

  val dateToUse: LocalDate = LocalDate.now()

  def createReturnModel(flatRateScheme: Boolean): SubmitVatReturnModel =
    SubmitVatReturnModel(
      10.00,
      25.55,
      33.00,
      74.00,
      95.06,
      1006,
      1006.66,
      889.9,
      900.0,
      flatRateScheme,
      dateToUse,
      dateToUse,
      dateToUse
    )

  //scalastyle:off
  def expectedAnswers(frs: Boolean, language: Language): Seq[Answers] = {
    def ifEnglishElse(ifEnglish: String, ifWelsh: String): String = if (language == EN) ifEnglish else ifWelsh

    val box1Expected = ifEnglishElse(box1Text, box1TextWelsh)
    val box1ExpectedNIProtocol = ifEnglishElse(box1TextNIProtocol, box1TextNIProtocolWelsh)

    val box2Expected = ifEnglishElse(box2Text, box2TextWelsh)
    val box2ExpectedNIProtocol = ifEnglishElse(box2TextNIProtocol, box2TextNIProtocolWelsh)

    val box3Expected = ifEnglishElse(box3Text, box3TextWelsh)
    val box3ExpectedNIProtocol = ifEnglishElse(box3TextNIProtocol, box3TextNIProtocolWelsh)

    val box4Expected = ifEnglishElse(box4Text, box4TextWelsh)
    val box4ExpectedNIProtocol = ifEnglishElse(box4TextNIProtocol, box4TextNIProtocolWelsh)

    val box5Expected = ifEnglishElse(box5Text, box5TextWelsh)
    val box5ExpectedNIProtocol = ifEnglishElse(box5TextNIProtocol, box5TextNIProtocolWelsh)


    val box6Expected = (frs, language) match {
      case (true, EN) => "Total value of sales and other supplies, including VAT"
      case (false, EN) => "Total value of sales and other supplies, excluding VAT"
      case (true, CY) => "Cyfanswm gwerth gwerthiannau a chyflenwadau eraill, gan gynnwys TAW"
      case (_, _) => "Cyfanswm gwerth y gwerthiannau a chyflenwadau eraill, ac eithrio TAW"
    }

    val box7Expected = ifEnglishElse(box7Text, box7TextWelsh)
    val box7ExpectedNIProtocol = ifEnglishElse(box7TextNIProtocol, box7TextNIProtocolWelsh)

    val box8Expected = ifEnglishElse(box8Text, box8TextWelsh)
    val box8ExpectedNIProtocol = ifEnglishElse(box8TextNIProtocol, box8TextNIProtocolWelsh)

    val box9Expected = ifEnglishElse(box9Text, box9TextWelsh)
    val box9ExpectedNIProtocol = ifEnglishElse(box9TextNIProtocol, box9TextNIProtocolWelsh)

    val pageTitle = ifEnglishElse(
      "You have submitted a VAT Return",
      "Rydych wedi cyflwyno Ffurflen TAW"
    )

    Seq(Answers(
      pageTitle,
      if(mockAppConfig.features.nineBoxNIProtocolContentEnabled()) {
        Seq(
          ("box1", box1ExpectedNIProtocol, Some("£10.00"), None),
          ("box2", box2ExpectedNIProtocol, Some("£25.55"), None),
          ("box3", box3ExpectedNIProtocol, Some("£33.00"), None),
          ("box4", box4ExpectedNIProtocol, Some("£74.00"), None),
          ("box5", box5ExpectedNIProtocol, Some("£95.06"), None),
          ("box6", box6Expected, Some("£1,006.00"), None),
          ("box7", box7ExpectedNIProtocol, Some("£1,006.66"), None),
          ("box8", box8ExpectedNIProtocol, Some("£889.90"), None),
          ("box9", box9ExpectedNIProtocol, Some("£900.00"), None)
        ).map((Answer.apply _).tupled(_))
      } else {
        Seq(
          ("box1", box1Expected, Some("£10.00"), None),
          ("box2", box2Expected, Some("£25.55"), None),
          ("box3", box3Expected, Some("£33.00"), None),
          ("box4", box4Expected, Some("£74.00"), None),
          ("box5", box5Expected, Some("£95.06"), None),
          ("box6", box6Expected, Some("£1,006.00"), None),
          ("box7", box7Expected, Some("£1,006.66"), None),
          ("box8", box8Expected, Some("£889.90"), None),
          ("box9", box9Expected, Some("£900.00"), None)
        ).map((Answer.apply _).tupled(_))
      }))
  }

  def expectedDeclaration(isAgent: Boolean, language: Language): Declaration = {
    val declarationText: String = (isAgent, language) match {
      case (true, EN) => "I confirm that my client has received a copy of the information contained in this return and approved " +
        "the information as being correct and complete to the best of their knowledge and belief."
      case (false, EN) => "By submitting this return, you are making a legal declaration that the information is " +
        "correct and complete to the best of your knowledge and belief. A false declaration can result in prosecution."
      case (true, CY) => "Rwy’n cadarnhau bod fy nghleient wedi cael copi o’r wybodaeth sydd ar y Ffurflen TAW hon, ac " +
        "wedi cytuno bod yr wybodaeth yn gywir ac yn gyflawn hyd eithaf ei wybodaeth a’i gred."
      case _ => "Drwy gyflwyno’r Ffurflen TAW hon, rydych yn gwneud datganiad cyfreithiol bod yr wybodaeth yn gywir " +
        "ac yn gyflawn hyd eithaf eich gwybodaeth a’ch cred. Gall datganiad ffug arwain at erlyniad."
    }

    Declaration(declarationText, "Test Name", None, declarationConsent = true)
  }

  val errorResponse = Left(BadRequestError("Bad Request", "There has been a bad request"))

  private def successResponse(frs: Boolean, noName: Boolean = false) = {
    Right(CustomerDetails(
      if(noName) None else Some("Test"), if(noName) None else Some("Name"), None, None, frs, isInsolvent = false, None, None, None
    ))
  }

  def userWithCookie(cookieString: String): User[AnyContentAsEmpty.type] = User[AnyContentAsEmpty.type](vrn)(fakeRequest
    .withCookies(Cookie(
      "PLAY_LANG", cookieString, None, "/", None, secure = false, httpOnly = true
    )))

  def agentUserWithCookie(cookieString: String): User[AnyContentAsEmpty.type] = User[AnyContentAsEmpty.type](vrn, Some(arn))(fakeRequestWithClientsVRN
    .withCookies(Cookie(
      "PLAY_LANG", cookieString, None, "/", None, secure = false, httpOnly = true
    )))

  val welshUser: User[AnyContentAsEmpty.type] = userWithCookie(CY.languageCode)

  "extractReceiptData" should {

    "return a receipt" when {

      "the user is an agent, with frs and english selected" in {
        val agentUser = agentUserWithCookie(EN.languageCode)

        val result = await(service().extractReceiptData(createReturnModel(true), successResponse(frs = true))(agentUser))

        result shouldBe Right(
          ReceiptData(EN, expectedAnswers(frs = true, EN), expectedDeclaration(isAgent = true, EN))
        )
      }

      "the user is an individual, without frs and welsh selected" in {
        val result = await(service().extractReceiptData(createReturnModel(false), successResponse(frs = false))(welshUser))

        result shouldBe Right(
          ReceiptData(CY, expectedAnswers(frs = false, CY), expectedDeclaration(isAgent = false, CY))
        )
      }

      "the user has no language cookie" in {
        val result = await(service().extractReceiptData(createReturnModel(false), successResponse(frs = false))(user))

        result shouldBe Right(
          ReceiptData(EN, expectedAnswers(frs = false, EN), expectedDeclaration(isAgent = false, EN))
        )
      }

      "the NI protocol feature switch is on, with english selected" in {
        val result = await(service(niProtocolEnabled = true).extractReceiptData(
          createReturnModel(false), successResponse(frs = false)
        )(user))

        result shouldBe Right(
          ReceiptData(EN, expectedAnswers(frs = false, EN), expectedDeclaration(isAgent = false, EN))
        )
      }

      "the NI protocol feature switch is on, with welsh selected" in {
        val result = await(service(niProtocolEnabled = true).extractReceiptData(
          createReturnModel(false), successResponse(frs = false)
        )(welshUser))

        result shouldBe Right(
          ReceiptData(CY, expectedAnswers(frs = false, CY), expectedDeclaration(isAgent = false, CY))
        )
      }
    }

    "return an error" when {

      val implicitUser: User[AnyContentAsEmpty.type] = userWithCookie(EN.languageCode)

      "there is an error from vat subscription" in {
        val expectedResult = BadRequestError("Bad Request", "There has been a bad request")

        val result = await(service().extractReceiptData(createReturnModel(true), errorResponse)(user = implicitUser))

        result shouldBe Left(expectedResult)
      }

      "the user has no name" in {
        val expectedResult = UnknownError

        val result = await(service().extractReceiptData(
          createReturnModel(true), successResponse(frs = true, noName = true)
        )(user = implicitUser))

        result shouldBe Left(expectedResult)
      }
    }
  }
}
