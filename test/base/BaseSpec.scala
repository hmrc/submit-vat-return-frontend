/*
 * Copyright 2022 HM Revenue & Customs
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

import auth.AuthKeys
import common.SessionKeys
import config.{AppConfig, ErrorHandler}
import mocks.MockConfig
import models.auth.User
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.play.guice._
import play.api.Configuration
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents}
import play.api.test.{FakeRequest, Injecting}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext

trait BaseSpec extends AnyWordSpecLike with Matchers with OptionValues with GuiceOneAppPerSuite with MockFactory
  with BeforeAndAfterEach with Injecting {

  implicit val config: Configuration = app.configuration

  implicit val mockAppConfig: AppConfig = new MockConfig

  lazy val mcc: MessagesControllerComponents = inject[MessagesControllerComponents]
  lazy val messagesApi: MessagesApi = inject[MessagesApi]
  implicit lazy val messages: Messages = messagesApi.preferred(Seq(Lang("en")))
  lazy val welshMessages: Messages = messagesApi.preferred(Seq(Lang("cy")))

  lazy val errorHandler: ErrorHandler = inject[ErrorHandler]

  implicit lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")
    .withSession(SessionKeys.insolventWithoutAccessKey -> "false", SessionKeys.futureInsolvencyBlock -> "false")
  lazy val fakeRequestPost: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("POST", "/")
    .withSession(SessionKeys.insolventWithoutAccessKey -> "false", SessionKeys.futureInsolvencyBlock -> "false")
  lazy val fakeRequestWithClientsVRN: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest().withSession(AuthKeys.agentSessionVrn -> vrn)
  lazy val insolventRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest().withSession(SessionKeys.insolventWithoutAccessKey -> "true")
  lazy val futureInsolvencyRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest().withSession(SessionKeys.insolventWithoutAccessKey -> "false", SessionKeys.futureInsolvencyBlock -> "true")

  val vrn: String = "999999999"
  val arn = "ABCD12345678901"

  lazy val user: User[AnyContentAsEmpty.type] = User[AnyContentAsEmpty.type](vrn)(fakeRequest)
  lazy val agentUser: User[AnyContentAsEmpty.type] = User[AnyContentAsEmpty.type](vrn, Some(arn))(fakeRequestWithClientsVRN)


  implicit lazy val hc: HeaderCarrier = HeaderCarrier()
  implicit lazy val ec: ExecutionContext = inject[ExecutionContext]
}
