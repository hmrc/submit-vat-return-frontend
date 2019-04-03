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
import mocks.MockAppConfig
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.guice._
import play.api.Configuration
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.Injector
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest

trait BaseSpec extends WordSpec with Matchers with GuiceOneAppPerSuite {

  lazy val injector: Injector = app.injector

  implicit val config: Configuration = app.configuration

  implicit val mockAppConfig: AppConfig = new MockAppConfig

  implicit lazy val messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

  implicit lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")

  implicit lazy val messages: Messages = messagesApi.preferred(fakeRequest)

}