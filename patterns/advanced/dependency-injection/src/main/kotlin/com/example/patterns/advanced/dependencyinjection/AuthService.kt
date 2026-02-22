package com.example.patterns.advanced.dependencyinjection

import kotlin.reflect.KClass

// --- Abstractions (les contrats dont dépend le code métier) ---

interface UserRepository {
    fun findByUsername(username: String): UserRecord?
}

interface PasswordHasher {
    fun hash(raw: String): String

    fun verify(
        raw: String,
        hashed: String,
    ): Boolean
}

interface TokenGenerator {
    fun generate(userId: String): String

    fun validate(token: String): String?
}

// --- Domain models ---

data class UserRecord(
    val id: String,
    val username: String,
    val hashedPassword: String,
)

data class AuthResult(
    val success: Boolean,
    val token: String? = null,
    val error: String? = null,
)

// --- Service (reçoit ses dépendances par constructor injection) ---

class AuthenticationService(
    private val userRepo: UserRepository,
    private val hasher: PasswordHasher,
    private val tokenGen: TokenGenerator,
) {
    fun login(
        username: String,
        password: String,
    ): AuthResult {
        val user =
            userRepo.findByUsername(username)
                ?: return AuthResult(success = false, error = "Unknown user: $username")

        if (!hasher.verify(password, user.hashedPassword)) {
            return AuthResult(success = false, error = "Invalid password")
        }

        val token = tokenGen.generate(user.id)
        return AuthResult(success = true, token = token)
    }
}

// --- Implémentations concrètes ---

class InMemoryUserRepository(
    private val users: MutableMap<String, UserRecord> = mutableMapOf(),
) : UserRepository {
    fun addUser(user: UserRecord) {
        users[user.username] = user
    }

    override fun findByUsername(username: String): UserRecord? = users[username]
}

class SimplePasswordHasher : PasswordHasher {
    private val prefix = "hashed:"

    override fun hash(raw: String): String = "$prefix$raw"

    override fun verify(
        raw: String,
        hashed: String,
    ): Boolean = hashed == "$prefix$raw"
}

class IncrementalTokenGenerator : TokenGenerator {
    private var counter = 0L

    override fun generate(userId: String): String = "token-$userId-${++counter}"

    override fun validate(token: String): String? {
        if (!token.startsWith("token-")) return null
        val withoutPrefix = token.removePrefix("token-")
        val lastDash = withoutPrefix.lastIndexOf('-')
        if (lastDash <= 0) return null
        return withoutPrefix.substring(0, lastDash)
    }
}

// --- DI Container (composition root) ---

class Container {
    private sealed interface Binding {
        fun resolve(): Any
    }

    private class SingletonBinding(
        create: () -> Any,
    ) : Binding {
        private val instance by lazy(create)

        override fun resolve(): Any = instance
    }

    private class FactoryBinding(
        private val create: () -> Any,
    ) : Binding {
        override fun resolve(): Any = create()
    }

    private val bindings = mutableMapOf<KClass<*>, Binding>()

    @PublishedApi
    internal fun <T : Any> registerSingleton(
        type: KClass<T>,
        create: (Container) -> T,
    ) {
        bindings[type] = SingletonBinding { create(this) }
    }

    @PublishedApi
    internal fun <T : Any> registerFactory(
        type: KClass<T>,
        create: (Container) -> T,
    ) {
        bindings[type] = FactoryBinding { create(this) }
    }

    @PublishedApi
    internal fun <T : Any> resolveBinding(type: KClass<T>): T {
        val binding =
            bindings[type]
                ?: throw IllegalStateException("No binding found for ${type.simpleName}")
        @Suppress("UNCHECKED_CAST")
        return binding.resolve() as T
    }

    inline fun <reified T : Any> singleton(noinline create: (Container) -> T) {
        registerSingleton(T::class, create)
    }

    inline fun <reified T : Any> factory(noinline create: (Container) -> T) {
        registerFactory(T::class, create)
    }

    inline fun <reified T : Any> resolve(): T = resolveBinding(T::class)
}
