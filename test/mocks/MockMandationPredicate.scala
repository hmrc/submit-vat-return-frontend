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

package mocks

import base.BaseSpec
import controllers.predicates.MandationStatusPredicate
import mocks.service.MockMandationStatusService
import views.html.errors.MtdMandatedUser

trait MockMandationPredicate extends BaseSpec with MockMandationStatusService {

  val mtdMandatedUser: MtdMandatedUser = inject[MtdMandatedUser]

  val mockMandationStatusPredicate: MandationStatusPredicate =
    new MandationStatusPredicate(
      mockMandationStatusService,
      errorHandler,
      messagesApi,
      mtdMandatedUser,
      mockAppConfig,
      ec
    )

}
