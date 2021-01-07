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

package mocks

import org.scalamock.scalatest.MockFactory
import play.api.libs.json.Writes
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.http.HttpClient

import scala.concurrent.ExecutionContext

trait MockHttp extends UnitSpec with MockFactory {

  val mockHttp: HttpClient = mock[HttpClient]

  def setupMockHttpGet[T](url: String)(response: T): Unit =
    (mockHttp.GET[T](_: String)(_: HttpReads[T], _: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returns(response)


  def setupMockHttpPost[I,O](url: String)(response: O): Unit =
    (mockHttp.POST[I,O](_: String, _: I, _: Seq[(String, String)])(_: Writes[I], _: HttpReads[O], _: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *, *, *, *)
      .returns(response)

  def setupMockHttpPut[I,O](url: String)(response: O): Unit =
    (mockHttp.PUT[I,O](_: String, _: I)(_: Writes[I], _: HttpReads[O], _: HeaderCarrier, _: ExecutionContext)).expects(*, *, *, *, *, *)
      .returns(response)

}
