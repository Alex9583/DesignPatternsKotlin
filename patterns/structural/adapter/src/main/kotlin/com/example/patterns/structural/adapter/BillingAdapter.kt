package com.example.patterns.structural.adapter

interface ModernPaymentGateway {
    fun charge(
        amountCents: Long,
        currency: String,
    ): PaymentReceipt
}

data class PaymentReceipt(
    val success: Boolean,
    val transactionId: String,
    val amountCents: Long,
)

class LegacyBillingService {
    fun processBill(
        dollarAmount: Double,
        accountId: String,
    ): String {
        if (dollarAmount <= 0) {
            return "FAIL:invalid-amount"
        }
        val txnId = "TXN-${accountId.uppercase()}-${System.nanoTime()}"
        return "OK:$txnId"
    }
}

class BillingAdapter(
    private val legacyService: LegacyBillingService,
    private val accountId: String,
) : ModernPaymentGateway {
    override fun charge(
        amountCents: Long,
        currency: String,
    ): PaymentReceipt {
        val dollarAmount = amountCents / 100.0
        val result = legacyService.processBill(dollarAmount, accountId)

        return parseResult(result, amountCents)
    }

    private fun parseResult(
        result: String,
        amountCents: Long,
    ): PaymentReceipt {
        val parts = result.split(":", limit = 2)
        return when (parts[0]) {
            "OK" ->
                PaymentReceipt(
                    success = true,
                    transactionId = parts[1],
                    amountCents = amountCents,
                )
            else ->
                PaymentReceipt(
                    success = false,
                    transactionId = "",
                    amountCents = amountCents,
                )
        }
    }
}
