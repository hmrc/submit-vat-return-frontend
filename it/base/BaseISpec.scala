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

package base

import common.SessionKeys
import config.AppConfig
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.data.Form
import play.api.http.HeaderNames
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.{Application, Environment, Mode}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import utils.WireMockHelper

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Awaitable}

trait BaseISpec extends WordSpec with WireMockHelper with Matchers with
  BeforeAndAfterEach with GuiceOneServerPerSuite {

  def servicesConfig: Map[String, String] = Map(
    "play.filters.csrf.header.bypassHeaders.Csrf-Token" -> "nocheck",
    "microservice.services.vat-subscription.host" -> WireMockHelper.wireMockHost,
    "microservice.services.vat-subscription.port" -> WireMockHelper.wireMockPort.toString,
    "microservice.services.vat-obligations.host" -> WireMockHelper.wireMockHost,
    "microservice.services.vat-obligations.port" -> WireMockHelper.wireMockPort.toString,
    "microservice.services.vat-returns.host" -> WireMockHelper.wireMockHost,
    "microservice.services.vat-returns.port" -> WireMockHelper.wireMockPort.toString,
    "microservice.services.auth.host" -> WireMockHelper.wireMockHost,
    "microservice.services.auth.port" -> WireMockHelper.wireMockPort.toString,
    "play.filters.csrf.header.bypassHeaders.Csrf-Token" -> "nocheck"
  )

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure(servicesConfig)
    .build()

  lazy val httpClient: HttpClient = app.injector.instanceOf[HttpClient]
  lazy val wsClient: WSClient = app.injector.instanceOf[WSClient]
  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  val appRouteContext: String = "/vat-through-software/submit-vat-return"

  override def beforeEach(): Unit = {
    super.beforeEach()
    startServer()
  }

  override def afterEach(): Unit = {
    stopServer()
    super.afterEach()
  }

  def await[T](awaitable: Awaitable[T]): T = Await.result(awaitable, Duration.Inf)

  def get(path: String, additionalCookies: Map[String, String] = Map.empty): WSResponse = await(
    buildRequest(path, additionalCookies).get()
  )

  def postJson(path: String, additionalCookies: Map[String, String] = Map.empty, body: JsValue = Json.obj()): WSResponse = await(
    buildRequest(path, additionalCookies).post(body)
  )

  def postForm(path: String, additionalCookies: Map[String, String] = Map.empty, body: Map[String, Seq[String]]): WSResponse = await(
    buildRequest(path, additionalCookies).post(body)
  )

  def buildRequest(path: String, additionalCookies: Map[String, String] = Map.empty): WSRequest =
    wsClient.url(s"http://localhost:$port$appRouteContext$path")
      .withHeaders(HeaderNames.COOKIE -> SessionCookieBaker.bakeSessionCookie(additionalCookies), "Csrf-Token" -> "nocheck")
      .withFollowRedirects(false)

  def formatSessionMandationStatus: Option[String] => Map[String, String] =_.fold(Map.empty[String, String])(x => Map(SessionKeys.mandationStatus -> x))

  def formatViewModel: Option[String] => Map[String, String] =_.fold(Map.empty[String, String])(x => Map(SessionKeys.viewModel -> x))

  def toFormData[T](form: Form[T], data: T): Map[String, Seq[String]] =
    form.fill(data).data map { case (k, v) => k -> Seq(v) }
}
