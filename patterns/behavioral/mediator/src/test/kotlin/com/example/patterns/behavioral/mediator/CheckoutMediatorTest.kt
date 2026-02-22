package com.example.patterns.behavioral.mediator

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckoutMediatorTest {
    private fun createCheckoutSystem(): CheckoutCoordinator {
        val coordinator = CheckoutCoordinator()
        coordinator.cart = CartComponent(coordinator)
        coordinator.inventory = InventoryComponent(coordinator)
        coordinator.payment = PaymentComponent(coordinator)
        coordinator.shipping = ShippingComponent(coordinator)
        return coordinator
    }

    @Test
    fun should_completeAllSteps_when_checkoutIsSuccessful() {
        val coordinator = createCheckoutSystem()
        coordinator.cart.items = listOf("Laptop", "Mouse")

        coordinator.cart.checkout()

        assertTrue(coordinator.inventory.reserved)
        assertTrue(coordinator.payment.charged)
        assertTrue(coordinator.shipping.scheduled)
        assertEquals(emptyList(), coordinator.cart.items)
    }

    @Test
    fun should_releaseInventory_when_paymentFails() {
        val coordinator = createCheckoutSystem()
        coordinator.cart.items = listOf("Keyboard")
        coordinator.payment.shouldFail = true

        coordinator.cart.checkout()

        assertFalse(coordinator.inventory.reserved)
        assertFalse(coordinator.payment.charged)
        assertFalse(coordinator.shipping.scheduled)
        // Cart should not be cleared when payment fails
        assertEquals(listOf("Keyboard"), coordinator.cart.items)
    }

    @Test
    fun should_logAllActions_when_checkoutSucceeds() {
        val coordinator = createCheckoutSystem()
        coordinator.cart.items = listOf("Monitor")

        coordinator.cart.checkout()

        assertEquals(listOf("checkout_initiated", "cart_cleared"), coordinator.cart.log)
        assertEquals(listOf("inventory_reserved"), coordinator.inventory.log)
        assertEquals(listOf("payment_charged"), coordinator.payment.log)
        assertEquals(listOf("shipping_scheduled"), coordinator.shipping.log)
    }

    @Test
    fun should_coordinateInCorrectOrder_when_checkoutTriggered() {
        val coordinator = createCheckoutSystem()
        coordinator.cart.items = listOf("Phone")

        coordinator.cart.checkout()

        // The order of events proves the mediator orchestrates correctly:
        // 1. Cart initiates checkout
        // 2. Inventory is reserved
        // 3. Payment is charged
        // 4. Shipping is scheduled
        // 5. Cart is cleared
        val allEvents =
            coordinator.cart.log +
                coordinator.inventory.log +
                coordinator.payment.log +
                coordinator.shipping.log

        assertEquals(
            listOf(
                "checkout_initiated",
                "cart_cleared",
                "inventory_reserved",
                "payment_charged",
                "shipping_scheduled",
            ),
            allEvents,
        )
    }
}
