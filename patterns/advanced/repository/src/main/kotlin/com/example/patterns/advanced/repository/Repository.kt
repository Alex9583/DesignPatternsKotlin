package com.example.patterns.advanced.repository

import java.math.BigDecimal

interface Repository<T, ID> {
    fun findById(id: ID): T?

    fun findAll(): List<T>

    fun save(entity: T): T

    fun delete(id: ID): Boolean

    fun existsById(id: ID): Boolean
}

data class Product(
    val id: String,
    val name: String,
    val price: BigDecimal,
    val category: String,
)

class InMemoryProductRepository : Repository<Product, String> {
    private val store: MutableMap<String, Product> = mutableMapOf()

    override fun findById(id: String): Product? = store[id]

    override fun findAll(): List<Product> = store.values.toList()

    override fun save(entity: Product): Product {
        store[entity.id] = entity
        return entity
    }

    override fun delete(id: String): Boolean = store.remove(id) != null

    override fun existsById(id: String): Boolean = store.containsKey(id)

    fun findByCategory(category: String): List<Product> = store.values.filter { it.category == category }

    fun findByPriceRange(
        min: BigDecimal,
        max: BigDecimal,
    ): List<Product> = store.values.filter { it.price in min..max }
}
