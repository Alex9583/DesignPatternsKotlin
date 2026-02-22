package com.example.patterns.behavioral.strategy

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class PricingStrategyTest {
    private val unitPrice = BigDecimal("100.00")

    @Test
    fun should_returnFullPrice_when_noDiscountApplied() {
        val engine = PricingEngine(NoDiscount)
        val context = PricingContext("Wireless Mouse", unitPrice, 3)

        val result = engine.calculate(context)

        assertEquals(BigDecimal("300.00"), result.setScale(2))
    }

    @Test
    fun should_applyPercentageDiscount_when_percentageStrategyUsed() {
        val engine = PricingEngine(PercentageDiscount(BigDecimal("10")))
        val context = PricingContext("Mechanical Keyboard", unitPrice, 2)

        val result = engine.calculate(context)

        // 100 * 2 = 200, 10 % off = 180
        assertEquals(BigDecimal("180.00"), result.setScale(2))
    }

    @Test
    fun should_returnFullPrice_when_bulkThresholdNotReached() {
        val engine = PricingEngine(BulkDiscount(threshold = 10, percent = BigDecimal("15")))
        val context = PricingContext("USB-C Cable", unitPrice, 5)

        val result = engine.calculate(context)

        // quantity 5 < threshold 10 → no discount
        assertEquals(BigDecimal("500.00"), result.setScale(2))
    }

    @Test
    fun should_applyBulkDiscount_when_thresholdReached() {
        val engine = PricingEngine(BulkDiscount(threshold = 10, percent = BigDecimal("15")))
        val context = PricingContext("USB-C Cable", unitPrice, 10)

        val result = engine.calculate(context)

        // 100 * 10 = 1000, 15 % off = 850
        assertEquals(BigDecimal("850.00"), result.setScale(2))
    }

    @Test
    fun should_swapStrategyAtRuntime_when_businessRulesChange() {
        val engine = PricingEngine(NoDiscount)
        val context = PricingContext("Monitor Stand", unitPrice, 4)

        val fullPrice = engine.calculate(context)
        assertEquals(BigDecimal("400.00"), fullPrice.setScale(2))

        // Black Friday kicks in — swap to 20 % discount
        engine.strategy = PercentageDiscount(BigDecimal("20"))
        val discountedPrice = engine.calculate(context)

        assertEquals(BigDecimal("320.00"), discountedPrice.setScale(2))
    }

    @Test
    fun should_matchHighestTier_when_tieredDiscountUsed() {
        val tiers =
            listOf(
                5 to BigDecimal("5"),
                20 to BigDecimal("15"),
                50 to BigDecimal("25"),
            )
        val engine = PricingEngine(TieredDiscount(tiers))
        val context = PricingContext("Webcam", unitPrice, 25)

        val result = engine.calculate(context)

        // quantity 25 >= 20 (but < 50), so 15 % discount
        // 100 * 25 = 2500, 15 % off = 2125
        assertEquals(BigDecimal("2125.00"), result.setScale(2))
    }
}
