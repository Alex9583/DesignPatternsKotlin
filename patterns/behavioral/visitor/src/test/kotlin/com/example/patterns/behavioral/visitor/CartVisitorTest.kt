package com.example.patterns.behavioral.visitor

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CartVisitorTest {
    private val taxRate = BigDecimal("0.20") // 20 %
    private val priceVisitor = PriceCalculatorVisitor(taxRate)

    @Test
    fun should_includeTaxAndShipping_when_physicalProductVisited() {
        val product = PhysicalProduct("Desk Lamp", BigDecimal("40.00"), weightKg = 1.5)

        // price 40 + tax 8 + shipping (1.5 * 2 = 3) = 51.00
        val result = product.accept(priceVisitor)

        assertEquals(BigDecimal("51.00"), result)
    }

    @Test
    fun should_includeTaxOnly_when_digitalProductVisited() {
        val ebook =
            DigitalProduct(
                "Kotlin in Action",
                BigDecimal("29.99"),
                "https://shop.example.com/kotlin.pdf",
            )

        // 29.99 + 20 % = 35.988 → 35.99
        val result = ebook.accept(priceVisitor)

        assertEquals(BigDecimal("35.99"), result)
    }

    @Test
    fun should_multiplyByMonths_when_subscriptionVisited() {
        val sub = Subscription("Cloud Storage Pro", BigDecimal("9.99"), months = 12)

        // 9.99 × 12 = 119.88
        val result = sub.accept(priceVisitor)

        assertEquals(BigDecimal("119.88"), result)
    }

    @Test
    fun should_sumAllItems_when_mixedCartCalculated() {
        val cart =
            listOf(
                PhysicalProduct("Wireless Mouse", BigDecimal("25.00"), weightKg = 0.3),
                DigitalProduct("IDE License", BigDecimal("199.00"), "https://shop.example.com/ide"),
                Subscription("CI/CD Pipeline", BigDecimal("49.00"), months = 6),
            )

        // Mouse:  25 + 5 + 0.6 = 30.60
        // IDE:    199 + 39.80  = 238.80
        // CI/CD:  49 × 6      = 294.00
        // Total: 563.40
        val total = cart.totalPrice(priceVisitor)

        assertEquals(BigDecimal("563.40"), total)
    }

    @Test
    fun should_returnReadableSummary_when_summaryVisitorUsed() {
        val summary = SummaryVisitor()
        val product = PhysicalProduct("Standing Desk", BigDecimal("450.00"), weightKg = 25.0)

        val result = product.accept(summary)

        assertTrue(result.contains("Standing Desk"))
        assertTrue(result.contains("physical"))
        assertTrue(result.contains("25.0 kg"))
    }

    @Test
    fun should_supportNewVisitor_when_addedExternally() {
        val monthCounter =
            object : CartVisitor<Int> {
                override fun visit(item: PhysicalProduct): Int = 0

                override fun visit(item: DigitalProduct): Int = 0

                override fun visit(item: Subscription): Int = item.months
            }

        val items: List<CartItem> =
            listOf(
                PhysicalProduct("Headphones", BigDecimal("60.00"), 0.2),
                Subscription("Music Streaming", BigDecimal("9.99"), months = 3),
            )

        val totalMonths = items.sumOf { it.accept(monthCounter) }
        assertEquals(3, totalMonths)
    }

    @Test
    fun should_returnZero_when_cartIsEmpty() {
        val empty = emptyList<CartItem>()

        assertEquals(BigDecimal("0.00"), empty.totalPrice(priceVisitor))
    }
}
