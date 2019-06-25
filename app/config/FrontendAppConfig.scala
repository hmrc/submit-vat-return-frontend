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

package config

import java.util.Base64
import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Environment}
import play.api.Mode.Mode
import uk.gov.hmrc.play.config.ServicesConfig
import common.ConfigKeys
import config.features.Features
import play.api.mvc.Call
import uk.gov.hmrc.play.binders.ContinueUrl

trait AppConfig extends ServicesConfig {
  val analyticsToken: String
  val analyticsHost: String
  val reportAProblemPartialUrl: String
  val reportAProblemNonJSUrl: String
  val betaFeedbackUrl: String
  val betaFeedbackUnauthenticatedUrl: String
  val whitelistedIps: Seq[String]
  val whitelistEnabled: Boolean
  val whitelistExcludedPaths: Seq[Call]
  val shutterPage: String
  val signInUrl: String
  val agentClientLookupStartUrl: String => String
  val agentClientUnauthorisedUrl: String => String
  val govUkGuidanceMtdVat: String
  val govUkGuidanceAgentServices: String
  val vatSummaryUrl: String
  val returnDeadlinesUrl: String
  val signOutUrl: String
  val feedbackSurveyUrl: String
  val features: Features
  val staticDateValue: String

  val manageClientUrl: String
  val changeClientUrl: String

  def vatReturnsUrl(vrn: String): String
}

@Singleton
class FrontendAppConfig @Inject()(val runModeConfiguration: Configuration, environment: Environment) extends AppConfig {
  override protected def mode: Mode = environment.mode

  private val contactHost = getString(ConfigKeys.contactFrontendService)
  private val contactFormServiceIdentifier = "VATC"

  override lazy val analyticsToken: String = getString(ConfigKeys.googleAnalyticsToken)
  override lazy val analyticsHost: String = getString(ConfigKeys.googleAnalyticsHost)
  lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  override lazy val betaFeedbackUrl = s"$contactHost/contact/beta-feedback"
  override lazy val betaFeedbackUnauthenticatedUrl = s"$contactHost/contact/beta-feedback-unauthenticated"

  // Gov.uk guidance
  override lazy val govUkGuidanceMtdVat: String = getString(ConfigKeys.govUkGuidanceMtdVat)
  override lazy val govUkGuidanceAgentServices: String = getString(ConfigKeys.govUkGuidanceAgentServices)

  // Whitelist config
  private def whitelistConfig(key: String): Seq[String] = Some(new String(Base64.getDecoder
    .decode(getString(key)), "UTF-8"))
    .map(_.split(",")).getOrElse(Array.empty).toSeq

  override lazy val whitelistEnabled: Boolean = getBoolean(ConfigKeys.whitelistEnabled)
  override lazy val whitelistedIps: Seq[String] = whitelistConfig(ConfigKeys.whitelistedIps)
  override lazy val whitelistExcludedPaths: Seq[Call] = whitelistConfig(ConfigKeys.whitelistExcludedPaths) map
    (path => Call("GET", path))
  override val shutterPage: String = getString(ConfigKeys.whitelistShutterPage)

  // Sign-in
  private lazy val signInBaseUrl: String = getString(ConfigKeys.signInBaseUrl)
  private lazy val signInContinueBaseUrl: String = getString(ConfigKeys.signInContinueBaseUrl)
  private lazy val signInContinueUrl: String = signInContinueBaseUrl + getString(ConfigKeys.signInContinueUrl)
  private lazy val signInOrigin = getString(ConfigKeys.appName)
  override lazy val signInUrl: String = s"$signInBaseUrl?continue=$signInContinueUrl&origin=$signInOrigin"

  //Sign-out
  private lazy val feedbackSurveyBaseUrl = getString(ConfigKeys.feedbackSurveyHost) + getString(ConfigKeys.feedbackSurveyUrl)

  override lazy val feedbackSurveyUrl = s"$feedbackSurveyBaseUrl/$contactFormServiceIdentifier"


  private lazy val governmentGatewayHost: String = getString(ConfigKeys.governmentGatewayHost)

  override lazy val signOutUrl = s"$governmentGatewayHost/gg/sign-out?continue=$feedbackSurveyUrl"

  override lazy val vatSummaryUrl: String = getString(ConfigKeys.vatSummaryHost) + getString(ConfigKeys.vatSummaryUrl)
  override lazy val returnDeadlinesUrl: String = getString(ConfigKeys.viewVatReturnsHost) + getString(ConfigKeys.returnDeadlinesUrl)

  // Agent Client Lookup
  private lazy val platformHost = getString(ConfigKeys.platformHost)
  private lazy val agentClientLookupRedirectUrl: String => String = uri => ContinueUrl(platformHost + uri).encodedUrl
  private lazy val agentClientLookupHost = getString(ConfigKeys.vatAgentClientLookupFrontendHost)
  override lazy val agentClientLookupStartUrl: String => String = uri =>
    agentClientLookupHost +
    getString(ConfigKeys.vatAgentClientLookupFrontendStartUrl) +
    s"?redirectUrl=${agentClientLookupRedirectUrl(uri)}"
  override lazy val agentClientUnauthorisedUrl: String => String = uri =>
    agentClientLookupHost +
    getString(ConfigKeys.vatAgentClientLookupFrontendUnauthorisedUrl) +
    s"?redirectUrl=${agentClientLookupRedirectUrl(uri)}"

  override lazy val manageClientUrl: String =
    getString(ConfigKeys.vatAgentClientLookupFrontendNonStubHost) + getString(ConfigKeys.manageClientUrl)
  override lazy val changeClientUrl: String =
    getString(ConfigKeys.vatAgentClientLookupFrontendHost) + getString(ConfigKeys.changeClientUrl)

  override def vatReturnsUrl(vrn: String): String = s"${baseUrl(ConfigKeys.vatReturnsBase)}/${getString(ConfigKeys.vatReturnsUrl)}/$vrn"

  override val features = new Features(runModeConfiguration)
  override lazy val staticDateValue: String = getString(ConfigKeys.staticDateValue)

}
