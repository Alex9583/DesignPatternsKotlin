package com.example.patterns.structural.adapter

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BillingAdapterTest {
    private val legacyService = LegacyBillingService()

    @Test
    fun should_returnSuccessfulReceipt_when_legacyServiceAcceptsPayment() {
        val gateway: ModernPaymentGateway = BillingAdapter(legacyService, "acct-42")

        val receipt = gateway.charge(amountCents = 1999, currency = "USD")

        assertTrue(receipt.success)
        assertTrue(receipt.transactionId.startsWith("TXN-ACCT-42-"))
        assertEquals(1999L, receipt.amountCents)
    }

    @Test
    fun should_returnFailedReceipt_when_amountIsZeroOrNegative() {
        val gateway: ModernPaymentGateway = BillingAdapter(legacyService, "acct-1")

        val receipt = gateway.charge(amountCents = 0, currency = "EUR")

        assertFalse(receipt.success)
        assertEquals("", receipt.transactionId)
        assertEquals(0L, receipt.amountCents)
    }

    @Test
    fun should_allowSwappingAdapters_when_differentLegacyAccountsAreUsed() {
        val gatewayA: ModernPaymentGateway = BillingAdapter(legacyService, "shop-eu")
        val gatewayB: ModernPaymentGateway = BillingAdapter(legacyService, "shop-us")

        val receiptA = gatewayA.charge(amountCents = 500, currency = "EUR")
        val receiptB = gatewayB.charge(amountCents = 500, currency = "USD")

        assertTrue(receiptA.transactionId.contains("SHOP-EU"))
        assertTrue(receiptB.transactionId.contains("SHOP-US"))
    }

    @Test
    fun should_compileAndWork_when_clientOnlyKnowsModernPaymentGateway() {
        fun processOrder(
            gateway: ModernPaymentGateway,
            totalCents: Long,
        ): Boolean = gateway.charge(totalCents, "USD").success

        val adapter = BillingAdapter(legacyService, "merchant-7")

        assertTrue(processOrder(adapter, 4200))
    }

    @Test
    fun should_handleSmallCentAmount_when_lessThanOneDollar() {
        val gateway: ModernPaymentGateway = BillingAdapter(legacyService, "micro-pay")

        val receipt = gateway.charge(amountCents = 1, currency = "USD")

        assertTrue(receipt.success)
        assertEquals(1L, receipt.amountCents)
    }
}
