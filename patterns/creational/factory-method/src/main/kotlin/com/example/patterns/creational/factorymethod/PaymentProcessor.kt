package com.example.patterns.creational.factorymethod

import java.math.BigDecimal

data class Money(
    val amount: BigDecimal,
    val currency: String = "EUR",
) {
    init {
        require(amount >= BigDecimal.ZERO) { "Amount must be non-negative" }
    }
}

data class PaymentResult(
    val success: Boolean,
    val transactionId: String,
    val message: String,
)

interface PaymentProcessor {
    fun process(money: Money): PaymentResult

    fun name(): String
}

class CreditCardProcessor : PaymentProcessor {
    override fun process(money: Money): PaymentResult =
        PaymentResult(
            success = true,
            transactionId = "CC-${System.nanoTime()}",
            message = "Credit card payment of ${money.amount} ${money.currency} processed",
        )

    override fun name(): String = "CreditCard"
}

class PayPalProcessor : PaymentProcessor {
    override fun process(money: Money): PaymentResult =
        PaymentResult(
            success = true,
            transactionId = "PP-${System.nanoTime()}",
            message = "PayPal payment of ${money.amount} ${money.currency} processed",
        )

    override fun name(): String = "PayPal"
}

class BankTransferProcessor : PaymentProcessor {
    override fun process(money: Money): PaymentResult =
        PaymentResult(
            success = true,
            transactionId = "BT-${System.nanoTime()}",
            message = "Bank transfer of ${money.amount} ${money.currency} initiated",
        )

    override fun name(): String = "BankTransfer"
}

sealed interface PaymentMethod {
    data object CreditCard : PaymentMethod

    data object PayPal : PaymentMethod

    data object BankTransfer : PaymentMethod
}

abstract class PaymentProcessorFactory {
    abstract fun createProcessor(): PaymentProcessor

    fun processPayment(money: Money): PaymentResult {
        val processor = createProcessor()
        return processor.process(money)
    }
}

class CreditCardProcessorFactory : PaymentProcessorFactory() {
    override fun createProcessor(): PaymentProcessor = CreditCardProcessor()
}

class PayPalProcessorFactory : PaymentProcessorFactory() {
    override fun createProcessor(): PaymentProcessor = PayPalProcessor()
}

class BankTransferProcessorFactory : PaymentProcessorFactory() {
    override fun createProcessor(): PaymentProcessor = BankTransferProcessor()
}

object PaymentProcessorFactoryProvider {
    fun factoryFor(method: PaymentMethod): PaymentProcessorFactory =
        when (method) {
            PaymentMethod.CreditCard -> CreditCardProcessorFactory()
            PaymentMethod.PayPal -> PayPalProcessorFactory()
            PaymentMethod.BankTransfer -> BankTransferProcessorFactory()
        }
}
