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

package testOnly.controllers

import javax.inject.Inject
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import testOnly.connectors.DynamicStubConnector
import testOnly.models.{DataModel, SchemaModel}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

class DynamicStubController @Inject()(dynamicStubConnector: DynamicStubConnector,
                                      mcc: MessagesControllerComponents,
                                      implicit val ec: ExecutionContext) extends FrontendController(mcc) {

  def populateStub: Action[JsValue] = Action.async(parse.tolerantJson) { implicit request =>
    Try(request.body.as[DataModel]) match {
      case Success(dataModel) =>
        dynamicStubConnector.populateStub(dataModel).map { response =>
          response.status match {
            case OK => Ok(s"Successfully populated vat-returns-dynamic-stub with $dataModel")
            case _ => InternalServerError(s"Unable to populate vat-returns-dynamic-stub with $dataModel")
          }
        }
      case _ => Future.successful(BadRequest("Unable to convert json to model"))
    }
  }

  def populateSchema: Action[JsValue] = Action.async(parse.tolerantJson) { implicit request =>
    Try(request.body.as[SchemaModel]) match {
      case Success(schemaModel) =>
        dynamicStubConnector.populateSchema(schemaModel).map { response =>
          response.status match {
            case OK => Ok(s"Successfully populated vat-returns-dynamic-stub schema with $schemaModel")
            case _ => InternalServerError(s"Unable to populate vat-returns-dynamic-stub schema with $schemaModel")
          }
        }
      case _ => Future.successful(BadRequest("Unable to convert json to model"))
    }
  }

  def clearSchema(schemaId: String): Action[AnyContent] = Action.async { implicit request =>
    dynamicStubConnector.clearSchema(schemaId).map(
      response => response.status match {
        case OK => Ok(s"Successfully cleared vat-returns-dynamic-stub schema: $schemaId")
        case _ => InternalServerError(s"Unable to clear vat-returns-dynamic-stub schema: $schemaId. ${response.body}")
      }
    )
  }

  def clearDataForSchema(schemaId: String): Action[AnyContent] = Action.async { implicit request =>
    dynamicStubConnector.clearDataForSchema(schemaId).map(
      response => response.status match {
        case OK => Ok(s"Successfully cleared vat-returns-dynamic-stub data for schema: $schemaId")
        case _ => InternalServerError(s"Unable to clear vat-returns-dynamic-stub data for schema: $schemaId. ${response.body}")
      }
    )
  }
}
