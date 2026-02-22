package com.example.patterns.creational.singleton

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AppConfigTest {
    @AfterTest
    fun tearDown() {
        AppConfig.clear()
    }

    @Test
    fun should_returnSameInstance_when_referencedMultipleTimes() {
        val ref1 = AppConfig
        val ref2 = AppConfig
        assertSame(ref1, ref2)
    }

    @Test
    fun should_storeAndRetrieveValues_when_settingConfig() {
        AppConfig.set(ConfigKey.DATABASE_URL, "jdbc:postgresql://localhost:5432/shop")
        AppConfig.set(ConfigKey.MAX_CONNECTIONS, "10")

        assertEquals("jdbc:postgresql://localhost:5432/shop", AppConfig.get(ConfigKey.DATABASE_URL))
        assertEquals("10", AppConfig.get(ConfigKey.MAX_CONNECTIONS))
    }

    @Test
    fun should_returnNull_when_keyNotSet() {
        assertNull(AppConfig.get(ConfigKey.API_KEY))
    }

    @Test
    fun should_returnDefault_when_keyAbsent() {
        val ttl = AppConfig.getOrDefault(ConfigKey.CACHE_TTL_SECONDS, "300")
        assertEquals("300", ttl)
    }

    @Test
    fun should_shareStateBetweenCallers_when_modifiedFromAnywhere() {
        fun serviceA() = AppConfig.set(ConfigKey.API_KEY, "secret-key-123")

        fun serviceB() = AppConfig.get(ConfigKey.API_KEY)

        serviceA()
        assertEquals("secret-key-123", serviceB())
    }

    @Test
    fun should_returnImmutableSnapshot_when_snapshotCalled() {
        AppConfig.set(ConfigKey.DATABASE_URL, "jdbc:h2:mem:test")
        val snapshot = AppConfig.snapshot()

        AppConfig.set(ConfigKey.DATABASE_URL, "jdbc:postgresql://prod:5432/db")

        assertEquals("jdbc:h2:mem:test", snapshot[ConfigKey.DATABASE_URL])
        assertEquals("jdbc:postgresql://prod:5432/db", AppConfig.get(ConfigKey.DATABASE_URL))
    }

    @Test
    fun should_beEmpty_when_cleared() {
        AppConfig.set(ConfigKey.API_KEY, "key")
        AppConfig.clear()

        assertTrue(AppConfig.snapshot().isEmpty())
    }
}
