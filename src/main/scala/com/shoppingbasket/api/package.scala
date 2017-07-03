package com.shoppingbasket

package object api {

  sealed trait ShoppingRequest

  case class AddItemRequest(productId: String, quantity: Int) extends ShoppingRequest {
    require(quantity > 0)
  }

}
