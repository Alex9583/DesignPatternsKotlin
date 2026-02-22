package com.example.patterns.advanced.circuitbreaker

enum class CircuitState {
    CLOSED,
    OPEN,
    HALF_OPEN,
}

data class CircuitBreakerConfig(
    val failureThreshold: Int = 3,
    val resetTimeoutMs: Long = 5000,
    val halfOpenMaxCalls: Int = 1,
) {
    init {
        require(failureThreshold > 0) { "failureThreshold must be positive, got $failureThreshold" }
        require(resetTimeoutMs > 0) { "resetTimeoutMs must be positive, got $resetTimeoutMs" }
        require(halfOpenMaxCalls > 0) { "halfOpenMaxCalls must be positive, got $halfOpenMaxCalls" }
    }
}

class CircuitBreaker(
    private val config: CircuitBreakerConfig = CircuitBreakerConfig(),
    private val clock: () -> Long = System::currentTimeMillis,
) {
    var state: CircuitState = CircuitState.CLOSED
        private set

    private var failureCount = 0
    private var lastFailureTime = 0L
    private var halfOpenCalls = 0

    fun <T> execute(action: () -> T): T {
        when (state) {
            CircuitState.OPEN -> {
                val elapsed = clock() - lastFailureTime
                if (elapsed >= config.resetTimeoutMs) {
                    transitionTo(CircuitState.HALF_OPEN)
                } else {
                    throw CircuitOpenException("Circuit is OPEN, rejecting call")
                }
            }
            CircuitState.HALF_OPEN -> {
                if (halfOpenCalls >= config.halfOpenMaxCalls) {
                    throw CircuitOpenException("Circuit is HALF_OPEN, max trial calls reached")
                }
                halfOpenCalls++
            }
            CircuitState.CLOSED -> { /* proceed */ }
        }

        return try {
            val result = action()
            onSuccess()
            result
        } catch (e: CircuitOpenException) {
            throw e
        } catch (e: Exception) {
            onFailure()
            throw e
        }
    }

    private fun onSuccess() {
        when (state) {
            CircuitState.HALF_OPEN -> {
                transitionTo(CircuitState.CLOSED)
            }
            else -> {
                failureCount = 0
            }
        }
    }

    private fun onFailure() {
        when (state) {
            CircuitState.CLOSED -> {
                failureCount++
                if (failureCount >= config.failureThreshold) {
                    lastFailureTime = clock()
                    transitionTo(CircuitState.OPEN)
                }
            }
            CircuitState.HALF_OPEN -> {
                lastFailureTime = clock()
                transitionTo(CircuitState.OPEN)
            }
            CircuitState.OPEN -> { /* already open */ }
        }
    }

    private fun transitionTo(newState: CircuitState) {
        state = newState
        when (newState) {
            CircuitState.CLOSED -> {
                failureCount = 0
                halfOpenCalls = 0
            }
            CircuitState.HALF_OPEN -> {
                halfOpenCalls = 0
            }
            CircuitState.OPEN -> { /* keep failure time */ }
        }
    }
}

class CircuitOpenException(
    message: String,
) : Exception(message)
