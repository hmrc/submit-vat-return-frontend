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

import java.time.LocalDate

import models.{VatObligation, VatObligations}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class SubmitFormViewSpec extends ViewBaseSpec {

  "Rendering the submit_form page" should {

    val obligations: VatObligations = VatObligations(Seq(VatObligation(LocalDate.parse("2019-01-12"), LocalDate.parse("2019-04-12"), LocalDate.parse("2019-05-12"), "18AA")))

    lazy val view = views.html.submit_form("18AA", Some("ABC Studios"), flatRateScheme = true, obligations)
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

    "display the start date passed into the view" in {
      elementText("#content p:nth-of-type(4)") shouldBe "Start date: 2019-01-12"
    }

    "display the end date passed into the view" in {
      elementText("#content p:nth-of-type(5)") shouldBe "End date: 2019-04-12"
    }

    "display the due date passed into the view" in {
      elementText("#content p:nth-of-type(6)") shouldBe "Due date: 2019-05-12"
    }
  }
}
