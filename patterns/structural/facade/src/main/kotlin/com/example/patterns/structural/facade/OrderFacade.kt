package com.example.patterns.structural.facade

import java.math.BigDecimal
import java.util.UUID

data class PaymentConfirmation(
    val transactionId: String,
    val success: Boolean,
)

data class TrackingInfo(
    val trackingNumber: String,
    val estimatedDays: Int,
)

data class OrderResult(
    val success: Boolean,
    val orderId: String?,
    val tracking: TrackingInfo?,
    val message: String,
)

class InventoryService {
    private val stock = mutableMapOf<String, Int>()

    fun restock(
        productId: String,
        quantity: Int,
    ) {
        stock[productId] = (stock[productId] ?: 0) + quantity
    }

    fun check(
        productId: String,
        quantity: Int,
    ): Boolean = (stock[productId] ?: 0) >= quantity

    fun reserve(
        productId: String,
        quantity: Int,
    ) {
        val current = stock[productId] ?: 0
        require(current >= quantity) { "Insufficient stock for $productId" }
        stock[productId] = current - quantity
    }
}

class PaymentService {
    var shouldFail: Boolean = false

    fun charge(
        amount: BigDecimal,
        method: String,
    ): PaymentConfirmation =
        if (shouldFail) {
            PaymentConfirmation(transactionId = "", success = false)
        } else {
            PaymentConfirmation(
                transactionId = "TXN-${UUID.randomUUID().toString().take(8)}",
                success = true,
            )
        }
}

class ShippingService {
    fun schedule(
        orderId: String,
        address: String,
    ): TrackingInfo =
        TrackingInfo(
            trackingNumber = "TRACK-${orderId.takeLast(8)}",
            estimatedDays = 3,
        )
}

class OrderFacade(
    private val inventory: InventoryService,
    private val payment: PaymentService,
    private val shipping: ShippingService,
) {
    fun placeOrder(
        productId: String,
        quantity: Int,
        amount: BigDecimal,
        paymentMethod: String,
        shippingAddress: String,
    ): OrderResult {
        // Step 0 — validate inputs
        require(quantity > 0) { "Quantity must be positive, got $quantity" }
        require(amount > BigDecimal.ZERO) { "Amount must be positive, got $amount" }

        // Step 1 — check inventory
        if (!inventory.check(productId, quantity)) {
            return OrderResult(
                success = false,
                orderId = null,
                tracking = null,
                message = "Product $productId is out of stock",
            )
        }

        // Step 2 — charge payment
        val confirmation = payment.charge(amount, paymentMethod)
        if (!confirmation.success) {
            return OrderResult(
                success = false,
                orderId = null,
                tracking = null,
                message = "Payment failed for method $paymentMethod",
            )
        }

        // Step 3 — reserve inventory
        inventory.reserve(productId, quantity)

        // Step 4 — schedule shipping
        val orderId = "ORD-${UUID.randomUUID().toString().take(8)}"
        val tracking = shipping.schedule(orderId, shippingAddress)

        return OrderResult(
            success = true,
            orderId = orderId,
            tracking = tracking,
            message = "Order placed successfully",
        )
    }
}
