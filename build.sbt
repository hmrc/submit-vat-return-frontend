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

import play.core.PlayVersion
import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

lazy val appDependencies: Seq[ModuleID] = compile ++ test
val appName = "submit-vat-return-frontend"
lazy val plugins: Seq[Plugins] = Seq.empty
lazy val playSettings: Seq[Setting[_]] = Seq.empty

lazy val coverageSettings: Seq[Setting[_]] = {
  import scoverage.ScoverageKeys

  val excludedPackages = Seq(
    "<empty>",
    "Reverse.*",
    ".*standardError*.*",
    ".*govuk_wrapper*.*",
    ".*main_template*.*",
    "uk.gov.hmrc.BuildInfo",
    "testOnlyDoNotUseInAppConf.*",
    "app.*",
    "common.*",
    "config.*",
    "testOnly.*",
    ".*LanguageSwitchController",
    "prod.*",
    "views.*")

  Seq(
    ScoverageKeys.coverageExcludedPackages := excludedPackages.mkString(";"),
    ScoverageKeys.coverageMinimum := 95,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}

val compile = Seq(
  play.sbt.PlayImport.ws,
  "uk.gov.hmrc" %% "govuk-template" % "5.38.0-play-25",
  "uk.gov.hmrc" %% "play-ui" % "8.0.0-play-25",
  "uk.gov.hmrc" %% "bootstrap-play-25" % "4.16.0",
  "uk.gov.hmrc" %% "play-whitelist-filter" % "3.1.0-play-25",
  "uk.gov.hmrc" %% "play-language" % "3.4.0",
  "uk.gov.hmrc" %% "auth-client" % "2.27.2-play-25",
  "uk.gov.hmrc" %% "domain" % "5.6.0-play-25"
)

val test = Seq(
  "uk.gov.hmrc" %% "hmrctest" % "3.9.0-play-25",
  "org.scalatest" %% "scalatest" % "3.0.8",
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1",
  "org.pegdown" % "pegdown" % "1.6.0",
  "org.jsoup" % "jsoup" % "1.12.1",
  "com.typesafe.play" %% "play-test" % PlayVersion.current,
  "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0",
  "com.github.tomakehurst" % "wiremock" % "2.23.2"
).map(_ % s"$Test, $IntegrationTest")

def oneForkedJvmPerTest(tests: Seq[TestDefinition]): Seq[Group] = tests map {
  test =>
    Group(
      test.name,
      Seq(test),
      SubProcess(ForkOptions(runJVMOptions = Seq("-Dtest.name=" + test.name, "-Dlogger.resource=logback-test.xml")))
    )
}

lazy val microservice: Project = Project(appName, file("."))
  .enablePlugins(Seq(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory) ++ plugins: _*)
  .settings(PlayKeys.playDefaultPort := 9147)
  .settings(coverageSettings: _*)
  .settings(playSettings: _*)
  .settings(scalaSettings: _*)
  .settings(publishingSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(majorVersion := 0)
  .settings(
    Keys.fork in Test := true,
    javaOptions in Test += "-Dlogger.resource=logback-test.xml",
    scalaVersion := "2.11.11",
    libraryDependencies ++= appDependencies,
    retrieveManaged := true,
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    routesGenerator := InjectedRoutesGenerator
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    Keys.fork in IntegrationTest := false,
    unmanagedSourceDirectories in IntegrationTest <<= (baseDirectory in IntegrationTest)(base => Seq(base / "it")),
    addTestReportOption(IntegrationTest, "int-test-reports"),
    testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value),
    parallelExecution in IntegrationTest := false,
    resourceDirectory in IntegrationTest := baseDirectory.value / "it" / "resources")
  .settings(resolvers ++= Seq(
    Resolver.bintrayRepo("hmrc", "releases"),
    Resolver.jcenterRepo
  ))
