package com.example.patterns.structural.composite

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class CatalogTest {
    @Test
    fun should_returnProductPrice_when_componentIsASingleProduct() {
        val product = Product("Wireless Mouse", BigDecimal("29.99"))

        assertEquals(BigDecimal("29.99"), product.price())
        assertEquals(1, product.count())
    }

    @Test
    fun should_sumChildrenPrices_when_categoryContainsProducts() {
        val category =
            Category("Peripherals")
                .add(Product("Keyboard", BigDecimal("49.99")))
                .add(Product("Mouse", BigDecimal("29.99")))
                .add(Product("Webcam", BigDecimal("79.99")))

        assertEquals(BigDecimal("159.97"), category.price())
        assertEquals(3, category.count())
    }

    @Test
    fun should_computePriceRecursively_when_categoriesAreDeeplyNested() {
        val cpus =
            Category("CPUs")
                .add(Product("Ryzen 7", BigDecimal("329.00")))
                .add(Product("Core i7", BigDecimal("399.00")))

        val gpus =
            Category("GPUs")
                .add(Product("RTX 4070", BigDecimal("599.00")))

        val components =
            Category("Components")
                .add(cpus)
                .add(gpus)

        val root =
            Category("Store")
                .add(components)
                .add(Product("USB Cable", BigDecimal("9.99")))

        assertEquals(BigDecimal("1336.99"), root.price())
        assertEquals(4, root.count())
    }

    @Test
    fun should_notMutateOriginalCategory_when_addIsCalled() {
        val original = Category("Books")
        val updated = original.add(Product("Clean Code", BigDecimal("39.99")))

        assertEquals(0, original.count())
        assertEquals(BigDecimal.ZERO, original.price())

        assertEquals(1, updated.count())
        assertEquals(BigDecimal("39.99"), updated.price())
    }

    @Test
    fun should_treatProductsAndCategoriesUniformly_when_iteratingComponents() {
        val items: List<CatalogComponent> =
            listOf(
                Product("Sticker Pack", BigDecimal("4.99")),
                Category("Bundles")
                    .add(Product("T-Shirt", BigDecimal("19.99")))
                    .add(Product("Mug", BigDecimal("12.99"))),
            )

        val totalPrice = items.fold(BigDecimal.ZERO) { acc, c -> acc + c.price() }
        val totalCount = items.sumOf { it.count() }

        assertEquals(BigDecimal("37.97"), totalPrice)
        assertEquals(3, totalCount)
    }

    @Test
    fun should_returnZeroPriceAndCount_when_categoryIsEmpty() {
        val empty = Category("Empty Section")

        assertEquals(BigDecimal.ZERO, empty.price())
        assertEquals(0, empty.count())
    }
}
