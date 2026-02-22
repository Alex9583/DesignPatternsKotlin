package com.example.patterns.behavioral.chainofresponsibility

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthHandlerTest {
    private val credentials = mapOf("alice" to "secret123", "bob" to "pass456")
    private val twoFactorUsers = setOf("alice")
    private val twoFactorCodes = mapOf("alice" to "123456")

    private fun defaultChain(): AuthHandler =
        buildAuthChain(
            RateLimitHandler(maxAttempts = 3),
            CredentialHandler(credentials),
            TwoFactorHandler(twoFactorUsers, twoFactorCodes),
        )

    @Test
    fun should_authenticateSuccessfully_when_allChecksPass() {
        val chain = defaultChain()
        val request =
            AuthRequest(
                username = "alice",
                password = "secret123",
                ip = "192.168.1.1",
                twoFactorCode = "123456",
            )

        val result = chain.handle(request)

        assertTrue(result.authenticated)
        assertNull(result.rejectedBy)
    }

    @Test
    fun should_authenticateWithout2FA_when_userDoesNotRequireIt() {
        val chain = defaultChain()
        val request =
            AuthRequest(
                username = "bob",
                password = "pass456",
                ip = "10.0.0.1",
            )

        val result = chain.handle(request)

        assertTrue(result.authenticated)
        assertNull(result.rejectedBy)
    }

    @Test
    fun should_rejectWithCredentialHandler_when_passwordIsWrong() {
        val chain = defaultChain()
        val request =
            AuthRequest(
                username = "alice",
                password = "wrongpassword",
                ip = "192.168.1.2",
            )

        val result = chain.handle(request)

        assertFalse(result.authenticated)
        assertEquals("CredentialHandler", result.rejectedBy)
        assertTrue(result.message.contains("Invalid username or password"))
    }

    @Test
    fun should_rejectWithRateLimitHandler_when_maxAttemptsExceeded() {
        val chain = defaultChain()
        val request =
            AuthRequest(
                username = "alice",
                password = "secret123",
                ip = "10.0.0.5",
                twoFactorCode = "123456",
            )

        // Exhaust the rate limit
        repeat(3) {
            val result = chain.handle(request)
            assertTrue(result.authenticated, "Attempt ${it + 1} should succeed")
        }

        // Fourth attempt should be rejected
        val result = chain.handle(request)
        assertFalse(result.authenticated)
        assertEquals("RateLimitHandler", result.rejectedBy)
        assertTrue(result.message.contains("Rate limit exceeded"))
    }

    @Test
    fun should_rejectWithTwoFactorHandler_when_codeIsMissing() {
        val chain = defaultChain()
        val request =
            AuthRequest(
                username = "alice",
                password = "secret123",
                ip = "172.16.0.1",
                twoFactorCode = null,
            )

        val result = chain.handle(request)

        assertFalse(result.authenticated)
        assertEquals("TwoFactorHandler", result.rejectedBy)
        assertTrue(result.message.contains("Two-factor authentication required"))
    }

    @Test
    fun should_rejectWithTwoFactorHandler_when_codeIsInvalid() {
        val chain = defaultChain()
        val request =
            AuthRequest(
                username = "alice",
                password = "secret123",
                ip = "172.16.0.2",
                twoFactorCode = "000000",
            )

        val result = chain.handle(request)

        assertFalse(result.authenticated)
        assertEquals("TwoFactorHandler", result.rejectedBy)
        assertTrue(result.message.contains("Invalid two-factor code"))
    }

    @Test
    fun should_respectChainOrder_when_rateLimitIsCheckedBeforeCredentials() {
        val rateLimitHandler = RateLimitHandler(maxAttempts = 1)
        val credentialHandler = CredentialHandler(credentials)

        // Rate limit first, then credentials
        val chain = buildAuthChain(rateLimitHandler, credentialHandler)

        val badCredentialsRequest =
            AuthRequest(
                username = "unknown",
                password = "wrong",
                ip = "10.0.0.99",
            )

        // First attempt: fails at credentials (rate limit not yet exceeded)
        val firstResult = chain.handle(badCredentialsRequest)
        assertEquals("CredentialHandler", firstResult.rejectedBy)

        // Second attempt: fails at rate limit (before even checking credentials)
        val secondResult = chain.handle(badCredentialsRequest)
        assertEquals("RateLimitHandler", secondResult.rejectedBy)
    }
}
