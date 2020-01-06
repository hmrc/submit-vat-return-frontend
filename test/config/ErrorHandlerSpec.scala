/*
 * Copyright 2020 HM Revenue & Customs
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

import base.BaseSpec
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.http.Status._
import play.api.mvc.Cookie
import play.api.test.FakeRequest
import views.ViewBaseSpec

class ErrorHandlerSpec extends ViewBaseSpec {

  "ErrorHandler" when {

    lazy val service: ErrorHandler = new ErrorHandler(messagesApi, mockAppConfig)

    "calling showInternalServerError" when {

      "language is set to English" should {

        lazy val fakeRequest = FakeRequest().withCookies(Cookie("PLAY_LANG", "en"))
        lazy val result = service.showInternalServerError(fakeRequest)
        implicit lazy val document: Document = Jsoup.parse(bodyOf(result))

        "return 500" in {
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }

        "render page in English" in {
          document.title shouldBe "There is a problem with the service - VAT - GOV.UK"
        }
      }

      "language is set to Welsh" should {

        lazy val fakeRequest = FakeRequest().withCookies(Cookie("PLAY_LANG", "cy"))
        lazy val result = service.showInternalServerError(fakeRequest)
        implicit lazy val document: Document = Jsoup.parse(bodyOf(result))

        "return 500" in {
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }

        "render page in Welsh" in {
          document.title shouldBe "Mae problem gyda’r gwasanaeth – TAW – GOV.UK"
        }
      }
    }

    "calling showBadRequestError" when {

      "language is set to English" should {

        lazy val fakeRequest = FakeRequest().withCookies(Cookie("PLAY_LANG", "en"))
        lazy val result = service.showBadRequestError(fakeRequest)
        implicit lazy val document: Document = Jsoup.parse(bodyOf(result))

        "return 400" in {
          status(result) shouldBe BAD_REQUEST
        }

        "render page in English" in {
          document.title shouldBe "Bad request - 400"
        }
      }

      "language is set to Welsh" should {

        lazy val fakeRequest = FakeRequest().withCookies(Cookie("PLAY_LANG", "cy"))
        lazy val result = service.showBadRequestError(fakeRequest)
        implicit lazy val document: Document = Jsoup.parse(bodyOf(result))

        "return 400" in {
          status(result) shouldBe BAD_REQUEST
        }

        "render page in Welsh" in {
          document.title shouldBe "Cais drwg – 400"
        }
      }
    }
  }
}
