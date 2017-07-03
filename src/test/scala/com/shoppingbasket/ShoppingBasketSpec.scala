package com.shoppingbasket

import akka.actor.{ActorIdentity, ActorSystem}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.shoppingbasket.api.ProductCatalog.Product
import com.shoppingbasket.api.{ProductCatalog, ShoppingBasket}
import com.shoppingbasket.api.ShoppingBasket._
import com.shoppingbasket.util.SampleProducts
import org.scalatest.{MustMatchers, WordSpecLike}

class ShoppingBasketSpec extends TestKit(ActorSystem("testShoppingBasket"))
  with WordSpecLike
  with MustMatchers
  with ImplicitSender
  with DefaultTimeout
  with StopSystemAfterAll {

  "The ShoppingBasket" must {

    "Add an item to the basket" in new BasketScope {
      expectMsg(ItemAdded(onePencilItem))
    }
    "List the contents of the basket" in new BasketScope {
      expectMsg(ItemAdded(onePencilItem))
      shoppingBasket ! GetAllItems
      expectMsg(AllItems(List(onePencilItem)))
    }
    "Update the quantity of an item in the basket" in new BasketScope {
      expectMsg(ItemAdded(onePencilItem))
      shoppingBasket ! AddItem("1", 1)
      expectMsg(ItemUpdated(BasketItem("0", SampleProducts.pencil, 2)))
    }
    "Delete an item from the basket" in new BasketScope {
      expectMsg(ItemAdded(onePencilItem))
      shoppingBasket ! DeleteItem("0")
      expectMsg(ItemDeleted(onePencilItem))
    }
    "Give an appropriate response when a product is not in stock" in new BasketScope {
      expectMsg(ItemAdded(onePencilItem))
      shoppingBasket ! AddItem("1", 10)
      expectMsg(ItemNotInStock(SampleProducts.pencil.name))
    }
    "Give an appropriate response when trying to add a product that doesn't exist" in new BasketScope {
      expectMsg(ItemAdded(onePencilItem))
      val invalidProduct = Product("111", "aProductNotInTheCatalog", "", 0.0)
      shoppingBasket ! AddItem(invalidProduct.productId, 1)
      expectMsg(InvalidProduct(invalidProduct.productId))
    }
    "Give an appropriate response when trying to delete an item that doesn't exist" in new BasketScope {
      expectMsg(ItemAdded(onePencilItem))
      shoppingBasket ! DeleteItem("777")
      expectMsg(InvalidItem)
    }

  }

  class BasketScope {
    val productCatalog = system.actorOf(ProductCatalog.props)
    val shoppingBasket = system.actorOf(ShoppingBasket.props)
    val onePencilItem = BasketItem("0", SampleProducts.pencil, 1)
    shoppingBasket ! ActorIdentity("correlationId", Some(productCatalog))
    shoppingBasket ! AddItem("1", 1)
    Thread.sleep(100)
  }

}
