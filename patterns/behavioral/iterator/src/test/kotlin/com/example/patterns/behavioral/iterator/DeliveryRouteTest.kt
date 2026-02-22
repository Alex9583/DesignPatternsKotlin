package com.example.patterns.behavioral.iterator

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeliveryRouteTest {
    private val waypoints =
        listOf(
            Waypoint("Warehouse", 48.8566, 2.3522, deliveryOrder = 1),
            Waypoint("Customer C", 48.8700, 2.3800, deliveryOrder = 3),
            Waypoint("Customer A", 48.8600, 2.3400, deliveryOrder = 2),
            Waypoint("Depot Return", 48.8566, 2.3522, deliveryOrder = 4),
        )

    @Test
    fun should_iterateInDeliveryOrder_when_waypointsAreUnordered() {
        val route = DeliveryRoute(waypoints)
        val iterator = route.iterator()

        assertTrue(iterator.hasNext())
        assertEquals("Warehouse", iterator.next().name)
        assertEquals("Customer A", iterator.next().name)
        assertEquals("Customer C", iterator.next().name)
        assertEquals("Depot Return", iterator.next().name)
        assertFalse(iterator.hasNext())
    }

    @Test
    fun should_workWithForInLoop_when_iteratingRoute() {
        val route = DeliveryRoute(waypoints)
        val visited = mutableListOf<String>()

        for (waypoint in route) {
            visited.add(waypoint.name)
        }

        assertEquals(
            listOf("Warehouse", "Customer A", "Customer C", "Depot Return"),
            visited,
        )
    }

    @Test
    fun should_returnPendingWaypoints_when_someAlreadyVisited() {
        val route = DeliveryRoute(waypoints)

        // After visiting index 1 (Customer A), pending should be Customer C and Depot Return
        val remaining = route.pending(currentIndex = 1)

        assertEquals(2, remaining.size)
        assertEquals("Customer C", remaining[0].name)
        assertEquals("Depot Return", remaining[1].name)
    }

    @Test
    fun should_handleEmptyRoute_when_noWaypointsProvided() {
        val route = DeliveryRoute(emptyList())

        assertFalse(route.iterator().hasNext())
        assertEquals(0, route.totalStops)
        assertEquals(emptyList(), route.pending(0))
    }

    @Test
    fun should_throwNoSuchElementException_when_iteratingPastEnd() {
        val route =
            DeliveryRoute(
                listOf(Waypoint("Only Stop", 0.0, 0.0, deliveryOrder = 1)),
            )
        val iterator = route.iterator()

        iterator.next()

        assertFailsWith<NoSuchElementException> {
            iterator.next()
        }
    }

    @Test
    fun should_reportTotalStops_when_usingExtensionProperty() {
        val route = DeliveryRoute(waypoints)

        assertEquals(4, route.totalStops)
    }
}
