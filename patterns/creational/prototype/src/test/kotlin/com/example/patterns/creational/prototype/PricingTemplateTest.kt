package com.example.patterns.creational.prototype

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame

class PricingTemplateTest {
    private val standardTemplate =
        PricingTemplate(
            name = "Standard EU",
            segment = CustomerSegment.STANDARD,
            baseMarkup = BigDecimal("1.20"),
            discounts = listOf(DiscountRule("Loyalty", BigDecimal("5"))),
            freeShippingThreshold = BigDecimal("50.00"),
        )

    @Test
    fun should_cloneTemplate_when_usingDataClassCopy() {
        val clone = standardTemplate.copy()

        assertEquals(standardTemplate, clone)
        assertNotSame(standardTemplate, clone)
    }

    @Test
    fun should_customizeClone_when_usingWithMethods() {
        val vipTemplate =
            standardTemplate
                .withName("VIP EU")
                .withSegment(CustomerSegment.VIP)
                .withAdditionalDiscount(DiscountRule("VIP Bonus", BigDecimal("15")))
                .withFreeShippingThreshold(null)

        assertEquals("VIP EU", vipTemplate.name)
        assertEquals(CustomerSegment.VIP, vipTemplate.segment)
        assertEquals(2, vipTemplate.discounts.size)
        assertEquals(null, vipTemplate.freeShippingThreshold)
    }

    @Test
    fun should_preserveOriginal_when_cloneModified() {
        val clone = standardTemplate.withName("Premium EU").withSegment(CustomerSegment.PREMIUM)

        assertEquals("Standard EU", standardTemplate.name)
        assertEquals(CustomerSegment.STANDARD, standardTemplate.segment)
        assertEquals("Premium EU", clone.name)
        assertEquals(CustomerSegment.PREMIUM, clone.segment)
    }

    @Test
    fun should_cloneFromCatalog_when_templateRegistered() {
        val catalog = PricingCatalog()
        catalog.register(standardTemplate)

        val clone = catalog.clone("Standard EU")

        assertEquals(standardTemplate, clone)
        assertNotSame(standardTemplate, clone)
    }

    @Test
    fun should_throwException_when_cloningUnknownTemplate() {
        val catalog = PricingCatalog()

        assertFailsWith<NoSuchElementException> {
            catalog.clone("Unknown")
        }
    }

    @Test
    fun should_notAffectOriginalDiscounts_when_addingDiscountToClone() {
        val clone = standardTemplate.withAdditionalDiscount(DiscountRule("Flash Sale", BigDecimal("10")))

        assertEquals(1, standardTemplate.discounts.size)
        assertEquals(2, clone.discounts.size)
    }
}
