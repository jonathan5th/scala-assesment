package com.shoppingbasket.api

import akka.actor.{Actor, ActorIdentity, ActorRef, ActorSystem, Identify, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.shoppingbasket.util.ConfigParams

object ShoppingBasket {
  def props = Props(new ShoppingBasket)

  def name = "shoppingBasket"

  case object GetAllItems

  case class AddItem(productId: String, quantity: Int)

  case class DeleteItem(id: String)

  case class BasketItem(id: String = "", product: ProductCatalog.Product, quantity: Int) {
    require(quantity > 0)
  }

  sealed trait BasketResponse

  case class AllItems(items: List[BasketItem]) extends BasketResponse

  case class ItemAdded(item: BasketItem) extends BasketResponse

  case class ItemUpdated(item: BasketItem) extends BasketResponse

  case class ItemNotInStock(productName: String) extends BasketResponse

  case class InvalidProduct(productId: String) extends BasketResponse

  case object InvalidItem extends BasketResponse

  case class ItemDeleted(item: BasketItem) extends BasketResponse

}

class ShoppingBasket() extends Actor {

  import ProductCatalog._
  import ShoppingBasket._

  implicit def executionContext = ActorSystem().dispatcher
  implicit val timeout: Timeout = ConfigParams.timeout

  context.actorSelection("/user/productCatalog") ! Identify()

  def uuid = java.util.UUID.randomUUID.toString

  override def receive: Receive = {
    case ActorIdentity(_, optRef) =>
      optRef match {
        case Some(ref) => context.become(active(ref, List.empty[BasketItem]))
        case None => context.stop(self)
      }
  }

  def active(productCatalog: ActorRef, items: List[BasketItem]): Actor.Receive = {
    case GetAllItems =>
      sender ! AllItems(items)
    case AddItem(productId, quantity) =>
      val initialSender = sender
      productCatalog.ask(ProductCatalog.BookItems(productId, quantity)).mapTo[BookingResponse].foreach {
        case ItemsBooked(product) =>
          items.find(_.product == product) match {
            case None =>
              def generateId = {
                items match {
                  case Nil => "0"
                  case _ => (items.maxBy(_.id).id.toInt + 1).toString
                }
              }

              val newItem = BasketItem(generateId, product, quantity)
              context.become(active(productCatalog, items :+ newItem))
              initialSender ! ItemAdded(newItem)
            case Some(item) =>
              val updatedItem = item.copy(quantity = item.quantity + quantity)
              val updatedItems = items.map {
                case basketItem if basketItem.id == item.id => updatedItem
                case basketItem => basketItem
              }
              context.become(active(productCatalog, updatedItems))
              initialSender ! ItemUpdated(updatedItem)
          }
        case OutOfStock(product) =>
          initialSender ! ItemNotInStock(product.name)
        case InvalidProductId =>
          initialSender ! InvalidProduct(productId)
      }
    case DeleteItem(itemId) =>
      items.find(_.id == itemId) match {
        case Some(item) =>
          val initialSender = sender
          productCatalog.ask(ProductCatalog.CancelItemsBooking(item.product.productId, item.quantity)).mapTo[BookingResponse].foreach {
            case BookingCanceled =>
              context.become(active(productCatalog, items.filter(_.id != itemId)))
              initialSender ! ItemDeleted(item)
            case InvalidProductId =>
              context.become(active(productCatalog, items.filter(_.id != itemId)))
              initialSender ! InvalidProduct(item.product.productId)
          }
        case None =>
          sender() ! InvalidItem
      }
  }
}
