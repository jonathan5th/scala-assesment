package com.shoppingbasket

import akka.actor.ActorSystem
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.shoppingbasket.api.ProductCatalog
import com.shoppingbasket.api.ProductCatalog._
import com.shoppingbasket.util.SampleProducts
import org.scalatest.{MustMatchers, WordSpecLike}

class ProductCatalogSpec extends TestKit(ActorSystem("testShoppingBasket"))
  with WordSpecLike
  with MustMatchers
  with ImplicitSender
  with DefaultTimeout
  with StopSystemAfterAll {

  "The ProductCatalog" must {
    "Book a product if it is in stock" in new CatalogScope {
      productCatalog ! BookItems(SampleProducts.pencil.productId, 1)
      expectMsg(ItemsBooked(SampleProducts.pencil))
    }
    "Give an appropriate message when trying to book a product that is not in stock" in new CatalogScope {
      productCatalog ! BookItems(SampleProducts.pencil.productId, 11)
      expectMsg(OutOfStock(SampleProducts.pencil))
    }
    "Give an appropriate message when trying to book a product that doesn't exist" in new CatalogScope {
      val invalidProduct = Product("111", "aProductNotInTheCatalog", "", 0.0)
      productCatalog ! BookItems(invalidProduct.productId, 11)
      expectMsg(InvalidProductId)
    }
    "Cancel a booking" in new CatalogScope {
      productCatalog ! CancelItemsBooking(SampleProducts.pencil.productId, 2)
      expectMsg(BookingCanceled)
    }
    "Give an appropriate message when trying to cancel booking for a product that doesn't exist" in new CatalogScope {
      val invalidProduct = Product("111", "aProductNotInTheCatalog", "", 0.0)
      productCatalog ! CancelItemsBooking(invalidProduct.productId, 2)
      expectMsg(InvalidProductId)
    }
  }

  class CatalogScope {
    val productCatalog = system.actorOf(ProductCatalog.props)
  }

}
