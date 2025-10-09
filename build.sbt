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

import play.sbt.routes.RoutesKeys
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings}

lazy val appDependencies: Seq[ModuleID] = compile ++ test
val appName = "submit-vat-return-frontend"
val bootstrapPlayVersion       = "8.6.0"
val playFrontendHmrc           = "12.11.0"

lazy val coverageSettings: Seq[Setting[?]] = {
  import scoverage.ScoverageKeys

  val excludedPackages = Seq(
    "<empty>",
    ".*Reverse.*",
    "app.*",
    "common.*",
    "config.*",
    "testOnly.*",
    "prod.*",
    "views.*"
  )

  Seq(
    ScoverageKeys.coverageExcludedPackages := excludedPackages.mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 95,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}

val compile = Seq(
  play.sbt.PlayImport.ws,
  "uk.gov.hmrc"       %% "bootstrap-frontend-play-30" % bootstrapPlayVersion,
  "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30" % playFrontendHmrc
)

val test = Seq(
  "uk.gov.hmrc"            %% "bootstrap-test-play-30"      % bootstrapPlayVersion,
  "org.scalamock"          %% "scalamock"                   % "5.2.0"
).map(_ % s"$Test, $IntegrationTest")

TwirlKeys.templateImports ++= Seq(
  "uk.gov.hmrc.govukfrontend.views.html.components._",
  "uk.gov.hmrc.hmrcfrontend.views.html.components._",
  "uk.gov.hmrc.hmrcfrontend.views.html.helpers._"
)

lazy val microservice: Project = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(PlayKeys.playDefaultPort := 9147)
  .settings(coverageSettings *)
  .settings(scalaSettings *)
  .settings(defaultSettings() *)
  .settings(majorVersion := 0)
  .settings(
    Test / Keys.fork := true,
    Test / javaOptions += "-Dlogger.resource=logback-test.xml",
    scalaVersion := "2.13.16",
    libraryDependencies ++= appDependencies,
    scalacOptions ++= Seq("-Wconf:cat=unused-imports&site=.*views.html.*:s", "-Wconf:cat=unused&src=routes/.*:s"),
    retrieveManaged := true,
    RoutesKeys.routesImport := Seq("uk.gov.hmrc.play.bootstrap.binders.RedirectUrl")
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings) *)
  .settings(
    IntegrationTest / Keys.fork := false,
    IntegrationTest / unmanagedSourceDirectories := (IntegrationTest / baseDirectory)(base => Seq(base / "it")).value,
    addTestReportOption(IntegrationTest, "int-test-reports"),
    IntegrationTest / parallelExecution := false,
    IntegrationTest / resourceDirectory := baseDirectory.value / "it" / "resources"
  )
