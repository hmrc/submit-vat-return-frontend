/*
 * Copyright 2020 HM Revenue & Customs
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

import play.core.PlayVersion
import play.sbt.routes.RoutesKeys
import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

lazy val appDependencies: Seq[ModuleID] = compile ++ test
val appName = "submit-vat-return-frontend"

lazy val coverageSettings: Seq[Setting[_]] = {
  import scoverage.ScoverageKeys

  val excludedPackages = Seq(
    "<empty>",
    ".*Reverse.*",
    ".*standardError*.*",
    ".*GovUkWrapper*.*",
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
  "uk.gov.hmrc"       %% "govuk-template"             % "5.56.0-play-26",
  "uk.gov.hmrc"       %% "play-ui"                    % "8.12.0-play-26",
  "uk.gov.hmrc"       %% "bootstrap-frontend-play-26" % "2.24.0",
  "uk.gov.hmrc"       %% "play-language"              % "4.4.0-play-26",
  "uk.gov.hmrc"       %% "play-partials"              % "6.11.0-play-26",
  "uk.gov.hmrc"       %% "domain"                     % "5.9.0-play-26",
  "com.typesafe.play" %% "play-json-joda"             % "2.9.0"
)

val test = Seq(
  "uk.gov.hmrc"            %% "bootstrap-play-26"           % "1.14.0" classifier "tests",
  "uk.gov.hmrc"            %% "hmrctest"                    % "3.9.0-play-26",
  "org.scalatest"          %% "scalatest"                   % "3.0.8",
  "org.scalatestplus.play" %% "scalatestplus-play"          % "3.1.0",
  "org.pegdown"             % "pegdown"                     % "1.6.0",
  "org.jsoup"               % "jsoup"                       % "1.13.1",
  "com.typesafe.play"      %% "play-test"                   % PlayVersion.current,
  "org.scalamock"          %% "scalamock-scalatest-support" % "3.6.0",
  "com.github.tomakehurst"  % "wiremock-jre8"               % "2.26.3"
).map(_ % s"$Test, $IntegrationTest")

def oneForkedJvmPerTest(tests: Seq[TestDefinition]): Seq[Group] = tests map {
  test =>
    Group(test.name, Seq(test), SubProcess(
      ForkOptions().withRunJVMOptions(Vector("-Dtest.name=" + test.name, "-Dlogger.resource=logback-test.xml"))
    ))
}

lazy val microservice: Project = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory)
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
    resourceDirectory in IntegrationTest := baseDirectory.value / "it" / "resources")
  .settings(resolvers ++= Seq(
    Resolver.bintrayRepo("hmrc", "releases"),
    Resolver.jcenterRepo
  ))
