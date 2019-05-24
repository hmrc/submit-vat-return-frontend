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
import play.api.Mode.Mode
import play.api.mvc.Call
import play.api.{Configuration, Mode}

class MockConfig(implicit val runModeConfiguration: Configuration) extends AppConfig {

  override val mode: Mode = Mode.Test
  override val analyticsToken: String = ""
  override val analyticsHost: String = ""
  override val reportAProblemPartialUrl: String = ""
  override val reportAProblemNonJSUrl: String = ""
  override val betaFeedbackUrl: String = ""
  override val betaFeedbackUnauthenticatedUrl: String = ""
  override val whitelistEnabled: Boolean = false
  override val whitelistedIps: Seq[String] = Seq("")
  override val whitelistExcludedPaths: Seq[Call] = Nil
  override val shutterPage: String = "https://www.tax.service.gov.uk/shutter/vat-through-software"
  override val signInUrl: String = "sign-in-url"
  override val agentClientLookupStartUrl: String => String = uri => s"agent-client-lookup-start-url/$uri"
  override val agentClientUnauthorisedUrl: String => String = uri => s"agent-client-unauthorised-url/$uri"
  override val govUkGuidanceMtdVat: String = "mtd-vat"
  override val govUkGuidanceAgentServices: String = "agent-services"
  override val vatSummaryUrl: String = "vat-summary-frontend-url"
  override val returnDeadlinesUrl: String = "/return-deadlines"
}
