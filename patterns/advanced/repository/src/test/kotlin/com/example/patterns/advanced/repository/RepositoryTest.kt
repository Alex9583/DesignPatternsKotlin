package com.example.patterns.advanced.repository

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RepositoryTest {
    private fun createRepository(): InMemoryProductRepository = InMemoryProductRepository()

    @Test
    fun should_persistAndRetrieveProduct_when_saved() {
        val repo = createRepository()
        val product = Product("p1", "Wireless Mouse", BigDecimal("29.99"), "peripherals")

        repo.save(product)

        assertEquals(product, repo.findById("p1"))
        assertTrue(repo.existsById("p1"))
    }

    @Test
    fun should_returnMatchingProducts_when_filteringByCategory() {
        val repo = createRepository()
        repo.save(Product("p1", "Keyboard", BigDecimal("49.99"), "peripherals"))
        repo.save(Product("p2", "Monitor", BigDecimal("299.99"), "displays"))
        repo.save(Product("p3", "Mouse", BigDecimal("19.99"), "peripherals"))

        val peripherals = repo.findByCategory("peripherals")

        assertEquals(2, peripherals.size)
        assertTrue(peripherals.all { it.category == "peripherals" })
    }

    @Test
    fun should_removeProduct_when_deleted() {
        val repo = createRepository()
        repo.save(Product("p1", "USB Cable", BigDecimal("9.99"), "accessories"))

        assertTrue(repo.delete("p1"))
        assertNull(repo.findById("p1"))
        assertFalse(repo.delete("p1"), "Second delete should return false")
    }

    @Test
    fun should_returnProductsInRange_when_filteringByPrice() {
        val repo = createRepository()
        repo.save(Product("p1", "Budget Mouse", BigDecimal("9.99"), "peripherals"))
        repo.save(Product("p2", "Pro Keyboard", BigDecimal("149.99"), "peripherals"))
        repo.save(Product("p3", "Mid Monitor", BigDecimal("199.99"), "displays"))
        repo.save(Product("p4", "Premium Monitor", BigDecimal("599.99"), "displays"))

        val midRange = repo.findByPriceRange(BigDecimal("100.00"), BigDecimal("200.00"))

        assertEquals(2, midRange.size)
        assertTrue(midRange.any { it.id == "p2" })
        assertTrue(midRange.any { it.id == "p3" })
    }

    @Test
    fun should_returnEmptyList_when_repositoryIsEmpty() {
        val repo = createRepository()

        assertTrue(repo.findAll().isEmpty())
        assertTrue(repo.findByCategory("anything").isEmpty())
        assertNull(repo.findById("nonexistent"))
    }
}
