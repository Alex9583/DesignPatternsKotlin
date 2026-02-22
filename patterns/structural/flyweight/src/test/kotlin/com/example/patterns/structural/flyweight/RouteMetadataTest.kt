package com.example.patterns.structural.flyweight

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class RouteMetadataTest {
    @Test
    fun should_returnSameInstance_when_identicalRouteParametersRequested() {
        val factory = RouteMetadataFactory()

        val route1 = factory.getRoute("PARIS-NORTH", 35, "highway")
        val route2 = factory.getRoute("PARIS-NORTH", 35, "highway")

        assertSame(route1, route2, "Factory must return the exact same object reference")
    }

    @Test
    fun should_returnDifferentInstances_when_routeParametersDiffer() {
        val factory = RouteMetadataFactory()

        val highway = factory.getRoute("LYON-EAST", 120, "highway")
        val urban = factory.getRoute("LYON-EAST", 15, "urban")

        assertNotSame(highway, urban)
        assertEquals(2, factory.cacheSize())
    }

    @Test
    fun should_shareIntrinsicState_when_manyEventsReferSameRoute() {
        val factory = RouteMetadataFactory()
        val sharedRoute = factory.getRoute("MARSEILLE-PORT", 8, "urban")

        val events =
            (1..1_000).map { i ->
                DeliveryEvent(
                    orderId = "ORD-$i",
                    route = factory.getRoute("MARSEILLE-PORT", 8, "urban"),
                    timestamp = System.currentTimeMillis(),
                )
            }

        // All 1 000 events share the same intrinsic RouteMetadata instance
        events.forEach { event ->
            assertSame(sharedRoute, event.route)
        }

        // Only one entry in the cache despite 1 000 events
        assertEquals(1, factory.cacheSize())
    }

    @Test
    fun should_separateIntrinsicFromExtrinsicState_when_eventsShareRoute() {
        val factory = RouteMetadataFactory()
        val route = factory.getRoute("ZONE-A", 10, "highway")

        val event1 = DeliveryEvent(orderId = "ORD-100", route = route, timestamp = 1000L)
        val event2 = DeliveryEvent(orderId = "ORD-200", route = route, timestamp = 2000L)

        // Intrinsic state (route) is shared
        assertSame(event1.route, event2.route)
        // Extrinsic state (orderId, timestamp) differs
        assertNotEquals(event1.orderId, event2.orderId)
        assertNotEquals(event1.timestamp, event2.timestamp)
    }

    @Test
    fun should_growCacheOnlyForDistinctRoutes_when_duplicatesRequested() {
        val factory = RouteMetadataFactory()

        factory.getRoute("ZONE-A", 10, "highway")
        factory.getRoute("ZONE-A", 10, "highway") // duplicate
        factory.getRoute("ZONE-B", 20, "rural")
        factory.getRoute("ZONE-C", 5, "urban")
        factory.getRoute("ZONE-B", 20, "rural") // duplicate

        assertEquals(3, factory.cacheSize())
    }
}
