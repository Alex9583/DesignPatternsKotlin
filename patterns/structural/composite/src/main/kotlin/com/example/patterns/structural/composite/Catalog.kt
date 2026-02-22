package com.example.patterns.structural.composite

import java.math.BigDecimal

sealed interface CatalogComponent {
    val name: String

    fun price(): BigDecimal

    fun count(): Int
}

data class Product(
    override val name: String,
    val unitPrice: BigDecimal,
) : CatalogComponent {
    override fun price(): BigDecimal = unitPrice

    override fun count(): Int = 1
}

data class Category(
    override val name: String,
    val children: List<CatalogComponent> = emptyList(),
) : CatalogComponent {
    override fun price(): BigDecimal = children.fold(BigDecimal.ZERO) { acc, child -> acc + child.price() }

    override fun count(): Int = children.sumOf { it.count() }

    fun add(component: CatalogComponent): Category = copy(children = children + component)
}
