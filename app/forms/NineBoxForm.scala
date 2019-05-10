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

import models.NineBoxModel
import play.api.data.Form
import play.api.data.Forms._

object NineBoxForm {

  val nineBoxForm = Form(
    mapping(
      "box1" -> bigDecimal,
      "box2" -> bigDecimal,
      "box3" -> bigDecimal,
      "box4" -> bigDecimal,
      "box5" -> bigDecimal,
      "box6" -> bigDecimal,
      "box7" -> bigDecimal,
      "box8" -> bigDecimal,
      "box9" -> bigDecimal
    )(NineBoxModel.apply)(NineBoxModel.unapply)
  )

}
