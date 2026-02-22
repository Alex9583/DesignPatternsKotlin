package com.example.patterns.behavioral.observer

import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShipmentTrackerTest {
    private class RecordingObserver : TrackingObserver {
        val events = mutableListOf<TrackingEvent>()

        override fun onEvent(event: TrackingEvent) {
            events.add(event)
        }
    }

    @Test
    fun should_notifySingleObserver_when_statusUpdated() {
        val tracker = ShipmentTracker()
        val observer = RecordingObserver()
        tracker.subscribe(observer)

        tracker.updateStatus("SHP-001", "IN_TRANSIT", "Paris Hub")

        assertEquals(1, observer.events.size)
        assertEquals("IN_TRANSIT", observer.events.first().status)
    }

    @Test
    fun should_notifyAllObservers_when_multipleSubscribed() {
        val tracker = ShipmentTracker()
        val observer1 = RecordingObserver()
        val observer2 = RecordingObserver()
        tracker.subscribe(observer1)
        tracker.subscribe(observer2)

        tracker.updateStatus("SHP-002", "DELIVERED", "Customer Address")

        assertEquals(1, observer1.events.size)
        assertEquals(1, observer2.events.size)
        assertEquals("DELIVERED", observer2.events.first().status)
    }

    @Test
    fun should_stopReceivingEvents_when_unsubscribed() {
        val tracker = ShipmentTracker()
        val observer = RecordingObserver()
        tracker.subscribe(observer)

        tracker.updateStatus("SHP-003", "PICKED_UP", "Warehouse A")
        tracker.unsubscribe(observer)
        tracker.updateStatus("SHP-003", "IN_TRANSIT", "Sorting Center")

        assertEquals(1, observer.events.size)
        assertEquals("PICKED_UP", observer.events.first().status)
    }

    @Test
    fun should_containCorrectData_when_eventEmitted() {
        val tracker = ShipmentTracker()
        val observer = RecordingObserver()
        tracker.subscribe(observer)

        tracker.updateStatus("SHP-100", "CUSTOMS_HOLD", "Frankfurt Airport")

        val event = observer.events.single()
        assertEquals("SHP-100", event.shipmentId)
        assertEquals("CUSTOMS_HOLD", event.status)
        assertEquals("Frankfurt Airport", event.location)
        assertTrue(event.timestamp > 0)
    }

    @Test
    fun should_notThrow_when_noObserversRegistered() {
        val tracker = ShipmentTracker()

        // Must complete without error even with zero observers
        assertDoesNotThrow { tracker.updateStatus("SHP-404", "LOST", "Unknown") }
    }
}
