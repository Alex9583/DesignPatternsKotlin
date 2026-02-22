package com.example.patterns.behavioral.chainofresponsibility

data class AuthRequest(
    val username: String,
    val password: String,
    val ip: String,
    val twoFactorCode: String? = null,
)

data class AuthResult(
    val authenticated: Boolean,
    val rejectedBy: String? = null,
    val message: String = "",
)

abstract class AuthHandler {
    var next: AuthHandler? = null

    abstract fun handle(request: AuthRequest): AuthResult

    protected fun passToNext(request: AuthRequest): AuthResult =
        next?.handle(request)
            ?: AuthResult(authenticated = true, message = "All checks passed")
}

class RateLimitHandler(
    private val maxAttempts: Int,
) : AuthHandler() {
    private val attemptsByIp = mutableMapOf<String, Int>()

    override fun handle(request: AuthRequest): AuthResult {
        val attempts = attemptsByIp.getOrDefault(request.ip, 0) + 1
        attemptsByIp[request.ip] = attempts

        if (attempts > maxAttempts) {
            return AuthResult(
                authenticated = false,
                rejectedBy = "RateLimitHandler",
                message = "Rate limit exceeded for IP ${request.ip}: $attempts attempts (max $maxAttempts)",
            )
        }

        return passToNext(request)
    }

    fun resetAttempts(ip: String) {
        attemptsByIp.remove(ip)
    }
}

class CredentialHandler(
    private val validCredentials: Map<String, String>,
) : AuthHandler() {
    override fun handle(request: AuthRequest): AuthResult {
        val expectedPassword = validCredentials[request.username]

        if (expectedPassword == null || expectedPassword != request.password) {
            return AuthResult(
                authenticated = false,
                rejectedBy = "CredentialHandler",
                message = "Invalid username or password for '${request.username}'",
            )
        }

        return passToNext(request)
    }
}

class TwoFactorHandler(
    private val requiredUsers: Set<String>,
    private val validCodes: Map<String, String>,
) : AuthHandler() {
    override fun handle(request: AuthRequest): AuthResult {
        if (request.username !in requiredUsers) {
            return passToNext(request)
        }

        if (request.twoFactorCode == null) {
            return AuthResult(
                authenticated = false,
                rejectedBy = "TwoFactorHandler",
                message = "Two-factor authentication required for '${request.username}'",
            )
        }

        val expectedCode = validCodes[request.username]
        if (request.twoFactorCode != expectedCode) {
            return AuthResult(
                authenticated = false,
                rejectedBy = "TwoFactorHandler",
                message = "Invalid two-factor code for '${request.username}'",
            )
        }

        return passToNext(request)
    }
}

fun buildAuthChain(vararg handlers: AuthHandler): AuthHandler {
    require(handlers.isNotEmpty()) { "At least one handler is required to build a chain" }
    handlers.toList().zipWithNext().forEach { (current, next) ->
        current.next = next
    }
    return handlers.first()
}
