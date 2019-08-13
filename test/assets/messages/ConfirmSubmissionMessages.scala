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

package assets.messages

object ConfirmSubmissionMessages extends BaseMessages {

  val title = "Your VAT return" + titleSuffix
  val heading = "Submit return"
  val subHeadingVatDetails = "VAT details"
  val box1Heading = "Box 1"
  val box1Description = "VAT you charged on sales and other supplies"
  val box2Heading = "Box 2"
  val box2Description = "VAT you owe on goods purchased from EC countries and brought into the UK"
  val box3Heading = "Box 3"
  val box3Description = "VAT you owe before deductions (this is the total of box 1 and 2)"
  val box4Heading = "Box 4"
  val box4Description = "VAT you have claimed back"
  val box5Heading = "Box 5"
  val box5Description = "Return total"
  val box6Heading = "Box 6"
  val box6DescriptionHasFRS = "Total value of sales and other supplies, including VAT"
  val box6DescriptionNoFRS = "Total value of sales and other supplies, excluding VAT"
  val box7Heading = "Box 7"
  val box7Description = "Total value of purchases and other expenses, excluding VAT"
  val box8Heading = "Box 8"
  val box8Description = "Total value of supplied goods to EC countries and related costs (excluding VAT)"
  val box9Heading = "Box 9"
  val box9Description = "Total value of goods purchased from EC countries and brought into the UK, as well as any related costs (excluding VAT)"
  val returnTotal = "Return total:"
  val returnDueDate = "Return due date:"
  val changeReturnLink = "Change return details"
  val submitButton = "Accept and send"
  val declarationHeading = "Declaration"
  val agentDeclarationText: String = "I confirm that my client has received a copy of the information contained in this return and approved the" +
    " information as being correct and complete to the best of their knowledge and belief."
  val nonAgentDeclarationText: String = "By submitting this return, you are making a legal declaration that the information is correct and" +
    " complete to the best of your knowledge and belief. A false declaration can result in prosecution."
  val warning = "Warning"
}
