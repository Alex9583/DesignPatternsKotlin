package com.example.patterns.behavioral.memento

import java.math.BigDecimal
import java.math.RoundingMode

class PriceQuote(
    productName: String,
    unitPrice: BigDecimal,
    quantity: Int,
    discount: BigDecimal = BigDecimal.ZERO,
) {
    var productName: String = productName
        set(value) {
            require(value.isNotBlank()) { "Product name must not be blank" }
            field = value
        }

    var unitPrice: BigDecimal = unitPrice
        set(value) {
            require(value >= BigDecimal.ZERO) { "Unit price must be non-negative, got $value" }
            field = value
        }

    var quantity: Int = quantity
        set(value) {
            require(value > 0) { "Quantity must be positive, got $value" }
            field = value
        }

    var discount: BigDecimal = discount
        set(value) {
            require(value >= BigDecimal.ZERO && value <= BigDecimal("100")) {
                "Discount must be between 0 and 100, got $value"
            }
            field = value
        }

    init {
        require(productName.isNotBlank()) { "Product name must not be blank" }
        require(unitPrice >= BigDecimal.ZERO) { "Unit price must be non-negative, got $unitPrice" }
        require(quantity > 0) { "Quantity must be positive, got $quantity" }
        require(discount >= BigDecimal.ZERO && discount <= BigDecimal("100")) {
            "Discount must be between 0 and 100, got $discount"
        }
    }

    fun totalPrice(): BigDecimal {
        val gross = unitPrice * quantity.toBigDecimal()
        val multiplier = BigDecimal.ONE - discount.divide(BigDecimal("100"))
        return (gross * multiplier).setScale(2, RoundingMode.HALF_UP)
    }

    fun save(): Memento =
        Memento(
            productName = productName,
            unitPrice = unitPrice,
            quantity = quantity,
            discount = discount,
        )

    fun restore(memento: Memento) {
        productName = memento.productName
        unitPrice = memento.unitPrice
        quantity = memento.quantity
        discount = memento.discount
    }

    data class Memento(
        val productName: String,
        val unitPrice: BigDecimal,
        val quantity: Int,
        val discount: BigDecimal,
    )
}

class QuoteHistory {
    private val snapshots = ArrayDeque<PriceQuote.Memento>()

    fun push(memento: PriceQuote.Memento) {
        snapshots.addLast(memento)
    }

    fun pop(): PriceQuote.Memento? = if (snapshots.isEmpty()) null else snapshots.removeLast()

    fun peek(): PriceQuote.Memento? = snapshots.lastOrNull()

    val size: Int get() = snapshots.size
}
