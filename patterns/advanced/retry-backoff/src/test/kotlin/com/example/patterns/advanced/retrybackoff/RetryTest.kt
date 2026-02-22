package com.example.patterns.advanced.retrybackoff

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class RetryTest {
    private val noOpSleeper: (Duration) -> Unit = {}

    @Test
    fun should_succeedImmediately_when_noFailure() {
        val result = retry(sleeper = noOpSleeper) { "OK" }

        assertTrue(result.success)
        assertEquals("OK", result.value)
        assertEquals(1, result.attempts)
        assertNull(result.lastError)
    }

    @Test
    fun should_retryAndSucceed_when_transientFailure() {
        val gateway = UnreliablePaymentGateway(failuresRemaining = 2)

        val result =
            retry(
                policy = RetryPolicy(maxAttempts = 3),
                sleeper = noOpSleeper,
            ) {
                gateway.charge(BigDecimal("99.99"))
            }

        assertTrue(result.success)
        assertEquals("TXN-99.99", result.value)
        assertEquals(3, result.attempts)
        assertEquals(3, gateway.callCount)
    }

    @Test
    fun should_failAfterMaxAttempts_when_allRetriesFail() {
        val gateway = UnreliablePaymentGateway(failuresRemaining = 10)

        val result =
            retry(
                policy = RetryPolicy(maxAttempts = 3),
                sleeper = noOpSleeper,
            ) {
                gateway.charge(BigDecimal("50.00"))
            }

        assertEquals(false, result.success)
        assertNull(result.value)
        assertEquals(3, result.attempts)
        assertNotNull(result.lastError)
        assertTrue(result.lastError is PaymentException)
    }

    @Test
    fun should_applyExponentialBackoff_when_retrying() {
        val delays = mutableListOf<Duration>()
        val sleeper: (Duration) -> Unit = { delays.add(it) }
        val gateway = UnreliablePaymentGateway(failuresRemaining = 3)

        retry(
            policy =
                RetryPolicy(
                    maxAttempts = 4,
                    initialDelay = 100.milliseconds,
                    multiplier = 2.0,
                ),
            sleeper = sleeper,
        ) {
            gateway.charge(BigDecimal("10.00"))
        }

        assertEquals(3, delays.size)
        assertEquals(100L, delays[0].inWholeMilliseconds)
        assertEquals(200L, delays[1].inWholeMilliseconds)
        assertEquals(400L, delays[2].inWholeMilliseconds)
    }

    @Test
    fun should_respectCustomPolicy_when_configuredDifferently() {
        var attempts = 0
        val result =
            retry(
                policy = RetryPolicy(maxAttempts = 5),
                sleeper = noOpSleeper,
            ) {
                attempts++
                if (attempts < 5) throw RuntimeException("Not yet")
                "Finally"
            }

        assertTrue(result.success)
        assertEquals("Finally", result.value)
        assertEquals(5, result.attempts)
    }
}
