package com.example.patterns.behavioral.interpreter

import java.math.BigDecimal
import java.math.RoundingMode

data class RuleContext(
    val quantity: Int,
    val customerTier: String,
    val basePrice: BigDecimal,
)

sealed interface Expression

data class Literal(
    val value: BigDecimal,
) : Expression

data object QuantityRef : Expression

data object BasePriceRef : Expression

data class Multiply(
    val left: Expression,
    val right: Expression,
) : Expression

data class Add(
    val left: Expression,
    val right: Expression,
) : Expression

data class Subtract(
    val left: Expression,
    val right: Expression,
) : Expression

data class IfThenElse(
    val condition: Condition,
    val then: Expression,
    val otherwise: Expression,
) : Expression

sealed interface Condition

data class GreaterThan(
    val left: Expression,
    val right: Expression,
) : Condition

data class Equals(
    val value: String,
    val contextField: String,
) : Condition

fun evaluate(
    expression: Expression,
    context: RuleContext,
): BigDecimal =
    when (expression) {
        is Literal -> expression.value
        is QuantityRef -> context.quantity.toBigDecimal()
        is BasePriceRef -> context.basePrice
        is Multiply -> {
            val l = evaluate(expression.left, context)
            val r = evaluate(expression.right, context)
            (l * r).setScale(2, RoundingMode.HALF_UP)
        }
        is Add -> {
            val l = evaluate(expression.left, context)
            val r = evaluate(expression.right, context)
            l + r
        }
        is Subtract -> {
            val l = evaluate(expression.left, context)
            val r = evaluate(expression.right, context)
            l - r
        }
        is IfThenElse -> {
            if (evaluateCondition(expression.condition, context)) {
                evaluate(expression.then, context)
            } else {
                evaluate(expression.otherwise, context)
            }
        }
    }

fun evaluateCondition(
    condition: Condition,
    context: RuleContext,
): Boolean =
    when (condition) {
        is GreaterThan -> {
            val l = evaluate(condition.left, context)
            val r = evaluate(condition.right, context)
            l > r
        }
        is Equals -> {
            when (condition.contextField) {
                "tier" -> context.customerTier == condition.value
                else -> throw IllegalArgumentException(
                    "Unknown context field '${condition.contextField}'. Valid fields: tier",
                )
            }
        }
    }
