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

import play.sbt.routes.RoutesKeys
import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

lazy val appDependencies: Seq[ModuleID] = compile ++ test
val appName = "submit-vat-return-frontend"

lazy val coverageSettings: Seq[Setting[_]] = {
  import scoverage.ScoverageKeys

  val excludedPackages = Seq(
    "<empty>",
    ".*Reverse.*",
    ".*MainTemplate*.*",
    "uk.gov.hmrc.BuildInfo",
    "testOnlyDoNotUseInAppConf.*",
    "app.*",
    "common.*",
    "config.*",
    "testOnly.*",
    "prod.*",
    "views.*",
    "com.kenshoo.play.metrics.*",
    "controllers.javascript.*"
  )

  Seq(
    ScoverageKeys.coverageExcludedPackages := excludedPackages.mkString(";"),
    ScoverageKeys.coverageMinimum := 95,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}

val compile = Seq(
  play.sbt.PlayImport.ws,
  "uk.gov.hmrc"       %% "play-ui"                    % "9.6.0-play-26",
  "uk.gov.hmrc"       %% "play-frontend-govuk"        % "0.80.0-play-26",
  "uk.gov.hmrc"       %% "play-frontend-hmrc"         % "0.80.0-play-26",
  "uk.gov.hmrc"       %% "bootstrap-frontend-play-26" % "5.6.0",
  "uk.gov.hmrc"       %% "play-language"              % "5.1.0-play-26",
  "uk.gov.hmrc"       %% "domain"                     % "5.11.0-play-26",
  "com.typesafe.play" %% "play-json-joda"             % "2.9.1"
)

val test = Seq(
  "uk.gov.hmrc"            %% "hmrctest"                    % "3.10.0-play-26",
  "org.scalatest"          %% "scalatest"                   % "3.0.9",
  "org.scalatestplus.play" %% "scalatestplus-play"          % "3.1.3",
  "org.pegdown"             % "pegdown"                     % "1.6.0",
  "org.jsoup"               % "jsoup"                       % "1.13.1",
  "org.scalamock"          %% "scalamock-scalatest-support" % "3.6.0",
  "com.github.tomakehurst"  % "wiremock-jre8"               % "2.27.2"
).map(_ % s"$Test, $IntegrationTest")

TwirlKeys.templateImports ++= Seq(
  "uk.gov.hmrc.govukfrontend.views.html.components._",
  "uk.gov.hmrc.govukfrontend.views.html.helpers._",
  "uk.gov.hmrc.hmrcfrontend.views.html.components._",
  "uk.gov.hmrc.hmrcfrontend.views.html.helpers._"
)

def oneForkedJvmPerTest(tests: Seq[TestDefinition]): Seq[Group] = tests map {
  test =>
    Group(test.name, Seq(test), SubProcess(
      ForkOptions().withRunJVMOptions(Vector("-Dtest.name=" + test.name, "-Dlogger.resource=logback-test.xml"))
    ))
}

lazy val microservice: Project = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(PlayKeys.playDefaultPort := 9147)
  .settings(coverageSettings: _*)
  .settings(scalaSettings: _*)
  .settings(publishingSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(majorVersion := 0)
  .settings(
    Keys.fork in Test := true,
    javaOptions in Test += "-Dlogger.resource=logback-test.xml",
    scalaVersion := "2.12.12",
    libraryDependencies ++= appDependencies,
    retrieveManaged := true,
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    RoutesKeys.routesImport := Seq.empty
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    Keys.fork in IntegrationTest := false,
    unmanagedSourceDirectories in IntegrationTest := (baseDirectory in IntegrationTest)(base => Seq(base / "it")).value,
    addTestReportOption(IntegrationTest, "int-test-reports"),
    testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value),
    parallelExecution in IntegrationTest := false,
    resourceDirectory in IntegrationTest := baseDirectory.value / "it" / "resources"
  )
