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

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import auth.AuthKeys
import config.{AppConfig, ErrorHandler}
import mocks.MockConfig
import models.auth.User
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.guice._
import play.api.Configuration
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.inject.Injector
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext

trait BaseSpec extends WordSpec with Matchers with GuiceOneAppPerSuite with MockFactory with UnitSpec {

  lazy val injector: Injector = app.injector

  implicit val config: Configuration = app.configuration

  implicit val mockAppConfig: AppConfig = new MockConfig

  implicit lazy val messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

  lazy val errorHandler: ErrorHandler = injector.instanceOf[ErrorHandler]

  implicit lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")
  lazy val fakeRequestWithClientsVRN: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest().withSession(AuthKeys.agentSessionVrn -> vrn)

  val vrn: String = "999999999"
  val arn = "ABCD12345678901"

  lazy val user: User[AnyContentAsEmpty.type] = User[AnyContentAsEmpty.type](vrn)(fakeRequest)
  lazy val agentUser: User[AnyContentAsEmpty.type] = User[AnyContentAsEmpty.type](vrn, Some(arn))(fakeRequestWithClientsVRN)

  implicit lazy val messages: Messages = messagesApi.preferred(fakeRequest)

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = ActorMaterializer()
  implicit lazy val hc: HeaderCarrier = HeaderCarrier()
  implicit lazy val ec: ExecutionContext = injector.instanceOf[ExecutionContext]

}
