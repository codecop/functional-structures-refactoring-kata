package org.fprefactoring

import org.fprefactoring.Models._

object App {

  trait BoolResult[+A]
  case class TrueResult[A](value: A) extends BoolResult[A]
  case class FalseResult[A]() extends BoolResult[A]

  def applyDiscount(cartId: CartId, storage: Storage[Cart]): Unit = {
    val cart = loadCart(cartId)
    val loadResult = if (cart != Cart.missingCart) TrueResult(cart) else FalseResult()
    if (cart != Cart.missingCart) {
      val rule = lookupDiscountRule(cart.customerId)
      val lookupResult = if (rule != DiscountRule.noDiscount) TrueResult(rule) else FalseResult()
      if (rule != DiscountRule.noDiscount) {
        val discount = rule(cart)
        val updatedCart = updateAmount(cart, discount)
        save(updatedCart, storage)
      }
    }
  }

  def loadCart(id: CartId): Cart =
    if (id.value.contains("gold")) Cart(id, CustomerId("gold-customer"), Amount(100))
    else if (id.value.contains("normal")) Cart(id, CustomerId("normal-customer"), Amount(100))
    else Cart.missingCart

  def lookupDiscountRule(id: CustomerId): DiscountRule =
    if (id.value.contains("gold")) DiscountRule(half)
    else DiscountRule.noDiscount

  def half(cart: Cart): Amount =
    Amount(cart.amount.value / 2)

  def updateAmount(cart: Cart, discount: Amount): Cart =
    cart.copy(id = cart.id, customerId = cart.customerId, amount = Amount(cart.amount.value - discount.value))

  def save(cart: Cart, storage: Storage[Cart]): Unit =
    storage.flush(cart)
}
