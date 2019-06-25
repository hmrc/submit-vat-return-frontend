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

import assets.messages.{ConfirmationPageMessages => viewMessages}
import models.auth.User
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.AnyContentAsEmpty


class ConfirmationViewSpec extends ViewBaseSpec {

  val user = User[AnyContentAsEmpty.type]("999999999")(fakeRequest)

  "The confirmation view" should {

    lazy val view = views.html.confirmation_view() (fakeRequest, messages, mockAppConfig, user)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    s"display the title as ${viewMessages.title}" in {
      elementText("title") shouldBe viewMessages.title
    }

    s"display the h1 as ${viewMessages.heading}" in {
      elementText("h1") shouldBe viewMessages.heading
    }

    s"display the h2 as ${viewMessages.subHeading}" in {
      elementText("h2") shouldBe viewMessages.subHeading
    }

    s"display the paragraph text as ${viewMessages.paragraph}" in {
      elementText("#content p") shouldBe viewMessages.paragraph
    }

    "display a button" should {

      s"have the button text as ${viewMessages.button}" in {
        elementText("#content a") shouldBe viewMessages.button
      }

      "have the correct redirect link" in {
        element("#content a").attr("href") shouldBe "/what-to-do"
      }
    }

    "display the change client link" when {
      lazy val agent: User[AnyContentAsEmpty.type] = User[AnyContentAsEmpty.type]("999999999", Some("123456789"))
      lazy val feature_view = {
        views.html.confirmation_view() (fakeRequest, messages, mockAppConfig, agent)
      }
      lazy implicit val feature_document: Document = Jsoup.parse(feature_view.body)

      val changeClientLink = "#content > article > p:nth-child(4) > a"

      "the user is an agent" in {
        val changeClientLinkElem = element(changeClientLink)(feature_document)

        changeClientLinkElem.text() shouldBe "Change client"
        changeClientLinkElem.attributes().get("href") shouldBe "/change-client"
      }
    }

    "not display the change client link" when {
      lazy val agent: User[AnyContentAsEmpty.type] = User[AnyContentAsEmpty.type]("999999999", None)
      lazy val feature_view = {
        views.html.confirmation_view() (fakeRequest, messages, mockAppConfig, agent)
      }
      lazy implicit val feature_document: Document = Jsoup.parse(feature_view.body)

      val changeClientLink = "#content > article > p:nth-child(4) > a"

      "the user is not an agent" in {
        val changeClientLinkElem = element(changeClientLink)(feature_document)

        changeClientLinkElem.text() shouldNot be("Change client")
      }
    }

    "display the change client link" when {
      lazy val agent: User[AnyContentAsEmpty.type] = User[AnyContentAsEmpty.type]("999999999", Some("123456789"))
      lazy val feature_view = {
        views.html.confirmation_view() (fakeRequest, messages, mockAppConfig, agent)
      }
      lazy implicit val feature_document: Document = Jsoup.parse(feature_view.body)

      val changeClientLink = "#content > article > p:nth-child(4) > a"

      "the user is an agent" in {
        val changeClientLinkElem = element(changeClientLink)(feature_document)

        changeClientLinkElem.text() shouldBe "Change client"
        changeClientLinkElem.attributes().get("href") shouldBe "/change-client"
      }
    }

    "not display the change client link" when {
      lazy val agent: User[AnyContentAsEmpty.type] = User[AnyContentAsEmpty.type]("999999999", None)
      lazy val feature_view = {
        views.html.confirmation_view() (fakeRequest, messages, mockAppConfig, agent)
      }
      lazy implicit val feature_document: Document = Jsoup.parse(feature_view.body)

      val changeClientLink = "#content > article > p:nth-child(4) > a"

      "the user is not an agent" in {
        val changeClientLinkElem = element(changeClientLink)(feature_document)

        changeClientLinkElem.text() shouldNot be("Change client")
      }
    }
  }
}
