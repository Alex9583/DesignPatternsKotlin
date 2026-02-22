package com.example.patterns.behavioral.interpreter

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PricingRuleTest {
    private val context =
        RuleContext(
            quantity = 15,
            customerTier = "GOLD",
            basePrice = BigDecimal("100.00"),
        )

    @Test
    fun should_evaluateLiteral_when_simpleValueProvided() {
        val expr = Literal(BigDecimal("42.50"))

        assertEquals(BigDecimal("42.50"), evaluate(expr, context))
    }

    @Test
    fun should_evaluateArithmetic_when_multiplyAndAddUsed() {
        // basePrice * quantity = 100 * 15 = 1500
        val expr = Multiply(BasePriceRef, QuantityRef)

        assertEquals(BigDecimal("1500.00"), evaluate(expr, context))
    }

    @Test
    fun should_resolveQuantityRef_when_contextQueried() {
        val expr = QuantityRef

        assertEquals(BigDecimal("15"), evaluate(expr, context))
    }

    @Test
    fun should_evaluateCondition_when_greaterThanUsed() {
        val cond = GreaterThan(QuantityRef, Literal(BigDecimal("10")))

        assertTrue(evaluateCondition(cond, context))

        val smallContext = context.copy(quantity = 5)
        assertFalse(evaluateCondition(cond, smallContext))
    }

    @Test
    fun should_evaluateIfThenElse_when_tierMatchesGold() {
        // if tier == "GOLD" then basePrice * 0.9 else basePrice * 1.0
        val rule =
            IfThenElse(
                condition = Equals("GOLD", "tier"),
                then = Multiply(BasePriceRef, Literal(BigDecimal("0.90"))),
                otherwise = Multiply(BasePriceRef, Literal(BigDecimal("1.00"))),
            )

        assertEquals(BigDecimal("90.00"), evaluate(rule, context))

        val silverContext = context.copy(customerTier = "SILVER")
        assertEquals(BigDecimal("100.00"), evaluate(rule, silverContext))
    }

    @Test
    fun should_evaluateComplexNestedRule_when_quantityAndTierCombined() {
        // if quantity > 10 then basePrice * quantity * 0.85
        // else basePrice * quantity
        val rule =
            IfThenElse(
                condition = GreaterThan(QuantityRef, Literal(BigDecimal("10"))),
                then =
                    Multiply(
                        Multiply(BasePriceRef, QuantityRef),
                        Literal(BigDecimal("0.85")),
                    ),
                otherwise = Multiply(BasePriceRef, QuantityRef),
            )

        // quantity=15 > 10, so 100 * 15 * 0.85 = 1275.00
        assertEquals(BigDecimal("1275.00"), evaluate(rule, context))

        val smallOrder = context.copy(quantity = 5)
        // quantity=5 <= 10, so 100 * 5 = 500.00
        assertEquals(BigDecimal("500.00"), evaluate(rule, smallOrder))
    }

    @Test
    fun should_evaluateSubtraction_when_subtractUsed() {
        val expr =
            Subtract(
                Multiply(BasePriceRef, QuantityRef),
                Literal(BigDecimal("50.00")),
            )

        // 100 * 15 - 50 = 1450
        assertEquals(BigDecimal("1450.00"), evaluate(expr, context))
    }
}
