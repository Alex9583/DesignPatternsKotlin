package com.example.patterns.advanced.retrybackoff

import java.math.BigDecimal
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class RetryPolicy(
    val maxAttempts: Int = 3,
    val initialDelay: Duration = 100.milliseconds,
    val multiplier: Double = 2.0,
) {
    init {
        require(maxAttempts > 0) { "maxAttempts must be positive, got $maxAttempts" }
        require(!initialDelay.isNegative()) { "initialDelay must not be negative, got $initialDelay" }
    }
}

data class RetryResult<T>(
    val value: T?,
    val attempts: Int,
    val lastError: Exception?,
    val success: Boolean = lastError == null,
)

fun <T> retry(
    policy: RetryPolicy = RetryPolicy(),
    sleeper: (Duration) -> Unit = { Thread.sleep(it.inWholeMilliseconds) },
    action: () -> T,
): RetryResult<T> {
    var lastException: Exception? = null
    var delay = policy.initialDelay

    repeat(policy.maxAttempts) { attempt ->
        try {
            val result = action()
            return RetryResult(value = result, attempts = attempt + 1, lastError = null)
        } catch (e: Exception) {
            lastException = e
            if (attempt < policy.maxAttempts - 1) {
                sleeper(delay)
                delay = (delay.inWholeMilliseconds * policy.multiplier).toLong().milliseconds
            }
        }
    }

    return RetryResult(value = null, attempts = policy.maxAttempts, lastError = lastException)
}

interface PaymentGateway {
    fun charge(amount: BigDecimal): String
}

class UnreliablePaymentGateway(
    private var failuresRemaining: Int,
) : PaymentGateway {
    var callCount: Int = 0
        private set

    override fun charge(amount: BigDecimal): String {
        callCount++
        if (failuresRemaining > 0) {
            failuresRemaining--
            throw PaymentException("Gateway temporarily unavailable")
        }
        return "TXN-${amount.toPlainString()}"
    }
}

class PaymentException(
    message: String,
) : Exception(message)
