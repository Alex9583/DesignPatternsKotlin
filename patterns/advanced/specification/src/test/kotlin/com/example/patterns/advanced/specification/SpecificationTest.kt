package com.example.patterns.advanced.specification

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SpecificationTest {
    private val deliveries =
        listOf(
            Delivery("D1", "EU-WEST", 5.0, fragile = false, distanceKm = 120),
            Delivery("D2", "EU-WEST", 25.0, fragile = true, distanceKm = 50),
            Delivery("D3", "US-EAST", 3.0, fragile = false, distanceKm = 300),
            Delivery("D4", "EU-WEST", 12.0, fragile = true, distanceKm = 80),
            Delivery("D5", "ASIA-PAC", 0.5, fragile = false, distanceKm = 1000),
        )

    @Test
    fun should_filterByRegion_when_singleSpecUsed() {
        val spec = RegionSpec("EU-WEST")

        val result = deliveries.matching(spec)

        assertEquals(3, result.size)
        assertTrue(result.all { it.region == "EU-WEST" })
    }

    @Test
    fun should_combineSpecsWithAnd_when_multipleRulesRequired() {
        val spec = RegionSpec("EU-WEST") and MaxWeightSpec(10.0)

        val result = deliveries.matching(spec)

        assertEquals(1, result.size)
        assertEquals("D1", result.first().id)
    }

    @Test
    fun should_combineSpecsWithOr_when_eitherRuleSuffices() {
        val spec = RegionSpec("US-EAST") or RegionSpec("ASIA-PAC")

        val result = deliveries.matching(spec)

        assertEquals(2, result.size)
        assertTrue(result.any { it.id == "D3" })
        assertTrue(result.any { it.id == "D5" })
    }

    @Test
    fun should_negateSpec_when_notUsed() {
        val nonFragile = FragileSpec().not()

        val result = deliveries.matching(nonFragile)

        assertEquals(3, result.size)
        assertFalse(result.any { it.fragile })
    }

    @Test
    fun should_buildComplexCompositeSpec_when_multipleOperatorsCombined() {
        // EU-WEST, fragile, within 100km, under 20kg
        val spec =
            RegionSpec("EU-WEST") and
                FragileSpec() and
                MaxDistanceSpec(100) and
                MaxWeightSpec(20.0)

        val result = deliveries.matching(spec)

        assertEquals(1, result.size)
        assertEquals("D4", result.first().id)
    }

    @Test
    fun should_workWithLambda_when_funInterfaceUsed() {
        val lightweight = Specification<Delivery> { it.weightKg < 5.0 }

        val result = deliveries.matching(lightweight)

        assertEquals(2, result.size)
        assertTrue(result.all { it.weightKg < 5.0 })
    }
}
