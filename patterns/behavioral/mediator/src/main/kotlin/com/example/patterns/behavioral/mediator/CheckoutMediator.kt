package com.example.patterns.behavioral.mediator

interface CheckoutMediator {
    fun notify(
        sender: CheckoutComponent,
        event: String,
    )
}

abstract class CheckoutComponent(
    val mediator: CheckoutMediator,
) {
    val log: MutableList<String> = mutableListOf()
}

class CartComponent(
    mediator: CheckoutMediator,
) : CheckoutComponent(mediator) {
    var items: List<String> = emptyList()

    fun checkout() {
        log.add("checkout_initiated")
        mediator.notify(this, "checkout")
    }

    fun clear() {
        items = emptyList()
        log.add("cart_cleared")
    }
}

class InventoryComponent(
    mediator: CheckoutMediator,
) : CheckoutComponent(mediator) {
    var reserved: Boolean = false

    fun reserve() {
        reserved = true
        log.add("inventory_reserved")
    }

    fun release() {
        reserved = false
        log.add("inventory_released")
    }
}

class PaymentComponent(
    mediator: CheckoutMediator,
) : CheckoutComponent(mediator) {
    var charged: Boolean = false
    var shouldFail: Boolean = false

    fun charge() {
        if (shouldFail) {
            charged = false
            log.add("payment_failed")
            mediator.notify(this, "payment_failed")
            return
        }
        charged = true
        log.add("payment_charged")
    }

    fun refund() {
        charged = false
        log.add("payment_refunded")
    }
}

class ShippingComponent(
    mediator: CheckoutMediator,
) : CheckoutComponent(mediator) {
    var scheduled: Boolean = false

    fun schedule() {
        scheduled = true
        log.add("shipping_scheduled")
    }
}

class CheckoutCoordinator : CheckoutMediator {
    lateinit var cart: CartComponent
    lateinit var inventory: InventoryComponent
    lateinit var payment: PaymentComponent
    lateinit var shipping: ShippingComponent

    override fun notify(
        sender: CheckoutComponent,
        event: String,
    ) {
        when (event) {
            "checkout" -> {
                inventory.reserve()
                payment.charge()
                // Only schedule shipping and clear cart if payment succeeded
                if (payment.charged) {
                    shipping.schedule()
                    cart.clear()
                }
            }
            "payment_failed" -> {
                inventory.release()
            }
        }
    }
}
