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

package models

import play.api.libs.json.{Json, OFormat}

case class CalculatedNineBoxModel(box1: BigDecimal,
                                  box2: BigDecimal,
                                  box3: BigDecimal,
                                  box4: BigDecimal,
                                  box5: BigDecimal,
                                  box6: BigDecimal,
                                  box7: BigDecimal,
                                  box8: BigDecimal,
                                  box9: BigDecimal)

object CalculatedNineBoxModel {
  implicit val formats: OFormat[CalculatedNineBoxModel] = Json.format[CalculatedNineBoxModel]

  def fromNineBox(model: NineBoxModel): CalculatedNineBoxModel = {
    val box3: BigDecimal = model.box1 + model.box2
    val box5: BigDecimal = (box3 - model.box4).abs

    CalculatedNineBoxModel(
      model.box1,
      model.box2,
      box3,
      model.box4,
      box5,
      model.box6,
      model.box7,
      model.box8,
      model.box9
    )
  }
}
