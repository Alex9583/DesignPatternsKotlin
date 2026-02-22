package com.example.patterns.behavioral.state

import java.math.BigDecimal

sealed class PaymentState {
    data object Pending : PaymentState()

    data object Authorized : PaymentState()

    data object Captured : PaymentState()

    data object Refunded : PaymentState()

    data object Failed : PaymentState()
}

class Payment(
    val id: String,
    val amount: BigDecimal,
    state: PaymentState = PaymentState.Pending,
) {
    init {
        require(amount > BigDecimal.ZERO) { "Amount must be positive, got $amount" }
    }

    var state: PaymentState = state
        internal set
}

class PaymentProcessor {
    fun authorize(payment: Payment) {
        requireState(payment, PaymentState.Pending, "authorize")
        payment.state = PaymentState.Authorized
    }

    fun capture(payment: Payment) {
        requireState(payment, PaymentState.Authorized, "capture")
        payment.state = PaymentState.Captured
    }

    fun refund(payment: Payment) {
        requireState(payment, PaymentState.Captured, "refund")
        payment.state = PaymentState.Refunded
    }

    fun fail(payment: Payment) {
        when (payment.state) {
            PaymentState.Pending,
            PaymentState.Authorized,
            -> payment.state = PaymentState.Failed

            else -> throw IllegalStateException(
                "Cannot fail payment ${payment.id}: current state is ${payment.state}",
            )
        }
    }

    private fun requireState(
        payment: Payment,
        expected: PaymentState,
        action: String,
    ) {
        if (payment.state != expected) {
            throw IllegalStateException(
                "Cannot $action payment ${payment.id}: " +
                    "expected $expected but was ${payment.state}",
            )
        }
    }
}
