package com.example.patterns.advanced.dependencyinjection

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AuthServiceTest {
    private val hasher = SimplePasswordHasher()

    private fun setupService(): Pair<AuthenticationService, InMemoryUserRepository> {
        val repo = InMemoryUserRepository()
        val tokenGen = IncrementalTokenGenerator()
        repo.addUser(UserRecord("u1", "alice", hasher.hash("s3cret")))
        repo.addUser(UserRecord("u2", "bob", hasher.hash("pa55word")))
        val service = AuthenticationService(repo, hasher, tokenGen)
        return service to repo
    }

    @Test
    fun should_returnToken_when_credentialsAreValid() {
        val (service, _) = setupService()

        val result = service.login("alice", "s3cret")

        assertTrue(result.success)
        assertNotNull(result.token)
        assertTrue(result.token.contains("u1"))
    }

    @Test
    fun should_returnError_when_passwordIsWrong() {
        val (service, _) = setupService()

        val result = service.login("alice", "wrongpass")

        assertEquals(false, result.success)
        assertEquals("Invalid password", result.error)
        assertNull(result.token)
    }

    @Test
    fun should_returnError_when_userNotFound() {
        val (service, _) = setupService()

        val result = service.login("unknown", "any")

        assertEquals(false, result.success)
        assertEquals("Unknown user: unknown", result.error)
    }

    @Test
    fun should_allowSwappingImplementations_when_testDoublesUsed() {
        val fakeRepo =
            object : UserRepository {
                override fun findByUsername(username: String): UserRecord? =
                    if (username == "test") UserRecord("t1", "test", "fixed") else null
            }
        val fakeHasher =
            object : PasswordHasher {
                override fun hash(raw: String): String = "fixed"

                override fun verify(
                    raw: String,
                    hashed: String,
                ): Boolean = raw == "pass" && hashed == "fixed"
            }
        val fakeTokenGen =
            object : TokenGenerator {
                override fun generate(userId: String): String = "fake-token"

                override fun validate(token: String): String? = "t1"
            }

        val service = AuthenticationService(fakeRepo, fakeHasher, fakeTokenGen)
        val result = service.login("test", "pass")

        assertTrue(result.success)
        assertEquals("fake-token", result.token)
    }

    @Test
    fun should_resolveFullServiceGraph_when_containerConfigured() {
        val container = Container()
        val repo = InMemoryUserRepository()
        repo.addUser(UserRecord("u1", "admin", hasher.hash("admin123")))

        container.singleton<UserRepository> { _ -> repo }
        container.singleton<PasswordHasher> { _ -> SimplePasswordHasher() }
        container.singleton<TokenGenerator> { _ -> IncrementalTokenGenerator() }
        container.singleton<AuthenticationService> { c ->
            AuthenticationService(c.resolve(), c.resolve(), c.resolve())
        }

        val service = container.resolve<AuthenticationService>()
        val result = service.login("admin", "admin123")

        assertTrue(result.success)
        assertNotNull(result.token)
    }

    @Test
    fun should_returnSameInstance_when_singletonBinding() {
        val container = Container()
        container.singleton<PasswordHasher> { _ -> SimplePasswordHasher() }

        val first = container.resolve<PasswordHasher>()
        val second = container.resolve<PasswordHasher>()

        assertSame(first, second)
    }

    @Test
    fun should_returnNewInstance_when_factoryBinding() {
        val container = Container()
        container.factory<PasswordHasher> { _ -> SimplePasswordHasher() }

        val first = container.resolve<PasswordHasher>()
        val second = container.resolve<PasswordHasher>()

        assertTrue(first !== second)
    }

    @Test
    fun should_throwException_when_bindingNotFound() {
        val container = Container()

        assertFailsWith<IllegalStateException> {
            container.resolve<UserRepository>()
        }
    }
}
