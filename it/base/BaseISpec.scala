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

import config.AppConfig
import connectors.VatSubscriptionConnector
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.{Application, Environment, Mode}
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import utils.WireMockHelper

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Awaitable, ExecutionContext}

trait BaseISpec extends WordSpec with WireMockHelper with Matchers with
  BeforeAndAfterEach with GuiceOneServerPerSuite {

  def servicesConfig: Map[String, String] = Map(
    "microservice.services.vat-subscription.host" -> WireMockHelper.wireMockHost,
    "microservice.services.vat-subscription.port" -> WireMockHelper.wireMockPort.toString,
    "microservice.services.vat-obligations.host" -> WireMockHelper.wireMockHost,
    "microservice.services.vat-obligations.port" -> WireMockHelper.wireMockPort.toString
  )

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure(servicesConfig)
    .build()

  lazy val httpClient: HttpClient = app.injector.instanceOf[HttpClient]
  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

   override def beforeEach(): Unit = {
    super.beforeEach()
    startServer()
  }

  override def afterEach(): Unit = {
    stopServer()
    super.afterEach()
  }

  def await[T](awaitable: Awaitable[T]): T = Await.result(awaitable, Duration.Inf)
}
