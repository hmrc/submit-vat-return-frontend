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

package common

object SessionKeys {

  val mandationStatus = "mtdVatMandationStatus"
  val returnData = "mtdNineBoxReturnData"
  val viewModel = "mtdVatvcReturnInformation"
  val submissionYear = "mtdVatvcSubmissionYear"
  val inSessionPeriodKey = "mtdVatvcInSessionPeriodKey"
  val submittedReturn: String = "mtdVatvcSubmittedReturn"
  val insolventWithoutAccessKey: String = "insolventWithoutAccess"
  val futureInsolvencyBlock: String = "futureInsolvencyBlock"

  object HonestyDeclaration {
    val key = "mtdVatHonestyDeclaration"
    def format(vrn: String, periodKey: String): String = s"$vrn-$periodKey"
  }

}
