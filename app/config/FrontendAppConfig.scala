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
import config.features.Features
import play.api.{Configuration, Environment}
import play.api.Mode.Mode
import uk.gov.hmrc.play.config.ServicesConfig
import common.ConfigKeys
import play.api.mvc.Call

trait AppConfig extends ServicesConfig {
  val analyticsToken: String
  val analyticsHost: String
  val betaFeedbackUrl: String
  val betaFeedbackUnauthenticatedUrl: String
  val features: Features
  val finalReturnPeriodKey: String
  val reportAProblemPartialUrl: String
  val reportAProblemNonJSUrl: String
  val vatApiBaseUrl: String
  val vatObligationsBaseUrl: String
  val vatReturnsBaseUrl: String
  val financialDataBaseUrl: String
  val staticDateValue: String
  val future2020DateValue: String
  val whitelistedIps: Seq[String]
  val whitelistEnabled: Boolean
  val whitelistExcludedPaths: Seq[Call]
  val shutterPage: String


}

@Singleton
class FrontendAppConfig @Inject()(val runModeConfiguration: Configuration, val environment: Environment) extends AppConfig {
  override protected def mode: Mode = environment.mode

  private val contactHost = getString(ConfigKeys.contactFrontendService)
  private val contactFormServiceIdentifier = "VATC"

  override lazy val analyticsToken: String = getString(ConfigKeys.googleAnalyticsToken)
  override lazy val analyticsHost: String = getString(ConfigKeys.googleAnalyticsHost)
  lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  override lazy val betaFeedbackUrl = s"$contactHost/contact/beta-feedback"
  override lazy val betaFeedbackUnauthenticatedUrl = s"$contactHost/contact/beta-feedback-unauthenticated"

  //Whitelist config
  private def whitelistConfig(key: String): Seq[String] = Some(new String(Base64.getDecoder
    .decode(getString(key)), "UTF-8"))
    .map(_.split(",")).getOrElse(Array.empty).toSeq

  override lazy val whitelistEnabled: Boolean = getBoolean(ConfigKeys.whitelistEnabled)
  override lazy val whitelistedIps: Seq[String] = whitelistConfig(ConfigKeys.whitelistedIps)
  override lazy val whitelistExcludedPaths: Seq[Call] = whitelistConfig(ConfigKeys.whitelistExcludedPaths) map
    (path => Call("GET", path))
  override val shutterPage: String = getString(ConfigKeys.whitelistShutterPage)


}