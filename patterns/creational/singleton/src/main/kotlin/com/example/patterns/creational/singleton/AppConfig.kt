package com.example.patterns.creational.singleton

enum class ConfigKey {
    DATABASE_URL,
    API_KEY,
    MAX_CONNECTIONS,
    CACHE_TTL_SECONDS,
}

object AppConfig {
    private val properties: MutableMap<ConfigKey, String> = mutableMapOf()

    fun get(key: ConfigKey): String? = properties[key]

    fun set(
        key: ConfigKey,
        value: String,
    ) {
        properties[key] = value
    }

    fun getOrDefault(
        key: ConfigKey,
        default: String,
    ): String = properties.getOrDefault(key, default)

    fun contains(key: ConfigKey): Boolean = key in properties

    fun clear() {
        properties.clear()
    }

    fun snapshot(): Map<ConfigKey, String> = properties.toMap()
}
