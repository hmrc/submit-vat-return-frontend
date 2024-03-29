/*
 * Copyright 2023 HM Revenue & Customs
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
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}
import uk.gov.hmrc.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

trait MockHttp extends AnyWordSpecLike with Matchers with OptionValues with MockFactory {

  val mockHttp: HttpClient = mock[HttpClient]

  def setupMockHttpGet[T](response: T): Unit =
    (mockHttp.GET[T](_: String, _: Seq[(String, String)], _: Seq[(String, String)])
                    (_: HttpReads[T], _: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *, *, *)
      .returns(Future.successful(response))
}
