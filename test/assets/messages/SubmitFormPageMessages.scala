/*
 * Copyright 2020 HM Revenue & Customs
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

  val submitReturn = "Submit return"
  val returnDue: String => String = date => "Return due date: " + date
  val vatDetails = "VAT details"
  val errorHeading = "There is a problem"
  val back: String = "Back"

  val box1 = "Box 1"
  val box1Text = "VAT you charged on sales and other supplies"

  val box2 = "Box 2"
  val box2Text = "VAT you owe on goods purchased from EC countries and brought into the UK"
  val box2TextNIProtocol = "VAT due in this period on intra-community acquisitions of goods made in Northern Ireland from EU Member States"

  val box3 = "Box 3"
  val box3Text = "VAT you owe before deductions (this is the total of box 1 and 2)"

  val box4 = "Box 4"
  val box4Text = "VAT you have claimed back"

  val box5 = "Box 5"
  val box5Text = "Net VAT you owe HMRC or HMRC owes you (this is the difference between box 3 and 4)"

  val additionalInformation = "Additional information"

  val box6 = "Box 6"
  val box6FlatRateSchemeText = "Total value of sales and other supplies, including VAT"
  val box6NonFlatRateSchemeText = "Total value of sales and other supplies, excluding VAT"

  val box7 = "Box 7"
  val box7Text = "Total value of purchases and other expenses, excluding VAT"

  val box8 = "Box 8"
  val box8Text = "Total value of supplied goods to EC countries and related costs (excluding VAT)"
  val box8TextNIProtocol = "Total value of intra-community dispatches of goods and related costs (excluding VAT) from Northern Ireland to EU Member States"

  val box9 = "Box 9"
  val box9Text = "Total value of goods purchased from EC countries and brought into the UK, as well as any related costs (excluding VAT)"
  val box9TextNIProtocol = "Total value of intra-community acquisitions of goods and related costs (excluding VAT) from Northern Ireland to EU Member States"

  val nextScreen = "You can submit your return on the next screen."

  val continue = "Continue"
}
