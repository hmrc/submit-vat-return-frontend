/*
 * Copyright 2022 HM Revenue & Customs
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

object MtdMandationMessages extends BaseMessages {

  val title: String = "The business submits VAT Returns using Making Tax Digital" + titleSuffix
  val heading = "The business submits VAT Returns using Making Tax Digital"
  val nonAgentParagraph: String = "The business has signed up to the Making Tax Digital service." +
    " You must submit your VAT Returns to HMRC using compatible accounting software."
  val agentParagraph: String = "The business has signed up to the Making Tax Digital service." +
    " You must submit your client’s VAT Returns to HMRC using compatible accounting software."
  val nonAgentLinkText = "View your VAT Return deadlines"
  val agentLinkText = "View your client options"
}
