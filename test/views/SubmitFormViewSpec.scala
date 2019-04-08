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

package views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class SubmitFormViewSpec extends ViewBaseSpec {

  "Rendering the submit_form page" should {

    lazy val view = views.html.submit_form("18AA", Some("ABC Studios"), true)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct title" in {
      elementText("h1") shouldBe "Submit return"
    }

    "display the period key" in {
      elementText("#content p") shouldBe "Period key: 18AA"
    }

    "display the client name passed into the view" in {
      elementText("#content p:nth-of-type(2)") shouldBe "Client name: ABC Studios"
    }

    "display the flat rate scheme passed into the view" in {
      elementText("#content p:nth-of-type(3)") shouldBe "FlatRateScheme: true"
    }
  }
}
