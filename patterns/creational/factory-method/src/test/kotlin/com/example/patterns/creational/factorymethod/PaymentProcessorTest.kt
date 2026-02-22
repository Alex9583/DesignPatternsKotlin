package com.example.patterns.creational.factorymethod

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PaymentProcessorTest {
    @Test
    fun should_processPayment_when_usingCreditCard() {
        val factory = PaymentProcessorFactoryProvider.factoryFor(PaymentMethod.CreditCard)
        val result = factory.processPayment(Money(BigDecimal("49.99")))

        assertTrue(result.success)
        assertTrue(result.transactionId.startsWith("CC-"))
    }

    @Test
    fun should_processPayment_when_usingPayPal() {
        val factory = PaymentProcessorFactoryProvider.factoryFor(PaymentMethod.PayPal)
        val result = factory.processPayment(Money(BigDecimal("120.00"), "USD"))

        assertTrue(result.success)
        assertTrue(result.transactionId.startsWith("PP-"))
    }

    @Test
    fun should_processPayment_when_usingBankTransfer() {
        val factory = PaymentProcessorFactoryProvider.factoryFor(PaymentMethod.BankTransfer)
        val result = factory.processPayment(Money(BigDecimal("5000.00")))

        assertTrue(result.success)
        assertTrue(result.transactionId.startsWith("BT-"))
    }

    @Test
    fun should_rejectNegativeAmount_when_creatingMoney() {
        assertFailsWith<IllegalArgumentException> {
            Money(BigDecimal("-10.00"))
        }
    }

    @Test
    fun should_createDifferentProcessors_when_differentMethodsRequested() {
        val ccFactory = PaymentProcessorFactoryProvider.factoryFor(PaymentMethod.CreditCard)
        val ppFactory = PaymentProcessorFactoryProvider.factoryFor(PaymentMethod.PayPal)

        val ccProcessor = ccFactory.createProcessor()
        val ppProcessor = ppFactory.createProcessor()

        assertEquals("CreditCard", ccProcessor.name())
        assertEquals("PayPal", ppProcessor.name())
    }

    @Test
    fun should_handleAllPaymentMethods_when_iteratingSealed() {
        val methods = listOf(PaymentMethod.CreditCard, PaymentMethod.PayPal, PaymentMethod.BankTransfer)
        val money = Money(BigDecimal("10.00"))

        val results =
            methods.map { method ->
                PaymentProcessorFactoryProvider.factoryFor(method).processPayment(money)
            }

        assertTrue(results.all { it.success })
        assertEquals(3, results.map { it.transactionId.substringBefore("-") }.distinct().size)
    }
}
