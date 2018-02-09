package com.shoppingbasket.api

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import com.shoppingbasket.util.ConfigParams

import scala.concurrent.ExecutionContext

class ShoppingAPI(system: ActorSystem)
  extends ShoppingRoutes {
  implicit val requestTimeout = ConfigParams.timeout

  implicit def executionContext = system.dispatcher

  override def createBasket() = {
    //create the ProductCatalog first as the ShoppingBasket needs it
    system.actorOf(ProductCatalog.props, ProductCatalog.name)
    system.actorOf(ShoppingBasket.props, ShoppingBasket.name)
  }
}

trait ShoppingRoutes extends BasketAPI with ShoppingMarshalling {
  def routes = {
    pathPrefix("api" / "shoppingbasket" / "items") {
      pathEndOrSingleSlash {
        get {
          // GET api/shoppingbasket/items
          onSuccess(getAllItems()) { items => complete(OK, items) }
        } ~
          post {
            //POST api/shoppingbasket/items
            entity(as[AddItemRequest]) { itemToAdd => processAddItemRequest(itemToAdd) }
          }
      } ~
        (pathPrefix(Segment) & pathEndOrSingleSlash & delete) {
          // DELETE api/shoppingbasket/items/:itemId
          itemId => processDeleteItemRequest(itemId)
        }
    }
  }

  private def processAddItemRequest(itemToAdd: AddItemRequest) = {
    onSuccess(addItem(itemToAdd.productId, itemToAdd.quantity)) {
      case response@ShoppingBasket.ItemAdded(_) => complete(Created, response)
      case response@ShoppingBasket.ItemUpdated(_) => complete(Created, response)
      case ShoppingBasket.ItemNotInStock(productName) =>
        val err = s"Product '$productName' not available in quantity ${itemToAdd.quantity}"
        complete(Conflict, err)
      case ShoppingBasket.InvalidProduct(productId) =>
        val err = s"Product with id '$productId' does not exist"
        complete(NotFound, err)
      case _ => complete(InternalServerError)
    }
  }

  private def processDeleteItemRequest(itemId: String) = {
    onSuccess(deleteItem(itemId)) {
      case response@ShoppingBasket.ItemDeleted(_) => complete(OK, response)
      case ShoppingBasket.InvalidProduct(productId) =>
        val err = s"Product with id '$productId' does not exist"
        complete(NotFound, err)
      case ShoppingBasket.InvalidItem =>
        val err = s"Item with id '$itemId' does not exist"
        complete(NotFound, err)
      case _ => complete(InternalServerError)
    }
  }
}

trait BasketAPI {

  import ShoppingBasket._

  def createBasket(): ActorRef

  implicit def executionContext: ExecutionContext

  implicit def requestTimeout: Timeout

  val basket = createBasket()

  def getAllItems() =
    basket.ask(GetAllItems).mapTo[AllItems]

  def addItem(productId: String, quantity: Int) =
    basket.ask(AddItem(productId, quantity)).mapTo[BasketResponse]

  def deleteItem(productId: String) =
    basket.ask(DeleteItem(productId)).mapTo[BasketResponse]

}
