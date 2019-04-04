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
import play.api.libs.ws.WSClient
import utils.WireMockHelper

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Awaitable}

trait BaseISpec extends WordSpec with WireMockHelper with Matchers with
  BeforeAndAfterEach with GuiceOneServerPerSuite {

  lazy val ws: WSClient = app.injector.instanceOf[WSClient]
  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val connector: VatSubscriptionConnector = new VatSubscriptionConnector(ws, appConfig)

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
