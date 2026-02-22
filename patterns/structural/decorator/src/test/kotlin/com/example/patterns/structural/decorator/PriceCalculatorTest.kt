package com.example.patterns.structural.decorator

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.test.Test
import kotlin.test.assertEquals

class PriceCalculatorTest {
    @Test
    fun should_returnBasePriceUnchanged_when_noDecoratorApplied() {
        val calculator = BasePriceCalculator()

        val result = calculator.calculate(BigDecimal("100.00"))

        assertEquals(BigDecimal("100.00"), result)
    }

    @Test
    fun should_addTaxCorrectly_when_appliedOnBasePrice() {
        val calculator =
            TaxDecorator(
                inner = BasePriceCalculator(),
                taxRate = BigDecimal("0.20"),
            )

        val result = calculator.calculate(BigDecimal("100.00"))

        assertEquals(0, BigDecimal("120.00").compareTo(result))
    }

    @Test
    fun should_produceDifferentResults_when_flatDiscountAndTaxAppliedInDifferentOrder() {
        val base = BasePriceCalculator()

        // Flat discount (20 EUR) first, then 20% tax
        val discountThenTax =
            RoundingDecorator(
                TaxDecorator(
                    FlatDiscountDecorator(base, BigDecimal("20")),
                    BigDecimal("0.20"),
                ),
            )

        // 20% tax first, then flat discount (20 EUR)
        val taxThenDiscount =
            RoundingDecorator(
                FlatDiscountDecorator(
                    TaxDecorator(base, BigDecimal("0.20")),
                    BigDecimal("20"),
                ),
            )

        val price = BigDecimal("200.00")
        val resultA = discountThenTax.calculate(price)
        val resultB = taxThenDiscount.calculate(price)

        // Discount then tax: (200 - 20) * 1.20 = 216.00
        assertEquals(BigDecimal("216.00"), resultA)
        // Tax then discount: (200 * 1.20) - 20 = 220.00
        assertEquals(BigDecimal("220.00"), resultB)

        // With a non-multiplicative decorator, the order truly matters.
    }

    @Test
    fun should_stackMultipleDecorators_when_discountAndTaxCombined() {
        val calculator =
            RoundingDecorator(
                TaxDecorator(
                    DiscountDecorator(
                        BasePriceCalculator(),
                        BigDecimal("15"),
                    ),
                    BigDecimal("0.21"),
                ),
            )

        // 100 - 15% = 85, + 21% tax = 102.85
        val result = calculator.calculate(BigDecimal("100.00"))

        assertEquals(BigDecimal("102.85"), result)
    }

    @Test
    fun should_handleZeroBasePrice_when_decoratorsApplied() {
        val calculator =
            RoundingDecorator(
                TaxDecorator(
                    DiscountDecorator(
                        BasePriceCalculator(),
                        BigDecimal("50"),
                    ),
                    BigDecimal("0.20"),
                ),
            )

        val result = calculator.calculate(BigDecimal.ZERO)

        assertEquals(0, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP).compareTo(result))
    }
}
