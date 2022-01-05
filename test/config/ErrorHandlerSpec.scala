/*
 * Copyright 2022 HM Revenue & Customs
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

package config

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.http.Status._
import play.api.mvc.Cookie
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, status}
import views.ViewBaseSpec
import views.html.templates.ErrorTemplate

import scala.concurrent.Future

class ErrorHandlerSpec extends ViewBaseSpec {

  val errorTemplate: ErrorTemplate = inject[ErrorTemplate]

  "ErrorHandler" when {

    lazy val service: ErrorHandler = new ErrorHandler(messagesApi, errorTemplate, mockAppConfig)

    "calling showInternalServerError" when {

      "language is set to English" should {

        lazy val fakeRequest = FakeRequest().withCookies(Cookie("PLAY_LANG", "en"))
        lazy val result = service.showInternalServerError(fakeRequest)
        implicit lazy val document: Document = Jsoup.parse(contentAsString(Future.successful(result)))

        "return 500" in {
          status(Future.successful(result)) shouldBe INTERNAL_SERVER_ERROR
        }

        "render page in English" in {
          document.title shouldBe "There is a problem with the service - VAT - GOV.UK"
        }
      }

      "language is set to Welsh" should {

        lazy val fakeRequest = FakeRequest().withCookies(Cookie("PLAY_LANG", "cy"))
        lazy val result = service.showInternalServerError(fakeRequest)
        implicit lazy val document: Document = Jsoup.parse(contentAsString(Future.successful(result)))

        "return 500" in {
          status(Future.successful(result)) shouldBe INTERNAL_SERVER_ERROR
        }

        "render page in Welsh" in {
          document.title shouldBe "Mae problem gyda’r gwasanaeth - TAW - GOV.UK"
        }
      }
    }

    "calling showBadRequestError" when {

      "language is set to English" should {

        lazy val fakeRequest = FakeRequest().withCookies(Cookie("PLAY_LANG", "en"))
        lazy val result = service.showBadRequestError(fakeRequest)
        implicit lazy val document: Document = Jsoup.parse(contentAsString(Future.successful(result)))

        "return 400" in {
          status(Future.successful(result)) shouldBe BAD_REQUEST
        }

        "render page in English" in {
          document.title shouldBe "Bad request - VAT - GOV.UK"
        }
      }

      "language is set to Welsh" should {

        lazy val fakeRequest = FakeRequest().withCookies(Cookie("PLAY_LANG", "cy"))
        lazy val result = service.showBadRequestError(fakeRequest)
        implicit lazy val document: Document = Jsoup.parse(contentAsString(Future.successful(result)))

        "return 400" in {
          status(Future.successful(result)) shouldBe BAD_REQUEST
        }

        "render page in Welsh" in {
          document.title shouldBe "Cais drwg - TAW - GOV.UK"
        }
      }
    }

    "calling onClientError for a page not found" when {

      "language is set to English" should {

        lazy val fakeRequest = FakeRequest().withCookies(Cookie("PLAY_LANG", "en"))
        lazy val result = service.onClientError(fakeRequest, NOT_FOUND)
        implicit lazy val document: Document = Jsoup.parse(contentAsString(result))

        "return 400" in {
          status(result) shouldBe NOT_FOUND
        }

        "render page in English" in {
          document.title shouldBe "Page not found - VAT - GOV.UK"
        }
      }

      "language is set to Welsh" should {

        lazy val fakeRequest = FakeRequest().withCookies(Cookie("PLAY_LANG", "cy"))
        lazy val result = service.onClientError(fakeRequest, NOT_FOUND)
        implicit lazy val document: Document = Jsoup.parse(contentAsString(result))

        "return 400" in {
          status(result) shouldBe NOT_FOUND
        }

        "render page in Welsh" in {
          document.title shouldBe "Heb ddod o hyd i’r dudalen - TAW - GOV.UK"
        }
      }
    }
  }
}
