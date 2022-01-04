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

import common.ConfigKeys
import config.features.Features
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.i18n.Lang
import play.api.mvc.Call
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

trait AppConfig {
  val reportAProblemPartialUrl: String
  val reportAProblemNonJSUrl: String
  val signInUrl: String
  val timeoutPeriod: Int
  val timeoutCountdown: Int
  val agentClientLookupStartUrl: String => String
  val agentClientUnauthorisedUrl: String => String
  val govUkGuidanceMtdVat: String
  val govUkGuidanceAgentServices: String
  val vatSummaryUrl: String
  val manageClientUrl: String
  val changeClientUrl: String
  val returnDeadlinesUrl: String
  val viewSubmittedReturnUrl: String
  def signOutUrl(identifier: String): String
  val unauthorisedSignOutUrl: String
  def exitSurveyUrl(identifier: String): String
  val features: Features
  val staticDateValue: String
  def submitReturnUrl(vrn: String): String
  val submitNrsUrl: String
  val agentActionUrl: String
  def feedbackUrl(redirectUrl: String): String
  def routeToSwitchLanguage: String => Call
  def languageMap: Map[String, Lang]
  val vatObligationsBaseUrl: String
  val vatSubscriptionBaseUrl: String
  val maximum9BoxReturnBoxRatio: Double
  val gtmContainer: String
  val platformHost: String
  val directDebitInterruptUrl : String
  val btaHomeUrl: String
}

@Singleton
class FrontendAppConfig @Inject()(configuration: Configuration, servicesConfig: ServicesConfig) extends AppConfig {

  import servicesConfig._

  lazy val appName: String = getString(ConfigKeys.appName)

  private val contactHost = getString(ConfigKeys.contactFrontendService)
  private val contactFormServiceIdentifier = "VATC"

  lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"

  // Gov.uk guidance
  override lazy val govUkGuidanceMtdVat: String = getString(ConfigKeys.govUkGuidanceMtdVat)
  override lazy val govUkGuidanceAgentServices: String = getString(ConfigKeys.govUkGuidanceAgentServices)

  //Language config
  override def routeToSwitchLanguage: String => Call = (lang: String) => controllers.routes.LanguageController.switchToLanguage(lang)
  override def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy")
  )

  // Sign-in
  private lazy val signInBaseUrl: String = getString(ConfigKeys.signInBaseUrl)
  private lazy val signInContinueBaseUrl: String = getString(ConfigKeys.signInContinueBaseUrl)
  private lazy val signInContinueUrl: String = signInContinueBaseUrl + getString(ConfigKeys.signInContinueUrl)
  private lazy val signInOrigin = getString(ConfigKeys.appName)
  override lazy val signInUrl: String = s"$signInBaseUrl?continue=$signInContinueUrl&origin=$signInOrigin"

  //Sign-out
  private lazy val feedbackSurveyBaseUrl =getString(ConfigKeys.feedbackSurveyHost) + getString(ConfigKeys.feedbackSurveyUrl)
  override def exitSurveyUrl(identifier: String): String = s"$feedbackSurveyBaseUrl/$identifier"

  //Session timeout countdown
  override lazy val timeoutCountdown: Int = getInt(ConfigKeys.timeoutCountdown)
  override lazy val timeoutPeriod: Int = getInt(ConfigKeys.timeoutPeriod)

  private lazy val governmentGatewayHost: String = getString(ConfigKeys.governmentGatewayHost)

  override lazy val unauthorisedSignOutUrl: String = s"$governmentGatewayHost/bas-gateway/sign-out-without-state?continue=$signInContinueUrl"
  override def signOutUrl(identifier: String): String =
    s"$governmentGatewayHost/bas-gateway/sign-out-without-state?continue=${exitSurveyUrl(identifier)}"

  override lazy val vatSummaryUrl: String = getString(ConfigKeys.vatSummaryHost) + getString(ConfigKeys.vatSummaryUrl)
  override lazy val directDebitInterruptUrl: String = getString(ConfigKeys.vatSummaryHost) + getString(ConfigKeys.vatSummaryDirectDebitUrl)
  override lazy val returnDeadlinesUrl: String = getString(ConfigKeys.viewVatReturnsHost) + getString(ConfigKeys.returnDeadlinesUrl)

  override lazy val viewSubmittedReturnUrl: String = getString(ConfigKeys.viewVatReturnsHost) + getString(ConfigKeys.submittedReturnsUrl)

  // Agent Client Lookup
  override lazy val platformHost: String = getString(ConfigKeys.platformHost)
  private lazy val agentClientLookupRedirectUrl: String => String = uri => SafeRedirectUrl(platformHost + uri).encodedUrl
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
    getString(ConfigKeys.vatAgentClientLookupFrontendHost) + getString(ConfigKeys.vatAgentClientLookupFrontendStartUrl)
  override lazy val agentActionUrl: String = agentClientLookupHost + getString(ConfigKeys.vatAgentClientLookupFrontendAgentActionUrl)

  private lazy val vatReturnsHost: String = baseUrl(ConfigKeys.vatReturnsBase)
  override def submitReturnUrl(vrn: String): String = s"$vatReturnsHost/${getString(ConfigKeys.submitReturnUrl)}/$vrn"
  override lazy val submitNrsUrl: String = s"$vatReturnsHost/${getString(ConfigKeys.submitNrsUrl)}"

  override val features = new Features(configuration)
  override lazy val staticDateValue: String = getString(ConfigKeys.staticDateValue)

  override def feedbackUrl(redirectUrl: String): String = s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier" +
    s"&backUrl=${SafeRedirectUrl(platformHost + redirectUrl).encodedUrl}"


  override val vatObligationsBaseUrl: String = baseUrl("vat-obligations")
  override val vatSubscriptionBaseUrl: String = baseUrl("vat-subscription")
  override val maximum9BoxReturnBoxRatio: Double = configuration.get[Double](ConfigKeys.nineBoxReturnAllowedRatio)

  override val gtmContainer: String = servicesConfig.getString(ConfigKeys.gtmContainer)
  override lazy val btaHomeUrl: String =
    servicesConfig.getString(ConfigKeys.businessTaxAccountHost) + servicesConfig.getString(ConfigKeys.businessTaxAccountUrl)

}
