package com.shoppingbasket.api

import java.io.IOException

import akka.http.scaladsl.model.StatusCodes.{Created, OK}
import akka.http.scaladsl.model.{ContentTypes, HttpResponse}
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.Materializer
import spray.json._

import scala.concurrent.{ExecutionContext, Future}

trait ShoppingMarshalling extends DefaultJsonProtocol {

  import ShoppingBasket._

  implicit val addItemFormat = jsonFormat2(AddItemRequest)
  implicit val productFormat = jsonFormat4(ProductCatalog.Product)
  implicit val itemFormat = jsonFormat3(BasketItem)
  implicit val itemsFormat = jsonFormat1(AllItems)
  implicit val itemAddedFormat = jsonFormat1(ItemAdded)
  implicit val itemUpdatedFormat = jsonFormat1(ItemUpdated)
  implicit val itemDeletedFormat = jsonFormat1(ItemDeleted)

  implicit def allItemsUnmarshaller(implicit ec: ExecutionContext, mat: Materializer) = Unmarshaller[HttpResponse, AllItems] {
    _ => httpResponse => converter(httpResponse, parseAllItemsJson)
  }

  implicit def basketItemUnmarshaller(implicit ec: ExecutionContext, mat: Materializer) = Unmarshaller[HttpResponse, ItemDeleted] {
    _ => httpResponse => converter(httpResponse, parseItemDeletedJson)
  }

  implicit def itemAddedUnmarshaller(implicit ec: ExecutionContext, mat: Materializer) = Unmarshaller[HttpResponse, ItemAdded] {
    _ => httpResponse => converter(httpResponse, parseItemAddedJson)
  }

  implicit def itemUpdatedUnmarshaller(implicit ec: ExecutionContext, mat: Materializer) = Unmarshaller[HttpResponse, ItemUpdated] {
    _ => httpResponse => converter(httpResponse, parseItemUpdatedJson)
  }

  def parseAllItemsJson(str: String): AllItems = {
    str.parseJson.convertTo[AllItems]
  }

  def parseItemDeletedJson(str: String): ItemDeleted = {
    str.parseJson.convertTo[ItemDeleted]
  }

  def parseItemAddedJson(str: String): ItemAdded = {
    str.parseJson.convertTo[ItemAdded]
  }

  def parseItemUpdatedJson(str: String): ItemUpdated = {
    str.parseJson.convertTo[ItemUpdated]
  }

  def converter[T](httpResponse: HttpResponse, parser: String => T)(implicit ec: ExecutionContext, mat: Materializer): Future[T] = {
    httpResponse.status match {
      case OK | Created if httpResponse.entity.contentType == ContentTypes.`application/json` =>
        Unmarshal(httpResponse.entity).to[String].map { jsonString =>
          parser(jsonString)
        }
      case _ => Unmarshal(httpResponse.entity).to[String].flatMap { entity =>
        val error = s"Request failed with status code ${httpResponse.status} and entity $entity"
        Future.failed(new IOException(error))
      }
    }
  }
}
