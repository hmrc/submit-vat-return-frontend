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

package assets.messages

object SubmitFormPageMessages {

  val title = "Submit return 12 Jan to 12 Apr 2019 - Manage your VAT account - GOV.UK"
  val titleNBS = "Submit return 12\u00a0Jan to 12\u00a0Apr\u00a02019 - Manage your VAT account - GOV.UK"
  val agentTitle = "Submit return 12 Jan to 12 Apr 2019 - Your client’s VAT details - GOV.UK"
  val agentTitleNBS = "Submit return 12\u00a0Jan to 12\u00a0Apr\u00a02019 - Your client’s VAT details - GOV.UK"
  val heading = "Submit return 12 Jan to 12 Apr 2019"
  val headingNBS = "Submit return 12\u00a0Jan to 12\u00a0Apr\u00a02019"
  val returnDue: String => String = date => "Return due date: " + date
  val vatDetails = "VAT details"
  val errorHeading = "There is a problem"
  val back: String = "Back"
  val wholePounds = "Only enter whole pounds."
  val calculatedValue = "Calculated value"
  val errorPrefix = "Error:"
  val enterNumberError = "Enter a number in"

  val box1 = "Box 1"
  val box1Text = "VAT due in the period on sales and other outputs"
  val box1TextWelsh = "TAW sy’n ddyledus yn y cyfnod ar werthiannau ac allbynnau eraill"

  val box2 = "Box 2"
  val box2Text = "VAT due in the period on acquisitions of goods made in Northern Ireland from EU Member States"
  val box2TextWelsh = "TAW sy’n ddyledus yn y cyfnod ar gaffael nwyddau a wneir yng Ngogledd Iwerddon o Aelod-wladwriaethau’r UE"

  val box3 = "Box 3"
  val box3Text = "Total VAT due (this is the total of box 1 and 2)"
  val box3TextWelsh = "Cyfanswm y TAW sy’n ddyledus (dyma gyfanswm blychau 1 a 2)"

  val box4 = "Box 4"
  val box4Text = "VAT reclaimed in the period on purchases and other inputs (including acquisitions in Northern Ireland from EU member states)"
  val box4TextWelsh = "TAW a adenillwyd yn y cyfnod ar bryniadau a mewnbynnau eraill (gan gynnwys caffaeliadau yng Ngogledd Iwerddon o Aelod-wladwriaethau’r UE)"

  val box5 = "Box 5"
  val box5Text = "Net VAT to pay to HMRC or reclaim (this is the difference between box 3 and 4)"
  val box5TextWelsh = "TAW net i dalu i CThEF neu ei hadennill (dyma’r gwahaniaeth rhwng blychau 3 a 4)"

  val additionalInformation = "Additional information"

  val box6 = "Box 6"
  val box6FlatRateSchemeText = "Total value of sales and other supplies, including VAT"
  val box6NonFlatRateSchemeText = "Total value of sales and other supplies, excluding VAT"

  val box7 = "Box 7"
  val box7Text = "Total value of purchases and all other inputs excluding any VAT"
  val box7TextWelsh = "Cyfanswm gwerth y pryniadau a’r holl fewnbynnau eraill, heb gynnwys TAW"

  val box8 = "Box 8"
  val box8Text = "Total value of dispatches of goods and related costs (excluding VAT) from Northern Ireland to EU Member States"
  val box8TextWelsh = "Cyfanswm gwerth anfon y nwyddau a’r costau cysylltiedig (ac eithrio TAW) o Ogledd Iwerddon i Aelod-wladwriaethau’r UE"

  val box9 = "Box 9"
  val box9Text = "Total value of acquisitions of goods and related costs (excluding VAT) made in Northern Ireland from EU Member States"
  val box9TextWelsh = "Cyfanswm gwerth caffael y nwyddau a’r costau cysylltiedig (ac eithrio TAW) a wneir yng Ngogledd Iwerddon o Aelod-wladwriaethau’r UE"

  val nextScreen = "You can submit your return on the next screen."

  val continue = "Continue"
}
