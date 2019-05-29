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

import assets.CustomerDetailsTestAssets._
import base.BaseSpec
import common.{MandationStatuses, SessionKeys}
import connectors.httpParsers.ResponseHttpParsers.HttpGetResult
import mocks.service.{MockVatReturnsService, MockVatSubscriptionService}
import mocks.{MockAuth, MockMandationPredicate}
import models.auth.User
import models.{CustomerDetails, SubmitVatReturnModel}
import models.errors.UnexpectedJsonFormat
import models.vatReturnSubmission.SubmissionSuccessModel
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.Helpers._

import scala.concurrent.Future

class ConfirmSubmissionControllerSpec extends BaseSpec with MockAuth with MockMandationPredicate with MockVatSubscriptionService with MockVatReturnsService {

  object TestConfirmSubmissionController extends ConfirmSubmissionController(
    messagesApi,
    mockMandationStatusPredicate,
    errorHandler,
    mockVatSubscriptionService,
    mockAuthPredicate,
    mockVatReturnsService,
    ec,
    mockAppConfig
  )

  "ConfirmSubmissionController .show" when {

    "user is authorised" when {

      val nineBoxModel: String = Json.stringify(Json.toJson(
        SubmitVatReturnModel(
          1000.00,
          1000.00,
          1000.00,
          1000.00,
          1000.00,
          1000.00,
          1000.00,
          1000.00,
          1000.00,
          flatRateScheme = true,
          LocalDate.now(),
          LocalDate.now(),
          LocalDate.now()
        )
      ))

      "there is session data" when {

        "a successful response is returned from the vat subscription service" should {

          val vatSubscriptionResponse: Future[HttpGetResult[CustomerDetails]] = Future.successful(Right(customerDetailsWithFRS))

          lazy val requestWithSessionData: User[AnyContentAsEmpty.type] =
            User[AnyContentAsEmpty.type]("123456789")(fakeRequest.withSession(
              SessionKeys.returnData -> nineBoxModel,
              SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB
              )
            )

          lazy val result: Future[Result] = {
            setupVatSubscriptionService(vatSubscriptionResponse)
            TestConfirmSubmissionController.show("18AA")(requestWithSessionData)
          }

          "return 200" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            status(result) shouldBe Status.OK
          }

          "return HTML" in {
            contentType(result) shouldBe Some("text/html")
          }

          "the user name should be displayed" in {
            Jsoup.parse(bodyOf(result)).select("#content > article > section > h2").text() shouldBe "ABC Solutions"
          }
        }

        "an error response is returned from the vat subscription service" should {

          val vatSubscriptionResponse: Future[HttpGetResult[CustomerDetails]] = Future.successful(Left(UnexpectedJsonFormat))

          lazy val requestWithSessionData: User[AnyContentAsEmpty.type] =
            User[AnyContentAsEmpty.type]("123456789")(fakeRequest.withSession(
              SessionKeys.returnData -> nineBoxModel,
              SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB
            )
            )

          lazy val result: Future[Result] = {
            setupVatSubscriptionService(vatSubscriptionResponse)
            TestConfirmSubmissionController.show("18AA")(requestWithSessionData)
          }

          "return 200" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            status(result) shouldBe Status.OK
          }

          "return HTML" in {
            contentType(result) shouldBe Some("text/html")
          }

          "the user name should not be displayed" in {
            Jsoup.parse(bodyOf(result)).select("#content > article > div > h2").text() shouldBe ""
          }
        }
      }

      "there is no session data" should {

        lazy val result: Future[Result] = {
          TestConfirmSubmissionController.show("18AA")(fakeRequest.withSession(
            SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB)
          )
        }

        "return 303" in {
          mockAuthorise(mtdVatAuthorisedResponse)
          status(result) shouldBe Status.SEE_OTHER
        }

        s"redirect to ${controllers.routes.SubmitFormController.show("18AA").url}" in {
          redirectLocation(result) shouldBe Some(controllers.routes.SubmitFormController.show("18AA").url)
        }
      }
    }
    authControllerChecks(TestConfirmSubmissionController.show("18AA"), fakeRequest)
  }

  "ConfirmSubmissionController .submit" when {

    "user is authorised" when {

      "valid session data exists" when {

        val nineBoxData = Json.obj(
          "box1" -> "10.01",
          "box2" -> "10.02",
          "box3" -> "10.03",
          "box4" -> "10.04",
          "box5" -> "10.05",
          "box6" -> "10.06",
          "box7" -> "10.07",
          "box8" -> "10.08",
          "box9" -> "10.09",
          "flatRateScheme" -> false,
          "from" -> "2019-01-02",
          "to" -> "2019-01-02",
          "due" -> "2019-01-02"
        ).toString()

        "submission to backend is successful" should {

          lazy val result: Future[Result] = TestConfirmSubmissionController.submit("18AA")(fakeRequest.withSession(
            "mtdNineBoxReturnData" -> nineBoxData,
            SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB
          ))

          "return 303" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            mockVatReturnsService(Future.successful(Right(SubmissionSuccessModel("12345"))))
            status(result) shouldBe Status.SEE_OTHER
          }

          s"redirect to ${controllers.routes.ConfirmationController.show()}" in {
            redirectLocation(result) shouldBe Some(controllers.routes.ConfirmationController.show().url)
          }
        }

        "submission to backend is unsuccessful" should {

          lazy val result: Future[Result] = TestConfirmSubmissionController.submit("18AA")(fakeRequest.withSession(
            "mtdNineBoxReturnData" -> nineBoxData,
            SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB
          ))

          "return 500" in {
            mockAuthorise(mtdVatAuthorisedResponse)
            mockVatReturnsService(Future.successful(Left(UnexpectedJsonFormat)))
            status(result) shouldBe Status.INTERNAL_SERVER_ERROR
          }

          "show ISE page" in {
            Jsoup.parse(bodyOf(result)).title() shouldBe "Sorry, we are experiencing technical difficulties - 500"
          }
        }
      }

      "invalid session data exists" should {

        val nineBoxData = Json.obj("box10" -> "why").toString()
        lazy val result: Future[Result] = TestConfirmSubmissionController.submit("18AA")(fakeRequest.withSession(
          "mtdNineBoxReturnData" -> nineBoxData,
          SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB
        ))

        "return 500" in {
          mockAuthorise(mtdVatAuthorisedResponse)
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }

        "show ISE page" in {
          Jsoup.parse(bodyOf(result)).title() shouldBe "Sorry, we are experiencing technical difficulties - 500"
        }
      }

      "no session data exists for key" should {

        lazy val result: Future[Result] = TestConfirmSubmissionController.submit("18AA")(fakeRequest.withSession(
          SessionKeys.mandationStatus -> MandationStatuses.nonMTDfB
        ))

        "return 303" in {
          mockAuthorise(mtdVatAuthorisedResponse)
          status(result) shouldBe Status.SEE_OTHER
        }

        s"redirect to ${controllers.routes.SubmitFormController.show("18AA").url}" in {
          redirectLocation(result) shouldBe Some(controllers.routes.SubmitFormController.show("18AA").url)
        }
      }
    }

    authControllerChecks(TestConfirmSubmissionController.submit("18AA"), fakeRequest)
  }
}
