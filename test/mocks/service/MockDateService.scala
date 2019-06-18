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

package mocks.service

import java.time.LocalDate
import org.scalamock.scalatest.MockFactory
import services.DateService
import uk.gov.hmrc.play.test.UnitSpec

trait MockDateService extends UnitSpec with MockFactory {

  val mockDateService: DateService = mock[DateService]

  def mockCurrentDate(currentDate: LocalDate): Unit = {
    (mockDateService.now: () => LocalDate)
      .stubs()
      .returns(currentDate)
  }

  def mockDateHasPassed(response: Boolean): Unit = {
    (mockDateService.dateHasPassed(_: LocalDate))
      .expects(*)
      .returns(response)
  }
}