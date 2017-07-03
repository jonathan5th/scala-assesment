package com.shoppingbasket

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.shoppingbasket.api.ShoppingBasket._
import com.shoppingbasket.api.{AddItemRequest, ShoppingAPI, ShoppingMarshalling, ShoppingRoutes}
import com.shoppingbasket.util.SampleProducts
import net.liftweb.json.{DefaultFormats, Serialization}
import org.scalatest.{Matchers, WordSpecLike}

class ShoppingAPISpec extends WordSpecLike
  with ScalatestRouteTest
  with Matchers
  with ShoppingMarshalling {

  private val routes = new ShoppingAPI(system).routes
  implicit val formats = DefaultFormats
  private val addItemRequest = AddItemRequest(SampleProducts.pencil.productId, 1)
  private val httpEntity = HttpEntity(ContentTypes.`application/json`, Serialization.write(addItemRequest))
  private val onePencil = BasketItem("0", SampleProducts.pencil, 1)

  "The Shopping API" when {
    "receives a 'Add Item' request" should {
      "add it to Basket and respond with the added item" in {
        Post("/api/shoppingbasket/items", httpEntity) ~> routes ~> check {
          status shouldEqual Created
          responseAs[ItemAdded] shouldEqual ItemAdded(onePencil)
        }
        Get("/api/shoppingbasket/items") ~> routes ~> check {
          status shouldEqual OK
          responseAs[AllItems] shouldEqual AllItems(List(onePencil))
        }
      }
    }
    "receives a 'Get All Items' request" should {
      "respond with the current list of items in the Basket" in {
        Get("/api/shoppingbasket/items") ~> routes ~> check {
          status shouldEqual OK
          responseAs[AllItems] shouldEqual AllItems(List(onePencil))
        }
      }
    }
    "receives a 'Delete Item' request" should {
      "delete it from Basket and respond with the deleted item" in {
        Delete("/api/shoppingbasket/items/0") ~> routes ~> check {
          status shouldEqual OK
          responseAs[ItemDeleted] shouldEqual ItemDeleted(onePencil)
        }
        Get("/api/shoppingbasket/items") ~> routes ~> check {
          status shouldEqual OK
          responseAs[AllItems] shouldEqual AllItems(Nil)
        }
      }
    }
    "receives a 'Add Item' request for a product already in the basket" should {
      "update the quantity in Basket and respond with the updated item" in {
        Post("/api/shoppingbasket/items", httpEntity) ~> routes ~> check {
          status shouldEqual Created
          responseAs[ItemAdded] shouldEqual ItemAdded(onePencil)
        }
        Post("/api/shoppingbasket/items", httpEntity) ~> routes ~> check {
          status shouldEqual Created
          responseAs[ItemUpdated] shouldEqual ItemUpdated(BasketItem("0", SampleProducts.pencil, 2))
        }
        Get("/api/shoppingbasket/items") ~> routes ~> check {
          status shouldEqual OK
          responseAs[AllItems] shouldEqual AllItems(List(BasketItem("0", SampleProducts.pencil, 2)))
        }
      }
    }
    "receives a 'Add Item' request for a product not yet in the basket" should {
      "add it in Basket with a unique id and respond with the added item" in {
        val addItemRequest = AddItemRequest(SampleProducts.smartPencil.productId, 1)
        val anotherProductHttpEntity = HttpEntity(ContentTypes.`application/json`, Serialization.write(addItemRequest))
        Post("/api/shoppingbasket/items", anotherProductHttpEntity) ~> routes ~> check {
          status shouldEqual Created
          responseAs[ItemAdded] shouldEqual ItemAdded(BasketItem("1", SampleProducts.smartPencil, 1))
        }
        Get("/api/shoppingbasket/items") ~> routes ~> check {
          status shouldEqual OK
          val allItems = responseAs[AllItems]
          allItems.items.map(_.id).distinct.size shouldEqual allItems.items.size
        }
      }
    }
    "receives a 'Add Item' request with a quantity that is not in stock" should {
      "respond with the 'Out of stock' message" in {
        val quantity = 11
        val addItemRequest = AddItemRequest(SampleProducts.pencil.productId, quantity)
        val tooManyPencilsHttpEntity = HttpEntity(ContentTypes.`application/json`, Serialization.write(addItemRequest))
        Post("/api/shoppingbasket/items", tooManyPencilsHttpEntity) ~> routes ~> check {
          val outOfStockMessage = HttpEntity(ContentTypes.`text/plain(UTF-8)`, s"Product '${SampleProducts.pencil.name}' not available in quantity $quantity")
          response shouldEqual HttpResponse(Conflict, Nil, outOfStockMessage, HttpProtocols.`HTTP/1.1`)
        }
      }
    }
    "receives a 'Add Item' request with a product that is not in the catalog" should {
      "respond with the 'Invalid product' message" in {
        val nonExistentProductId = "77"
        val tooManyPencilsHttpEntity = HttpEntity(ContentTypes.`application/json`, Serialization.write(AddItemRequest(nonExistentProductId, 11)))
        Post("/api/shoppingbasket/items", tooManyPencilsHttpEntity) ~> routes ~> check {
          val nonExistentProductMessage = HttpEntity(ContentTypes.`text/plain(UTF-8)`, s"Product with id '$nonExistentProductId' does not exist")
          response shouldEqual HttpResponse(NotFound, Nil, nonExistentProductMessage, HttpProtocols.`HTTP/1.1`)
        }
      }
    }
    "receives a 'Delete Item' request with an item that is not in the Basket" should {
      "respond with the 'Invalid item' message" in {
        val invalidItemId = "88"
        Delete(s"/api/shoppingbasket/items/$invalidItemId") ~> routes ~> check {
          val nonExistentItemMessage = HttpEntity(ContentTypes.`text/plain(UTF-8)`, s"Item with id '$invalidItemId' does not exist")
          response shouldEqual HttpResponse(NotFound, Nil, nonExistentItemMessage, HttpProtocols.`HTTP/1.1`)
        }
      }
    }
  }
}
