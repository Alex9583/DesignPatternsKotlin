package com.example.patterns.creational.builder

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ShipmentOrderTest {
    private val paris = Address("10 Rue de Rivoli", "Paris", "75001", "FR")
    private val london = Address("221B Baker Street", "London", "NW1 6XE", "GB")

    @Test
    fun should_buildShipmentOrder_when_allFieldsProvided() {
        val order =
            ShipmentOrder
                .builder("SHP-2024-001")
                .withSender(paris)
                .withRecipient(london)
                .withItem("Laptop", quantity = 1, weightKg = BigDecimal("2.5"))
                .withItem("Charger", quantity = 2, weightKg = BigDecimal("0.3"))
                .withPriority(ShippingPriority.EXPRESS)
                .withFragile()
                .withInsurance(BigDecimal("1500.00"))
                .withNotes("Handle with care")
                .build()

        assertEquals("SHP-2024-001", order.reference)
        assertEquals(paris, order.sender)
        assertEquals(london, order.recipient)
        assertEquals(2, order.items.size)
        assertEquals(ShippingPriority.EXPRESS, order.priority)
        assertTrue(order.fragile)
        assertEquals(BigDecimal("1500.00"), order.insuranceValue)
    }

    @Test
    fun should_useDefaults_when_optionalFieldsOmitted() {
        val order =
            ShipmentOrder
                .builder("SHP-2024-002")
                .withSender(paris)
                .withRecipient(london)
                .withItem("Book", weightKg = BigDecimal("0.5"))
                .build()

        assertEquals(ShippingPriority.STANDARD, order.priority)
        assertEquals(false, order.fragile)
        assertNull(order.insuranceValue)
        assertNull(order.notes)
    }

    @Test
    fun should_calculateTotalWeight_when_multipleItemsPresent() {
        val order =
            ShipmentOrder
                .builder("SHP-2024-003")
                .withSender(paris)
                .withRecipient(london)
                .withItem("Book", quantity = 3, weightKg = BigDecimal("0.5"))
                .withItem("Poster Tube", quantity = 1, weightKg = BigDecimal("1.0"))
                .build()

        assertEquals(BigDecimal("2.5"), order.totalWeightKg)
    }

    @Test
    fun should_failValidation_when_noItemsProvided() {
        assertFailsWith<IllegalArgumentException> {
            ShipmentOrder
                .builder("SHP-2024-004")
                .withSender(paris)
                .withRecipient(london)
                .build()
        }
    }

    @Test
    fun should_failValidation_when_recipientMissing() {
        assertFailsWith<IllegalArgumentException> {
            ShipmentOrder
                .builder("SHP-2024-005")
                .withSender(paris)
                .withItem("Package", weightKg = BigDecimal("1.0"))
                .build()
        }
    }

    @Test
    fun should_failValidation_when_referenceBlank() {
        assertFailsWith<IllegalArgumentException> {
            ShipmentOrder
                .builder("")
                .withSender(paris)
                .withRecipient(london)
                .withItem("Package", weightKg = BigDecimal("1.0"))
                .build()
        }
    }

    @Test
    fun should_allowFluentChaining_when_buildingIncrementally() {
        val builder =
            ShipmentOrder
                .builder("SHP-2024-006")
                .withSender(paris)
                .withRecipient(london)

        val order =
            builder
                .withItem("Item A", weightKg = BigDecimal("1.0"))
                .withItem("Item B", weightKg = BigDecimal("2.0"))
                .withPriority(ShippingPriority.OVERNIGHT)
                .build()

        assertEquals(ShippingPriority.OVERNIGHT, order.priority)
        assertEquals(2, order.items.size)
    }
}
