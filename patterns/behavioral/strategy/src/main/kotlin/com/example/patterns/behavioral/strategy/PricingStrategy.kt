package com.example.patterns.behavioral.strategy

import java.math.BigDecimal

fun interface PricingStrategy {
    fun computePrice(
        basePrice: BigDecimal,
        quantity: Int,
    ): BigDecimal
}

object NoDiscount : PricingStrategy {
    override fun computePrice(
        basePrice: BigDecimal,
        quantity: Int,
    ): BigDecimal = basePrice * quantity.toBigDecimal()
}

class PercentageDiscount(
    val percent: BigDecimal,
) : PricingStrategy {
    override fun computePrice(
        basePrice: BigDecimal,
        quantity: Int,
    ): BigDecimal {
        val discountMultiplier = BigDecimal.ONE - percent.divide(BigDecimal("100"))
        return basePrice * quantity.toBigDecimal() * discountMultiplier
    }
}

class BulkDiscount(
    val threshold: Int,
    val percent: BigDecimal,
) : PricingStrategy {
    override fun computePrice(
        basePrice: BigDecimal,
        quantity: Int,
    ): BigDecimal {
        val total = basePrice * quantity.toBigDecimal()
        if (quantity < threshold) return total
        val discountMultiplier = BigDecimal.ONE - percent.divide(BigDecimal("100"))
        return total * discountMultiplier
    }
}

class TieredDiscount(
    val tiers: List<Pair<Int, BigDecimal>>,
) : PricingStrategy {
    private val sortedTiers = tiers.sortedByDescending { it.first }

    override fun computePrice(
        basePrice: BigDecimal,
        quantity: Int,
    ): BigDecimal {
        val total = basePrice * quantity.toBigDecimal()
        val matchedTier =
            sortedTiers.firstOrNull { quantity >= it.first }
                ?: return total
        val discountMultiplier = BigDecimal.ONE - matchedTier.second.divide(BigDecimal("100"))
        return total * discountMultiplier
    }
}

data class PricingContext(
    val productName: String,
    val basePrice: BigDecimal,
    val quantity: Int,
)

class PricingEngine(
    var strategy: PricingStrategy,
) {
    fun calculate(context: PricingContext): BigDecimal = strategy.computePrice(context.basePrice, context.quantity)
}
