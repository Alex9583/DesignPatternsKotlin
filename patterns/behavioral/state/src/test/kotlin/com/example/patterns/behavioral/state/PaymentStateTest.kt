package com.example.patterns.behavioral.state

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class PaymentStateTest {
    private val processor = PaymentProcessor()

    private fun newPayment(id: String = "PAY-001"): Payment = Payment(id, BigDecimal("49.99"))

    @Test
    fun should_transitionToAuthorizedThenCaptured_when_happyPath() {
        val payment = newPayment()

        processor.authorize(payment)
        assertIs<PaymentState.Authorized>(payment.state)

        processor.capture(payment)
        assertIs<PaymentState.Captured>(payment.state)
    }

    @Test
    fun should_transitionToRefunded_when_capturedPaymentRefunded() {
        val payment = newPayment()
        processor.authorize(payment)
        processor.capture(payment)

        processor.refund(payment)

        assertIs<PaymentState.Refunded>(payment.state)
    }

    @Test
    fun should_transitionToFailed_when_failCalledFromPending() {
        val payment = newPayment()

        processor.fail(payment)

        assertIs<PaymentState.Failed>(payment.state)
    }

    @Test
    fun should_throwException_when_captureCalledFromPending() {
        val payment = newPayment("PAY-INVALID")

        val exception =
            assertFailsWith<IllegalStateException> {
                processor.capture(payment)
            }

        assertEquals(
            "Cannot capture payment PAY-INVALID: expected Authorized but was Pending",
            exception.message,
        )
    }

    @Test
    fun should_throwException_when_refundCalledFromAuthorized() {
        val payment = newPayment()
        processor.authorize(payment)

        assertFailsWith<IllegalStateException> {
            processor.refund(payment)
        }
    }

    @Test
    fun should_throwException_when_failCalledFromCaptured() {
        val payment = newPayment()
        processor.authorize(payment)
        processor.capture(payment)

        assertFailsWith<IllegalStateException> {
            processor.fail(payment)
        }
    }

    @Test
    fun should_completeFullLifecycle_when_authorizedCapturedAndRefunded() {
        val payment = newPayment("PAY-LIFECYCLE")

        assertIs<PaymentState.Pending>(payment.state)

        processor.authorize(payment)
        assertIs<PaymentState.Authorized>(payment.state)

        processor.capture(payment)
        assertIs<PaymentState.Captured>(payment.state)

        processor.refund(payment)
        assertIs<PaymentState.Refunded>(payment.state)

        assertEquals(BigDecimal("49.99"), payment.amount)
    }
}
