package com.example.patterns.structural.facade

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OrderFacadeTest {
    private fun buildFacade(): Triple<InventoryService, PaymentService, OrderFacade> {
        val inventory = InventoryService()
        val payment = PaymentService()
        val shipping = ShippingService()
        val facade = OrderFacade(inventory, payment, shipping)
        return Triple(inventory, payment, facade)
    }

    @Test
    fun should_placeOrderSuccessfully_when_stockAndPaymentAvailable() {
        val (inventory, _, facade) = buildFacade()
        inventory.restock("SKU-001", 10)

        val result =
            facade.placeOrder(
                productId = "SKU-001",
                quantity = 2,
                amount = BigDecimal("49.99"),
                paymentMethod = "credit-card",
                shippingAddress = "42 Rue de Rivoli, Paris",
            )

        assertTrue(result.success)
        assertNotNull(result.orderId)
        assertNotNull(result.tracking)
        assertEquals("Order placed successfully", result.message)
    }

    @Test
    fun should_failOrder_when_productOutOfStock() {
        val (_, _, facade) = buildFacade()
        // No restock — inventory is empty

        val result =
            facade.placeOrder(
                productId = "SKU-002",
                quantity = 1,
                amount = BigDecimal("19.99"),
                paymentMethod = "credit-card",
                shippingAddress = "1 Avenue des Champs-Elysees, Paris",
            )

        assertEquals(false, result.success)
        assertNull(result.orderId)
        assertNull(result.tracking)
        assertTrue(result.message.contains("out of stock"))
    }

    @Test
    fun should_failOrder_when_paymentDeclined() {
        val (inventory, payment, facade) = buildFacade()
        inventory.restock("SKU-003", 5)
        payment.shouldFail = true

        val result =
            facade.placeOrder(
                productId = "SKU-003",
                quantity = 1,
                amount = BigDecimal("99.00"),
                paymentMethod = "debit-card",
                shippingAddress = "10 Place Bellecour, Lyon",
            )

        assertEquals(false, result.success)
        assertNull(result.orderId)
        assertTrue(result.message.contains("Payment failed"))
    }

    @Test
    fun should_simplifyClientCode_when_singleMethodCallOrchestratesSubsystems() {
        val (inventory, _, facade) = buildFacade()
        inventory.restock("SKU-004", 20)

        val result =
            facade.placeOrder(
                productId = "SKU-004",
                quantity = 3,
                amount = BigDecimal("150.00"),
                paymentMethod = "paypal",
                shippingAddress = "5 Quai des Belges, Marseille",
            )

        assertTrue(result.success)
        assertNotNull(result.tracking)
        assertTrue(result.tracking.trackingNumber.startsWith("TRACK-"))
    }
}
