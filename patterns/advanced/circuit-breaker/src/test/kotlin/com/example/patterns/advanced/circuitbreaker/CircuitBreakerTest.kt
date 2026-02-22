package com.example.patterns.advanced.circuitbreaker

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CircuitBreakerTest {
    private val config =
        CircuitBreakerConfig(
            failureThreshold = 3,
            resetTimeoutMs = 1000,
            halfOpenMaxCalls = 1,
        )

    @Test
    fun should_allowCalls_when_circuitClosed() {
        val cb = CircuitBreaker(config)

        val result = cb.execute { "OK" }

        assertEquals("OK", result)
        assertEquals(CircuitState.CLOSED, cb.state)
    }

    @Test
    fun should_openCircuit_when_failureThresholdReached() {
        val cb = CircuitBreaker(config)

        repeat(3) {
            runCatching { cb.execute { throw RuntimeException("fail") } }
        }

        assertEquals(CircuitState.OPEN, cb.state)
    }

    @Test
    fun should_rejectCalls_when_circuitOpen() {
        val cb = CircuitBreaker(config)

        repeat(3) {
            runCatching { cb.execute { throw RuntimeException("fail") } }
        }

        assertFailsWith<CircuitOpenException> {
            cb.execute { "should not run" }
        }
    }

    @Test
    fun should_transitionToHalfOpen_when_timeoutElapsed() {
        var currentTime = 0L
        val cb = CircuitBreaker(config) { currentTime }

        repeat(3) {
            runCatching { cb.execute { throw RuntimeException("fail") } }
        }
        assertEquals(CircuitState.OPEN, cb.state)

        currentTime = 1500L
        val result = cb.execute { "recovered" }

        assertEquals("recovered", result)
        assertEquals(CircuitState.CLOSED, cb.state)
    }

    @Test
    fun should_reopenCircuit_when_halfOpenCallFails() {
        var currentTime = 0L
        val cb = CircuitBreaker(config) { currentTime }

        repeat(3) {
            runCatching { cb.execute { throw RuntimeException("fail") } }
        }

        currentTime = 1500L
        runCatching { cb.execute { throw RuntimeException("still failing") } }

        assertEquals(CircuitState.OPEN, cb.state)
    }

    @Test
    fun should_resetFailureCount_when_successfulCallInClosedState() {
        val cb = CircuitBreaker(config)

        repeat(2) {
            runCatching { cb.execute { throw RuntimeException("fail") } }
        }
        cb.execute { "success resets counter" }

        runCatching { cb.execute { throw RuntimeException("fail") } }

        assertEquals(CircuitState.CLOSED, cb.state)
    }

    @Test
    fun should_useCustomConfig_when_differentThresholdProvided() {
        val strictConfig = CircuitBreakerConfig(failureThreshold = 1)
        val cb = CircuitBreaker(strictConfig)

        runCatching { cb.execute { throw RuntimeException("one strike") } }

        assertEquals(CircuitState.OPEN, cb.state)
    }
}
