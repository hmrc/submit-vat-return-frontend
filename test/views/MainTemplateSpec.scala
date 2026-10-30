/*
 * Copyright 2023 HM Revenue & Customs
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

import common.ConfigKeys
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import uk.gov.hmrc.hmrcfrontend.views.Aliases.UserResearchBanner
import views.helpers.UrBannerUtil
import views.html.MainTemplate

class MainTemplateSpec extends ViewBaseSpec {

  val injectedView: MainTemplate = inject[MainTemplate]
  val userBannerHeading: String = "Help make GOV.UK better"
  lazy val urBanner: UserResearchBanner = UrBannerUtil.getUrBanner()(mockAppConfig, messages)
  lazy val urBannerWithCloseButton: UserResearchBanner = UrBannerUtil.getUrBanner(hideCloseButton = false)(mockAppConfig, messages)

  override def afterEach(): Unit = {
    sys.props -= ConfigKeys.showUserResearchBannerEnabled
    super.afterEach()
  }

  object Selectors {
    val userResearchBanner = ".hmrc-user-research-banner"
    val userResearchBannerHeading = ".hmrc-user-research-banner__title"
    val userResearchBannerCloseButton = ".hmrc-user-research-banner__close"
  }

  "MainTemplate" when {

    "the user is an Agent" should {

      lazy val view = injectedView("Title of page",mockAppConfig, user = Some(agentUser))(Html(""))(fakeRequestWithClientsVRN, messages)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct service name" in {
        elementText(".govuk-service-navigation__link") shouldBe "Your client’s VAT details"
      }

      "have the correct service URL" in {
        element(".govuk-service-navigation__link").attr("href") shouldBe mockAppConfig.manageClientUrl
      }
    }

    "the user is not an Agent" should {

      lazy val view = injectedView("Title of page",mockAppConfig,user = Some(user))(Html(""))(fakeRequest, messages)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct service name" in {
        elementText(".govuk-service-navigation__link") shouldBe "Manage your VAT account"
      }

      "have the correct service URL" in {
        element(".govuk-service-navigation__link").attr("href") shouldBe mockAppConfig.vatSummaryUrl
      }
    }

    "the user type cannot be determined" should {

      lazy val view = injectedView("Title of page",mockAppConfig)(Html(""))(fakeRequest, messages)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct service name" in {
        elementText(".govuk-service-navigation__text") shouldBe "VAT"
      }

      "have the correct service URL" in {
        element(".govuk-service-navigation__text").attr("href") shouldBe ""
      }
    }

    "the showUserResearchBanner feature switch is turned off" when {

      "a user research banner is provided" should {

        lazy val view = injectedView("Title of page", mockAppConfig, user = Some(user), urBanner = Some(urBannerWithCloseButton))(Html(""))(fakeRequest, messages)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "not display the user research banner" in {
          mockAppConfig.features.showUserResearchBannerEnabled(false)
          document.select(Selectors.userResearchBanner).size shouldBe 0
        }
      }
    }

    "the showUserResearchBanner feature switch is turned on" when {

      "a user research banner with a close button is provided" should {

        lazy val view = injectedView("Title of page", mockAppConfig, user = Some(user), urBanner = Some(urBannerWithCloseButton))(Html(""))(fakeRequest, messages)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "display the correct user research banner heading" in {
          mockAppConfig.features.showUserResearchBannerEnabled(true)
          elementText(Selectors.userResearchBannerHeading) shouldBe userBannerHeading
        }

        "display the user research banner link with the configured url" in {
          mockAppConfig.features.showUserResearchBannerEnabled(true)
          element(s"${Selectors.userResearchBanner} a").attr("href") shouldBe mockAppConfig.urBannerUrl
        }

        "display the close button" in {
          mockAppConfig.features.showUserResearchBannerEnabled(true)
          document.select(Selectors.userResearchBannerCloseButton).size shouldBe 1
        }
      }

      "a user research banner without a close button is provided" should {

        lazy val view = injectedView("Title of page", mockAppConfig, user = Some(user), urBanner = Some(urBanner))(Html(""))(fakeRequest, messages)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "display the correct user research banner heading" in {
          mockAppConfig.features.showUserResearchBannerEnabled(true)
          elementText(Selectors.userResearchBannerHeading) shouldBe userBannerHeading
        }

        "not display the close button" in {
          mockAppConfig.features.showUserResearchBannerEnabled(true)
          document.select(Selectors.userResearchBannerCloseButton).size shouldBe 0
        }
      }

      "no user research banner is provided" should {

        lazy val view = injectedView("Title of page", mockAppConfig, user = Some(user))(Html(""))(fakeRequest, messages)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "not display the user research banner" in {
          mockAppConfig.features.showUserResearchBannerEnabled(true)
          document.select(Selectors.userResearchBanner).size shouldBe 0
        }
      }
    }
  }
}
