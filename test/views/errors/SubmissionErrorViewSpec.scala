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

package views.errors

import assets.messages.SubmissionErrorMessages
import auth.AuthKeys
import models.auth.User
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.ViewBaseSpec
import views.html.errors.SubmissionError

class SubmissionErrorViewSpec extends ViewBaseSpec {

  val submissionError: SubmissionError = inject[SubmissionError]

  "Rendering the Submission error page" when {

    object Selectors {
      val pageHeading = "#content > h1"
      val p1 = "#content > p:nth-child(2)"
      val p2 = "#content > p:nth-child(3)"
      val userLink = "#content > p > a"
    }

    "the user is a non agent" should {

      lazy val view = submissionError()(mockAppConfig, messages, user)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct document title" in {
        document.title shouldBe SubmissionErrorMessages.title
      }

      "have a the correct page heading" in {
        elementText(Selectors.pageHeading) shouldBe SubmissionErrorMessages.heading
      }

      "have the correct first paragraph on the page" in {
        elementText(Selectors.p1) shouldBe SubmissionErrorMessages.p1
      }

      "have the correct second paragraph on the page" in {
        elementText(Selectors.p2) shouldBe SubmissionErrorMessages.p2
      }

      "have a link to the vat-summary page" in {
        elementText(Selectors.userLink) shouldBe SubmissionErrorMessages.userLink
        element(Selectors.userLink).attr("href") shouldBe mockAppConfig.vatSummaryUrl
      }
    }

    "the user is an agent" should {

      lazy val fakeRequestWithClientsVRN: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession(AuthKeys.agentSessionVrn -> "999999999")
      lazy val agentUser: User[AnyContentAsEmpty.type] = User[AnyContentAsEmpty.type]("999999999", Some("XAIT012345678"))(fakeRequestWithClientsVRN)
      lazy val view = submissionError()(mockAppConfig, messages, user = agentUser)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have a link to the agent-action page" in {
        elementText(Selectors.userLink) shouldBe SubmissionErrorMessages.agentLink
        element(Selectors.userLink).attr("href") shouldBe mockAppConfig.agentActionUrl
      }
    }
  }

}
