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
import models.{NineBoxModel, SubmitVatReturnModel}

class SubmitVatReturnFormSpec extends BaseSpec {

  object MessageLookup {
    val tooManyCharacters: String = "Enter a maximum of 13 digits for pounds.\nEnter a maximum of 2 decimal places for pence.\nYou can use a negative amount eg -13.2"
    val tooManyCharactersNonDecimal: String = "Enter a maximum of 13 digits for pounds.\nYou can use a negative amount eg -13"
    val tooManyCharactersNonNegative: String = "Enter a maximum of 11 digits for pounds.\nEnter a maximum of 2 decimal places for pence.\n" +
      "Do not use a negative amount eg -13.2"
    def enterANumber(boxId : Int): String = s"Enter a number in box $boxId"
    def invalidNumber(boxId : Int): String = s"Enter a number in the correct format in box $boxId"
    val box3Sum: String = "Add the number from box 1 to the number from box 2 and write it here"
    val box5Sum: String = "Subtract the number in box 4 away from the number in box 3 and write it here"
  }

  val form =  SubmitVatReturnForm()

  "Binding a NineBoxForm" when {

    "values are valid" should {

      val formWithValues = form.nineBoxForm.bind(
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
        )
      )

      "produce a NineBoxModel" in {
        formWithValues.value shouldBe Some(NineBoxModel(
          box1 = 1.01,
          box2 = 2.02,
          box3 = 3.03,
          box4 = 4.04,
          box5 = 1.01,
          box6 = 6,
          box7 = 7,
          box8 = 8,
          box9 = 9
        ))
      }
    }

    "values are invalid" when {

      "box 1" when {

        "is not a number" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.invalidNumber(1)}" in {
            val messageKey = formWithValues.error("box1").get.message
            messages(messageKey) shouldBe MessageLookup.invalidNumber(1)
          }
        }

        "has more than 2 decimals" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.tooManyCharacters}" in {
            val messageKey = formWithValues.error("box1").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharacters
          }
        }

        "is empty" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.enterANumber(1)}" in {
            val messageKey = formWithValues.error("box1").get.message
            messages(messageKey) shouldBe MessageLookup.enterANumber(1)
          }
        }

        "is less than -9999999999999.99" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.tooManyCharacters}" in {
            val messageKey = formWithValues.error("box1").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharacters
          }
        }

        "is more than 9999999999999.99" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.tooManyCharacters}" in {
            val messageKey = formWithValues.error("box1").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharacters
          }
        }
      }

      "box 2" when {

        "is not a number" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.invalidNumber(2)}" in {
            val messageKey = formWithValues.error("box2").get.message
            messages(messageKey) shouldBe MessageLookup.invalidNumber(2)
          }
        }

        "has more than 2 decimals" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.tooManyCharacters}" in {
            val messageKey = formWithValues.error("box2").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharacters
          }
        }

        "is empty" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.enterANumber(2)}" in {
            val messageKey = formWithValues.error("box2").get.message
            messages(messageKey) shouldBe MessageLookup.enterANumber(2)
          }
        }

        "is less than -9999999999999.99" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.tooManyCharacters}" in {
            val messageKey = formWithValues.error("box2").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharacters
          }
        }

        "is more than 9999999999999.99" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.tooManyCharacters}" in {
            val messageKey = formWithValues.error("box2").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharacters
          }
        }
      }

      "box 3" when {

        "is not a number" should {

          val formWithValues =form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.invalidNumber(3)}" in {
            val messageKey = formWithValues.error("box3").get.message
            messages(messageKey) shouldBe MessageLookup.invalidNumber(3)
          }
        }

        "has more than 2 decimals" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.tooManyCharacters}" in {
            val messageKey = formWithValues.error("box3").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharacters
          }
        }

        "is empty" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.enterANumber(3)}" in {
            val messageKey = formWithValues.error("box3").get.message
            messages(messageKey) shouldBe MessageLookup.enterANumber(3)
          }
        }

        "is less than -9999999999999.99" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.tooManyCharacters}" in {
            val messageKey = formWithValues.error("box3").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharacters
          }
        }

        "is more than 9999999999999.99" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.tooManyCharacters}" in {
            val messageKey = formWithValues.error("box3").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharacters
          }
        }
      }

      "box 4" when {

        "is not a number" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.invalidNumber(4)}" in {
            val messageKey = formWithValues.error("box4").get.message
            messages(messageKey) shouldBe MessageLookup.invalidNumber(4)
          }
        }

        "has more than 2 decimals" should {

          val formWithValues =form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.tooManyCharacters}" in {
            val messageKey = formWithValues.error("box4").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharacters
          }
        }

        "is empty" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.enterANumber(4)}" in {
            val messageKey = formWithValues.error("box4").get.message
            messages(messageKey) shouldBe MessageLookup.enterANumber(4)
          }
        }

        "is less than -9999999999999.99" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.tooManyCharacters}" in {
            val messageKey = formWithValues.error("box4").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharacters
          }
        }

        "is more than 9999999999999.99" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.tooManyCharacters}" in {
            val messageKey = formWithValues.error("box4").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharacters
          }
        }
      }

      "box 5" when {

        "is not a number" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.invalidNumber(5)}" in {
            val messageKey = formWithValues.error("box5").get.message
            messages(messageKey) shouldBe MessageLookup.invalidNumber(5)
          }
        }

        "has more than 2 decimals" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.tooManyCharactersNonNegative}" in {
            val messageKey = formWithValues.error("box5").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharactersNonNegative
          }
        }

        "is empty" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.enterANumber(5)}" in {
            val messageKey = formWithValues.error("box5").get.message
            messages(messageKey) shouldBe MessageLookup.enterANumber(5)
          }
        }

        "is less than 0.00" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.tooManyCharactersNonNegative}" in {
            val messageKey = formWithValues.error("box5").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharactersNonNegative
          }
        }

        "is more than 99999999999.99" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.tooManyCharactersNonNegative}" in {
            val messageKey = formWithValues.error("box5").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharactersNonNegative
          }
        }

        "is negative" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.tooManyCharactersNonNegative}" in {
            val messageKey = formWithValues.error("box5").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharactersNonNegative
          }
        }
      }

      "box 6" when {

        "is not a number" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.invalidNumber(6)}" in {
            val messageKey = formWithValues.error("box6").get.message
            messages(messageKey) shouldBe MessageLookup.invalidNumber(6)
          }
        }

        "has decimals other than 0.00" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.tooManyCharactersNonDecimal}" in {
            val messageKey = formWithValues.error("box6").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharactersNonDecimal
          }
        }

        "is empty" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.enterANumber(6)}" in {
            val messageKey = formWithValues.error("box6").get.message
            messages(messageKey) shouldBe MessageLookup.enterANumber(6)
          }
        }

        "is less than -9999999999999" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.tooManyCharactersNonDecimal}" in {
            val messageKey = formWithValues.error("box6").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharactersNonDecimal
          }
        }

        "is more than 9999999999999" should {

          val formWithValues =form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.tooManyCharactersNonDecimal}" in {
            val messageKey = formWithValues.error("box6").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharactersNonDecimal
          }
        }
      }

      "box 7" when {

        "is not a number" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.invalidNumber(7)}" in {
            val messageKey = formWithValues.error("box7").get.message
            messages(messageKey) shouldBe MessageLookup.invalidNumber(7)
          }
        }

        "has decimals other than 0.00" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.tooManyCharactersNonDecimal}" in {
            val messageKey = formWithValues.error("box7").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharactersNonDecimal
          }
        }

        "is empty" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.enterANumber(7)}" in {
            val messageKey = formWithValues.error("box7").get.message
            messages(messageKey) shouldBe MessageLookup.enterANumber(7)
          }
        }

        "is less than -9999999999999" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.tooManyCharactersNonDecimal}" in {
            val messageKey = formWithValues.error("box7").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharactersNonDecimal
          }
        }

        "is more than 9999999999999" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.tooManyCharactersNonDecimal}" in {
            val messageKey = formWithValues.error("box7").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharactersNonDecimal
          }
        }
      }

      "box 8" when {

        "is not a number" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.invalidNumber(8)}" in {
            val messageKey = formWithValues.error("box8").get.message
            messages(messageKey) shouldBe MessageLookup.invalidNumber(8)
          }
        }

        "has decimals other than 0.00" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.tooManyCharactersNonDecimal}" in {
            val messageKey = formWithValues.error("box8").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharactersNonDecimal
          }
        }

        "is empty" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.enterANumber(8)}" in {
            val messageKey = formWithValues.error("box8").get.message
            messages(messageKey) shouldBe MessageLookup.enterANumber(8)
          }
        }

        "is less than -9999999999999" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.tooManyCharactersNonDecimal}" in {
            val messageKey = formWithValues.error("box8").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharactersNonDecimal
          }
        }

        "is more than 9999999999999" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.tooManyCharactersNonDecimal}" in {
            val messageKey = formWithValues.error("box8").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharactersNonDecimal
          }
        }
      }

      "box 9" when {

        "is not a number" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.invalidNumber(9)}" in {
            val messageKey = formWithValues.error("box9").get.message
            messages(messageKey) shouldBe MessageLookup.invalidNumber(9)
          }
        }

        "has decimals other than 0.00" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.tooManyCharactersNonDecimal}" in {
            val messageKey = formWithValues.error("box9").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharactersNonDecimal
          }
        }

        "is empty" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.enterANumber(9)}" in {
            val messageKey = formWithValues.error("box9").get.message
            messages(messageKey) shouldBe MessageLookup.enterANumber(9)
          }
        }

        "is less than -9999999999999" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.tooManyCharactersNonDecimal}" in {
            val messageKey = formWithValues.error("box9").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharactersNonDecimal
          }
        }

        "is more than 9999999999999" should {

          val formWithValues = form.nineBoxForm.bind(
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
            )
          )

          s"return a form field error with message ${MessageLookup.tooManyCharactersNonDecimal}" in {
            val messageKey = formWithValues.error("box9").get.message
            messages(messageKey) shouldBe MessageLookup.tooManyCharactersNonDecimal
          }
        }
      }
    }
  }

  "SubmitVatReturnForm .validateBoxCalculations" when {

    "form already has errors" should {

      val formWithValues = form.nineBoxForm.bind(
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
        )
      )

      "return the form with errors" in {
        formWithValues.hasErrors shouldBe true
        SubmitVatReturnForm().validateBoxCalculations(formWithValues) shouldBe formWithValues
      }
    }

    "all form fields are valid numbers in valid ranges" when {

      "box 3 and 5 are valid calculations" should {

        val formWithValues = form.nineBoxForm.bind(
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
          )
        )

        "return the form with no errors" in {
          SubmitVatReturnForm().validateBoxCalculations(formWithValues).hasErrors shouldBe false
        }
      }

      "box 3 is not the sum of box 1 and 2" should {

        val formWithValues = form.nineBoxForm.bind(
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
          )
        )

        "return the form with a field error for box 3" in {
          val messageKey = SubmitVatReturnForm().validateBoxCalculations(formWithValues).error("box3").get.message
          messages(messageKey) shouldBe MessageLookup.box3Sum
        }
      }

      "box 5 is not box 3 minus box 4" should {

        val formWithValues = form.nineBoxForm.bind(
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
          )
        )

        "return the form with a field error for box 5" in {
          val messageKey = SubmitVatReturnForm().validateBoxCalculations(formWithValues).error("box5").get.message
          messages(messageKey) shouldBe MessageLookup.box5Sum
        }
      }

      "multiple calculations are invalid" should {

        val formWithValues = form.nineBoxForm.bind(
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
          )
        )

        val validatedForm = SubmitVatReturnForm().validateBoxCalculations(formWithValues)

        "return both field errors" in {
          messages(validatedForm.error("box5").get.message) shouldBe MessageLookup.box5Sum
          messages(validatedForm.error("box3").get.message) shouldBe MessageLookup.box3Sum
        }
      }
    }
  }
}
