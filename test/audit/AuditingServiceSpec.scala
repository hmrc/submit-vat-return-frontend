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

package audit

import audit.models.TestExtendedAuditModel
import base.BaseSpec
import config.FrontendAppConfig
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.{mock => mockitoMock}
import uk.gov.hmrc.http.HeaderCarrier
import play.api.http.HeaderNames
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Success

import scala.concurrent.{ExecutionContext, Future}


class AuditingServiceSpec extends MockitoSugar with BaseSpec {

  val mockAuditConnector: AuditConnector = mockitoMock[AuditConnector]
  val mockConfiguration: FrontendAppConfig = mockitoMock[FrontendAppConfig]

  val testAuditingService = new AuditService(mockConfiguration, mockAuditConnector)

  "AuditService" should {

    "when calling the referer method" should {

      "extract the referer if there is one" in {
        val testPath = "/test/path"
        testAuditingService.referrer(HeaderCarrier().withExtraHeaders(HeaderNames.REFERER -> testPath)) shouldBe testPath
      }

      "default to hyphen '-' if there is no referrer" in {
        testAuditingService.referrer(HeaderCarrier()) shouldBe "-"
      }
    }

    "given an ExtendedAuditModel" should {

      "extract the data and pass it into the AuditConnector" in {

        val testModel = new TestExtendedAuditModel("foo", "bar")
        val testPath = "/test/path"
        val expectedData = testAuditingService.toExtendedDataEvent(mockConfiguration.appName, testModel, testPath)

        when(mockAuditConnector.sendExtendedEvent(
          ArgumentMatchers.refEq(expectedData, "eventId", "generatedAt")
        )(
          ArgumentMatchers.any[HeaderCarrier],
          ArgumentMatchers.any[ExecutionContext]
        )) thenReturn Future.successful(Success)

        testAuditingService.audit(testModel, Some(testPath))(hc,ec)

        verify(mockAuditConnector)
          .sendExtendedEvent(
            ArgumentMatchers.refEq(expectedData, "eventId", "generatedAt")
          )(
            ArgumentMatchers.any[HeaderCarrier],
            ArgumentMatchers.any[ExecutionContext]
          )
      }
    }
  }


}
