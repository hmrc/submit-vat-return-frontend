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

package forms

import java.time.LocalDate

import base.BaseSpec
import forms.SubmitVatReturnForm.submitVatReturnForm
import models.SubmitVatReturnModel

class SubmitVatReturnFormSpec extends BaseSpec {

  val defaultFieldValues = Map(
    "flatRateScheme" -> "false",
    "start" -> "2019-01-01",
    "end" -> "2019-01-01",
    "due" -> "2019-01-01"
  )

  object MessageLookup {
    val tooManyCharacters: String = "Enter a maximum of 13 digits for pounds.\nEnter a maximum of 2 decimal places for pence.\nYou can use a negative amount eg -13.2"
    val tooManyCharactersNonDecimal: String = "Enter a maximum of 13 digits for pounds.\nYou can use a negative amount eg -13"
    val tooManyCharactersNonNegative: String = "Enter a maximum of 11 digits for pounds.\nEnter a maximum of 2 decimal places for pence.\n" +
      "Do not use a negative amount eg -13.2"
    val enterANumber: String = "Enter a number"
    val invalidNumber: String = "Enter a number in the correct format"
    val box3Sum: String = "Add the number from box 1 to the number from box 2 and write it here"
    val box5Sum: String = "Subtract the number in box 4 away from the number in box 3 and write it here"
  }

  "Binding a SubmitVatReturnForm" when {

    "values are valid" should {

      val form = submitVatReturnForm.bind(
        Map(
          "box1" -> "1.01",
          "box2" -> "2.02",
          "box3" -> "3.03",
          "box4" -> "4.04",
          "box5" -> "1.01",
          "box6" -> "6",
          "box7" -> "7",
          "box8" -> "8",
          "box9" -> "9"
        ) ++ defaultFieldValues
      )

      "produce a SubmitVatReturnModel" in {
        form.value shouldBe Some(SubmitVatReturnModel(
          box1 = 1.01,
          box2 = 2.02,
          box3 = 3.03,
          box4 = 4.04,
          box5 = 1.01,
          box6 = 6,
          box7 = 7,
          box8 = 8,
          box9 = 9,
          flatRateScheme = false,
          start = LocalDate.parse("2019-01-01"),
          end = LocalDate.parse("2019-01-01"),
          due = LocalDate.parse("2019-01-01")
        ))
      }
    }

    "values are invalid" when {

      "box 1" when {

        "is not a number" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "x!",
              "box2" -> "1",
              "box3" -> "1",
              "box4" -> "1",
              "box5" -> "1",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.invalidNumber}" in {
            val messageKey = form.error("box1").get.message
            messages(messageKey) shouldBe MessageLookup.invalidNumber
          }
        }

        "has more than 2 decimals" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1.001",
              "box2" -> "1",
              "box3" -> "1",
              "box4" -> "1",
              "box5" -> "1",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.tooManyCharacters}" in {
            val messageKey = form.error("box1").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharacters
          }
        }

        "is empty" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "",
              "box2" -> "1",
              "box3" -> "1",
              "box4" -> "1",
              "box5" -> "1",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.enterANumber}" in {
            val messageKey = form.error("box1").get.message
            messages(messageKey) shouldBe MessageLookup.enterANumber
          }
        }

        "is less than -9999999999999.99" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "-10000000000000",
              "box2" -> "1",
              "box3" -> "1",
              "box4" -> "1",
              "box5" -> "1",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.tooManyCharacters}" in {
            val messageKey = form.error("box1").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharacters
          }
        }

        "is more than 9999999999999.99" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "10000000000000",
              "box2" -> "1",
              "box3" -> "1",
              "box4" -> "1",
              "box5" -> "1",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.tooManyCharacters}" in {
            val messageKey = form.error("box1").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharacters
          }
        }
      }

      "box 2" when {

        "is not a number" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "x!",
              "box3" -> "1",
              "box4" -> "1",
              "box5" -> "1",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.invalidNumber}" in {
            val messageKey = form.error("box2").get.message
            messages(messageKey) shouldBe MessageLookup.invalidNumber
          }
        }

        "has more than 2 decimals" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1.001",
              "box3" -> "1",
              "box4" -> "1",
              "box5" -> "1",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.tooManyCharacters}" in {
            val messageKey = form.error("box2").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharacters
          }
        }

        "is empty" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "",
              "box3" -> "1",
              "box4" -> "1",
              "box5" -> "1",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.enterANumber}" in {
            val messageKey = form.error("box2").get.message
            messages(messageKey) shouldBe MessageLookup.enterANumber
          }
        }

        "is less than -9999999999999.99" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "-10000000000000",
              "box3" -> "1",
              "box4" -> "1",
              "box5" -> "1",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.tooManyCharacters}" in {
            val messageKey = form.error("box2").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharacters
          }
        }

        "is more than 9999999999999.99" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "10000000000000",
              "box3" -> "1",
              "box4" -> "1",
              "box5" -> "1",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.tooManyCharacters}" in {
            val messageKey = form.error("box2").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharacters
          }
        }
      }

      "box 3" when {

        "is not a number" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "x!",
              "box4" -> "1",
              "box5" -> "1",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.invalidNumber}" in {
            val messageKey = form.error("box3").get.message
            messages(messageKey) shouldBe MessageLookup.invalidNumber
          }
        }

        "has more than 2 decimals" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "1.001",
              "box4" -> "1",
              "box5" -> "1",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.tooManyCharacters}" in {
            val messageKey = form.error("box3").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharacters
          }
        }

        "is empty" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "",
              "box4" -> "1",
              "box5" -> "1",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.enterANumber}" in {
            val messageKey = form.error("box3").get.message
            messages(messageKey) shouldBe MessageLookup.enterANumber
          }
        }

        "is less than -9999999999999.99" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "-10000000000000",
              "box4" -> "1",
              "box5" -> "1",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.tooManyCharacters}" in {
            val messageKey = form.error("box3").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharacters
          }
        }

        "is more than 9999999999999.99" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "10000000000000",
              "box4" -> "1",
              "box5" -> "1",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.tooManyCharacters}" in {
            val messageKey = form.error("box3").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharacters
          }
        }
      }

      "box 4" when {

        "is not a number" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "1",
              "box4" -> "x!",
              "box5" -> "1",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.invalidNumber}" in {
            val messageKey = form.error("box4").get.message
            messages(messageKey) shouldBe MessageLookup.invalidNumber
          }
        }

        "has more than 2 decimals" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "1",
              "box4" -> "1.001",
              "box5" -> "1",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.tooManyCharacters}" in {
            val messageKey = form.error("box4").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharacters
          }
        }

        "is empty" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "1",
              "box4" -> "",
              "box5" -> "1",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.enterANumber}" in {
            val messageKey = form.error("box4").get.message
            messages(messageKey) shouldBe MessageLookup.enterANumber
          }
        }

        "is less than -9999999999999.99" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "1",
              "box4" -> "-10000000000000",
              "box5" -> "1",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.tooManyCharacters}" in {
            val messageKey = form.error("box4").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharacters
          }
        }

        "is more than 9999999999999.99" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "1",
              "box4" -> "10000000000000",
              "box5" -> "1",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.tooManyCharacters}" in {
            val messageKey = form.error("box4").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharacters
          }
        }
      }

      "box 5" when {

        "is not a number" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "1",
              "box4" -> "1",
              "box5" -> "x!",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.invalidNumber}" in {
            val messageKey = form.error("box5").get.message
            messages(messageKey) shouldBe MessageLookup.invalidNumber
          }
        }

        "has more than 2 decimals" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "1",
              "box4" -> "1",
              "box5" -> "1.001",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.tooManyCharactersNonNegative}" in {
            val messageKey = form.error("box5").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharactersNonNegative
          }
        }

        "is empty" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "1",
              "box4" -> "1",
              "box5" -> "",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.enterANumber}" in {
            val messageKey = form.error("box5").get.message
            messages(messageKey) shouldBe MessageLookup.enterANumber
          }
        }

        "is less than 0.00" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "1",
              "box4" -> "1",
              "box5" -> "-0.01",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.tooManyCharactersNonNegative}" in {
            val messageKey = form.error("box5").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharactersNonNegative
          }
        }

        "is more than 99999999999.99" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "2",
              "box4" -> "1",
              "box5" -> "100000000000.00",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.tooManyCharactersNonNegative}" in {
            val messageKey = form.error("box5").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharactersNonNegative
          }
        }

        "is negative" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "2",
              "box4" -> "1",
              "box5" -> "-5",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.tooManyCharactersNonNegative}" in {
            val messageKey = form.error("box5").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharactersNonNegative
          }
        }
      }

      "box 6" when {

        "is not a number" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "1",
              "box4" -> "1",
              "box5" -> "1",
              "box6" -> "x!",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.invalidNumber}" in {
            val messageKey = form.error("box6").get.message
            messages(messageKey) shouldBe MessageLookup.invalidNumber
          }
        }

        "has decimals other than 0.00" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "1",
              "box4" -> "1",
              "box5" -> "1",
              "box6" -> "1.01",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.tooManyCharactersNonDecimal}" in {
            val messageKey = form.error("box6").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharactersNonDecimal
          }
        }

        "is empty" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "1",
              "box4" -> "1",
              "box5" -> "1",
              "box6" -> "",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.enterANumber}" in {
            val messageKey = form.error("box6").get.message
            messages(messageKey) shouldBe MessageLookup.enterANumber
          }
        }

        "is less than -9999999999999" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "1",
              "box4" -> "1",
              "box5" -> "1",
              "box6" -> "-10000000000000",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.tooManyCharactersNonDecimal}" in {
            val messageKey = form.error("box6").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharactersNonDecimal
          }
        }

        "is more than 9999999999999" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "1",
              "box4" -> "1",
              "box5" -> "1",
              "box6" -> "10000000000000",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.tooManyCharactersNonDecimal}" in {
            val messageKey = form.error("box6").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharactersNonDecimal
          }
        }
      }

      "box 7" when {

        "is not a number" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "1",
              "box4" -> "1",
              "box5" -> "1",
              "box6" -> "1",
              "box7" -> "x!",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.invalidNumber}" in {
            val messageKey = form.error("box7").get.message
            messages(messageKey) shouldBe MessageLookup.invalidNumber
          }
        }

        "has decimals other than 0.00" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "1",
              "box4" -> "1",
              "box5" -> "1",
              "box6" -> "1",
              "box7" -> "1.01",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.tooManyCharactersNonDecimal}" in {
            val messageKey = form.error("box7").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharactersNonDecimal
          }
        }

        "is empty" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "1",
              "box4" -> "1",
              "box5" -> "1",
              "box6" -> "1",
              "box7" -> "",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.enterANumber}" in {
            val messageKey = form.error("box7").get.message
            messages(messageKey) shouldBe MessageLookup.enterANumber
          }
        }

        "is less than -9999999999999" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "1",
              "box4" -> "1",
              "box5" -> "1",
              "box6" -> "1",
              "box7" -> "-10000000000000",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.tooManyCharactersNonDecimal}" in {
            val messageKey = form.error("box7").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharactersNonDecimal
          }
        }

        "is more than 9999999999999" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "1",
              "box4" -> "1",
              "box5" -> "1",
              "box6" -> "1",
              "box7" -> "10000000000000",
              "box8" -> "1",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.tooManyCharactersNonDecimal}" in {
            val messageKey = form.error("box7").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharactersNonDecimal
          }
        }
      }

      "box 8" when {

        "is not a number" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "1",
              "box4" -> "1",
              "box5" -> "1",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "x!",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.invalidNumber}" in {
            val messageKey = form.error("box8").get.message
            messages(messageKey) shouldBe MessageLookup.invalidNumber
          }
        }

        "has decimals other than 0.00" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "1",
              "box4" -> "1",
              "box5" -> "1",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "1.01",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.tooManyCharactersNonDecimal}" in {
            val messageKey = form.error("box8").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharactersNonDecimal
          }
        }

        "is empty" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "1",
              "box4" -> "1",
              "box5" -> "1",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.enterANumber}" in {
            val messageKey = form.error("box8").get.message
            messages(messageKey) shouldBe MessageLookup.enterANumber
          }
        }

        "is less than -9999999999999" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "1",
              "box4" -> "1",
              "box5" -> "1",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "-10000000000000",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.tooManyCharactersNonDecimal}" in {
            val messageKey = form.error("box8").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharactersNonDecimal
          }
        }

        "is more than 9999999999999" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "1",
              "box4" -> "1",
              "box5" -> "1",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "10000000000000",
              "box9" -> "1"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.tooManyCharactersNonDecimal}" in {
            val messageKey = form.error("box8").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharactersNonDecimal
          }
        }
      }

      "box 9" when {

        "is not a number" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "1",
              "box4" -> "1",
              "box5" -> "1",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> "x!"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.invalidNumber}" in {
            val messageKey = form.error("box9").get.message
            messages(messageKey) shouldBe MessageLookup.invalidNumber
          }
        }

        "has decimals other than 0.00" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "1",
              "box4" -> "1",
              "box5" -> "1",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> "1.01"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.tooManyCharactersNonDecimal}" in {
            val messageKey = form.error("box9").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharactersNonDecimal
          }
        }

        "is empty" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "1",
              "box4" -> "1",
              "box5" -> "1",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> ""
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.enterANumber}" in {
            val messageKey = form.error("box9").get.message
            messages(messageKey) shouldBe MessageLookup.enterANumber
          }
        }

        "is less than -9999999999999" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "1",
              "box4" -> "1",
              "box5" -> "1",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> "-10000000000000"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.tooManyCharactersNonDecimal}" in {
            val messageKey = form.error("box9").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharactersNonDecimal
          }
        }

        "is more than 9999999999999" should {

          val form = submitVatReturnForm.bind(
            Map(
              "box1" -> "1",
              "box2" -> "1",
              "box3" -> "1",
              "box4" -> "1",
              "box5" -> "1",
              "box6" -> "1",
              "box7" -> "1",
              "box8" -> "1",
              "box9" -> "10000000000000"
            ) ++ defaultFieldValues
          )

          s"return a form field error with message ${MessageLookup.tooManyCharactersNonDecimal}" in {
            val messageKey = form.error("box9").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharactersNonDecimal
          }
        }
      }
    }
  }

  "SubmitVatReturnForm .validateBoxCalculations" when {

    "form already has errors" should {

      val form = submitVatReturnForm.bind(
        Map(
          "box1" -> "x!",
          "box2" -> "1",
          "box3" -> "1",
          "box4" -> "1",
          "box5" -> "1",
          "box6" -> "1",
          "box7" -> "1",
          "box8" -> "1",
          "box9" -> "1"
        ) ++ defaultFieldValues
      )

      "return the form with errors" in {
        form.hasErrors shouldBe true
        SubmitVatReturnForm.validateBoxCalculations(form) shouldBe form
      }
    }

    "all form fields are valid numbers in valid ranges" when {

      "box 3 and 5 are valid calculations" should {

        val form = submitVatReturnForm.bind(
          Map(
            "box1" -> "1",
            "box2" -> "2",
            "box3" -> "3",
            "box4" -> "4",
            "box5" -> "1",
            "box6" -> "1",
            "box7" -> "1",
            "box8" -> "1",
            "box9" -> "1"
          ) ++ defaultFieldValues
        )

        "return the form with no errors" in {
          SubmitVatReturnForm.validateBoxCalculations(form).hasErrors shouldBe false
        }
      }

      "box 3 is not the sum of box 1 and 2" should {

        val form = submitVatReturnForm.bind(
          Map(
            "box1" -> "1",
            "box2" -> "2",
            "box3" -> "2",
            "box4" -> "1",
            "box5" -> "1",
            "box6" -> "1",
            "box7" -> "1",
            "box8" -> "1",
            "box9" -> "1"
          ) ++ defaultFieldValues
        )

        "return the form with a field error for box 3" in {
          val messageKey = SubmitVatReturnForm.validateBoxCalculations(form).error("box3").get.message
          messages(messageKey) shouldBe MessageLookup.box3Sum
        }
      }

      "box 5 is not box 3 minus box 4" should {

        val form = submitVatReturnForm.bind(
          Map(
            "box1" -> "1",
            "box2" -> "2",
            "box3" -> "3",
            "box4" -> "4",
            "box5" -> "2",
            "box6" -> "1",
            "box7" -> "1",
            "box8" -> "1",
            "box9" -> "1"
          ) ++ defaultFieldValues
        )

        "return the form with a field error for box 5" in {
          val messageKey = SubmitVatReturnForm.validateBoxCalculations(form).error("box5").get.message
          messages(messageKey) shouldBe MessageLookup.box5Sum
        }
      }

      "multiple calculations are invalid" should {

        val form = submitVatReturnForm.bind(
          Map(
            "box1" -> "1",
            "box2" -> "1",
            "box3" -> "1",
            "box4" -> "1",
            "box5" -> "1",
            "box6" -> "1",
            "box7" -> "1",
            "box8" -> "1",
            "box9" -> "1"
          ) ++ defaultFieldValues
        )

        val validatedForm = SubmitVatReturnForm.validateBoxCalculations(form)

        "return both field errors" in {
          messages(validatedForm.error("box5").get.message) shouldBe MessageLookup.box5Sum
          messages(validatedForm.error("box3").get.message) shouldBe MessageLookup.box3Sum
        }
      }
    }
  }
}
