package com.example.patterns.behavioral.visitor

import java.math.BigDecimal
import java.math.RoundingMode

sealed class CartItem {
    abstract val name: String
    abstract val price: BigDecimal

    abstract fun <T> accept(visitor: CartVisitor<T>): T
}

data class PhysicalProduct(
    override val name: String,
    override val price: BigDecimal,
    val weightKg: Double,
) : CartItem() {
    override fun <T> accept(visitor: CartVisitor<T>): T = visitor.visit(this)
}

data class DigitalProduct(
    override val name: String,
    override val price: BigDecimal,
    val downloadUrl: String,
) : CartItem() {
    override fun <T> accept(visitor: CartVisitor<T>): T = visitor.visit(this)
}

data class Subscription(
    override val name: String,
    override val price: BigDecimal,
    val months: Int,
) : CartItem() {
    override fun <T> accept(visitor: CartVisitor<T>): T = visitor.visit(this)
}

interface CartVisitor<T> {
    fun visit(item: PhysicalProduct): T

    fun visit(item: DigitalProduct): T

    fun visit(item: Subscription): T
}

class PriceCalculatorVisitor(
    val taxRate: BigDecimal,
) : CartVisitor<BigDecimal> {
    override fun visit(item: PhysicalProduct): BigDecimal {
        val tax = item.price * taxRate
        val shipping = BigDecimal.valueOf(item.weightKg * 2)
        return (item.price + tax + shipping).setScale(2, RoundingMode.HALF_UP)
    }

    override fun visit(item: DigitalProduct): BigDecimal {
        val tax = item.price * taxRate
        return (item.price + tax).setScale(2, RoundingMode.HALF_UP)
    }

    override fun visit(item: Subscription): BigDecimal = (item.price * item.months.toBigDecimal()).setScale(2, RoundingMode.HALF_UP)
}

class SummaryVisitor : CartVisitor<String> {
    override fun visit(item: PhysicalProduct): String = "${item.name} (physical, ${item.weightKg} kg) — ${item.price}"

    override fun visit(item: DigitalProduct): String = "${item.name} (digital, ${item.downloadUrl}) — ${item.price}"

    override fun visit(item: Subscription): String = "${item.name} (subscription, ${item.months} months) — ${item.price}/month"
}

fun List<CartItem>.totalPrice(visitor: PriceCalculatorVisitor): BigDecimal =
    fold(BigDecimal.ZERO) { acc, item -> acc + item.accept(visitor) }
        .setScale(2, RoundingMode.HALF_UP)
