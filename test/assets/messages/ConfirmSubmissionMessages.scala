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

package assets.messages

object ConfirmSubmissionMessages extends BaseMessages {

  val title: String = "Confirm your VAT Return" + titleSuffix
  val back: String = "Back"
  val agentTitle: String = "Confirm your VAT Return" + agentTitleSuffix
  val heading = "Submit return"
  val subHeadingVatDetails = "VAT details"
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
  val warningNonAgentDeclarationText = "Warning " + nonAgentDeclarationText

  object WarningMessages {
    val headerMultipleWarning = "The amounts you entered in boxes 1 and 4 are higher than what we would expect, based on the amounts in boxes 6 and 7."
    val listItemMultipleWarning = "have understated the amount in boxes 6 and 7"

    def headerSingleWarning: (Int, Int) => String = (number1: Int, number2: Int) => s"The amount you entered in Box $number1 " +
      s"is higher than what we would expect, based on the amount in Box $number2."
    def listItemSingleWarning: Int => String = boxNumber => s"have understated the amount in Box $boxNumber"

    val listHeading = "These might be correct if you:"
    val listCommonItem = "are adjusting a previous return"
    val listCommonBottomText = "Check the figures you entered. You can change the amounts in the VAT Return if you need to."
  }

}
