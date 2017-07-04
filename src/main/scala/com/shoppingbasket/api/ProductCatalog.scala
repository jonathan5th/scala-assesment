package com.shoppingbasket.api

import akka.actor.{Actor, Props}
import com.shoppingbasket.util.SampleProducts

object ProductCatalog {
  def props = Props(new ProductCatalog)

  def name = "productCatalog"

  case class Product(productId: String,
                     name: String,
                     description: String,
                     price: Double)

  case class ProductEntry(product: Product, stock: Int) {
    require(stock >= 0)
  }

  case class BookItems(productId: String, quantity: Int) {
    require(quantity > 0)
  }

  case class CancelItemsBooking(productId: String, quantity: Int) {
    require(quantity > 0)
  }

  sealed trait BookingResponse

  case class ItemsBooked(product: Product) extends BookingResponse

  case class OutOfStock(product: Product) extends BookingResponse

  case object InvalidProductId extends BookingResponse

  case object BookingCanceled extends BookingResponse

}

class ProductCatalog extends Actor {

  import ProductCatalog._

  private def updateStockForProductId(products: List[ProductEntry], productId: String, newStock: Int) = {
    products.map {
      case ProductEntry(product@Product(`productId`, _, _, _), _) => ProductEntry(product, newStock)
      case productEntry => productEntry
    }
  }

  override def receive: Receive = active(List(ProductEntry(SampleProducts.pencil, 10), ProductEntry(SampleProducts.smartPencil, 10)))

  def active(products: List[ProductEntry]): Receive = {

    case BookItems(productId, quantity) =>
      products.find(_.product.productId == productId) match {
        case None =>
          sender ! InvalidProductId
        case Some(productEntry) if productEntry.stock < quantity =>
          sender ! OutOfStock(productEntry.product)
        case Some(productEntry) =>
          val updatedProducts = updateStockForProductId(products, productId, productEntry.stock - quantity)
          context.become(active(updatedProducts))
          sender ! ItemsBooked(productEntry.product)
      }
    case CancelItemsBooking(productId, quantity) =>
      products.find(_.product.productId == productId) match {
        case None =>
          sender ! InvalidProductId
        case Some(productEntry) =>
          val updatedProducts = updateStockForProductId(products, productId, productEntry.stock + quantity)
          context.become(active(updatedProducts))
          sender ! BookingCanceled
      }
  }
}
