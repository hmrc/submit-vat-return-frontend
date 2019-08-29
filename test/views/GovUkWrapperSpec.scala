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

package views

import models.auth.User
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.AnyContentAsEmpty

class GovUkWrapperSpec extends ViewBaseSpec {

  val navTitleSelector = ".header__menu__proposition-name"

  "Calling .govuk_wrapper" when {

    "user is not known" should {

      lazy val view = views.html.govuk_wrapper(mockAppConfig, "title", user = None)(fakeRequest, messages)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the default nav title 'VAT" in {
        elementText(navTitleSelector) shouldBe "VAT"
      }
    }

    "user is known" when {

      "user is an agent" should {

        lazy val view = views.html.govuk_wrapper(mockAppConfig, "title", user = Some(User[AnyContentAsEmpty.type]("999999999", arn = Some("XARN1234567")))) (fakeRequest, messages)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have a nav title of 'Client’s VAT details'" in {
          elementText(navTitleSelector) shouldBe "Client’s VAT details"
        }
      }

      "user is not an agent" should {

        lazy val view = views.html.govuk_wrapper(mockAppConfig, "title", user = Some(User[AnyContentAsEmpty.type]("999999999", arn = None))) (fakeRequest, messages)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have a nav title of 'Business tax account'" in {
          elementText(navTitleSelector) shouldBe "Business tax account"
        }
      }
    }
  }
}
