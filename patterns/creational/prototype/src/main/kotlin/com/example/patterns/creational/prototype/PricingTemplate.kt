package com.example.patterns.creational.prototype

import java.math.BigDecimal

enum class CustomerSegment { STANDARD, PREMIUM, VIP }

data class DiscountRule(
    val label: String,
    val percentage: BigDecimal,
)

data class PricingTemplate(
    val name: String,
    val segment: CustomerSegment,
    val baseMarkup: BigDecimal,
    val discounts: List<DiscountRule>,
    val freeShippingThreshold: BigDecimal?,
) {
    fun withName(newName: String): PricingTemplate = copy(name = newName)

    fun withSegment(newSegment: CustomerSegment): PricingTemplate = copy(segment = newSegment)

    fun withAdditionalDiscount(rule: DiscountRule): PricingTemplate = copy(discounts = discounts + rule)

    fun withFreeShippingThreshold(threshold: BigDecimal?): PricingTemplate = copy(freeShippingThreshold = threshold)
}

class PricingCatalog {
    private val templates: MutableMap<String, PricingTemplate> = mutableMapOf()

    fun register(template: PricingTemplate) {
        templates[template.name] = template
    }

    fun clone(name: String): PricingTemplate =
        templates[name]?.copy()
            ?: throw NoSuchElementException("No template found with name: $name")

    fun list(): List<String> = templates.keys.toList()

    fun get(name: String): PricingTemplate? = templates[name]
}
