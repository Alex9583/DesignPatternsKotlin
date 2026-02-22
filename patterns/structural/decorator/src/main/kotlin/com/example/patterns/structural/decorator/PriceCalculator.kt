package com.example.patterns.structural.decorator

import java.math.BigDecimal
import java.math.RoundingMode

fun interface PriceCalculator {
    fun calculate(basePrice: BigDecimal): BigDecimal
}

class BasePriceCalculator : PriceCalculator {
    override fun calculate(basePrice: BigDecimal): BigDecimal = basePrice
}

class TaxDecorator(
    private val inner: PriceCalculator,
    private val taxRate: BigDecimal,
) : PriceCalculator {
    override fun calculate(basePrice: BigDecimal): BigDecimal {
        val subtotal = inner.calculate(basePrice)
        return subtotal + subtotal.multiply(taxRate)
    }
}

class DiscountDecorator(
    private val inner: PriceCalculator,
    private val discountPercent: BigDecimal,
) : PriceCalculator {
    override fun calculate(basePrice: BigDecimal): BigDecimal {
        val subtotal = inner.calculate(basePrice)
        val discount = subtotal.multiply(discountPercent).divide(BigDecimal(100))
        return subtotal - discount
    }
}

class FlatDiscountDecorator(
    private val inner: PriceCalculator,
    private val flatAmount: BigDecimal,
) : PriceCalculator {
    override fun calculate(basePrice: BigDecimal): BigDecimal {
        val subtotal = inner.calculate(basePrice)
        return subtotal - flatAmount
    }
}

class RoundingDecorator(
    private val inner: PriceCalculator,
    private val scale: Int = 2,
) : PriceCalculator {
    override fun calculate(basePrice: BigDecimal): BigDecimal = inner.calculate(basePrice).setScale(scale, RoundingMode.HALF_UP)
}
