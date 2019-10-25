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

package mocks

import config.AppConfig
import config.features.Features
import play.api.Mode.Mode
import play.api.i18n.Lang
import play.api.mvc.Call
import play.api.{Configuration, Mode}

class MockConfig(implicit val runModeConfiguration: Configuration) extends AppConfig {

  override val mode: Mode = Mode.Test
  override val reportAProblemPartialUrl: String = ""
  override val reportAProblemNonJSUrl: String = ""
  override val betaFeedbackUrl: String = ""
  override val betaFeedbackUnauthenticatedUrl: String = ""
  override val whitelistEnabled: Boolean = false
  override val whitelistedIps: Seq[String] = Seq("")
  override val whitelistExcludedPaths: Seq[Call] = Nil
  override val shutterPage: String = "https://www.tax.service.gov.uk/shutter/vat-through-software"
  override val signInUrl: String = "sign-in-url"
  override def signOutUrl(identifier: String): String = s"/some-gg-signout-url/$identifier"
  override def exitSurveyUrl(identifier: String): String = s"/some-survey-url/$identifier"
  override val timeoutCountdown: Int = 20
  override val timeoutPeriod: Int = 1800
  override val unauthorisedSignOutUrl: String = "/unauth-signout-url"
  override val agentClientLookupStartUrl: String => String = uri => s"agent-client-lookup-start-url/$uri"
  override val agentClientUnauthorisedUrl: String => String = uri => s"agent-client-unauthorised-url/$uri"
  override val govUkGuidanceMtdVat: String = "mtd-vat"
  override val govUkGuidanceAgentServices: String = "agent-services"
  override val vatSummaryUrl: String = "vat-summary-frontend-url"
  override val returnDeadlinesUrl: String = "/return-deadlines"
  override def submitReturnUrl(vrn: String): String = s"url/$vrn"
  override val submitNrsUrl: String = "/submit-nrs"
  override val features: Features = new Features(runModeConfiguration)
  override val staticDateValue: String = "2018-05-01"
  override val manageClientUrl: String = "/agent-action"
  override val changeClientUrl: String = "/change-client"
  override val agentActionUrl: String = "/agent-action"
  override val accessibilityLinkUrl: String = "/accessibility"
  override def feedbackUrl(redirectUrl: String): String = s"feedback-url/$redirectUrl"
  override def routeToSwitchLanguage: String => Call =
    (lang: String) => controllers.routes.LanguageController.switchToLanguage(lang)
  override def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy")
  )

  override val viewSubmittedReturnUrl: String = "/submittedreturns/year/periodkey"
}
