package com.example.patterns.advanced.eventbus

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EventBusTest {
    @Test
    fun should_deliverEvent_when_handlerSubscribed() {
        val bus = EventBus()
        val received = mutableListOf<OrderPlaced>()
        bus.subscribe<OrderPlaced> { received.add(it) }

        bus.publish(OrderPlaced("ORD-1", totalItems = 3))

        assertEquals(1, received.size)
        assertEquals("ORD-1", received.first().orderId)
    }

    @Test
    fun should_deliverToMultipleHandlers_when_severalSubscribed() {
        val bus = EventBus()
        val log1 = mutableListOf<String>()
        val log2 = mutableListOf<String>()

        bus.subscribe<ShipmentDispatched> { log1.add(it.shipmentId) }
        bus.subscribe<ShipmentDispatched> { log2.add(it.destination) }

        bus.publish(ShipmentDispatched("SHP-1", "Paris"))

        assertEquals(listOf("SHP-1"), log1)
        assertEquals(listOf("Paris"), log2)
    }

    @Test
    fun should_notDeliverEvent_when_differentTypeSubscribed() {
        val bus = EventBus()
        val received = mutableListOf<OrderPlaced>()
        bus.subscribe<OrderPlaced> { received.add(it) }

        bus.publish(ShipmentDispatched("SHP-1", "Berlin"))

        assertTrue(received.isEmpty())
    }

    @Test
    fun should_handleMultipleEventTypes_when_busHasMixedSubscriptions() {
        val bus = EventBus()
        val orders = mutableListOf<String>()
        val payments = mutableListOf<Long>()

        bus.subscribe<OrderPlaced> { orders.add(it.orderId) }
        bus.subscribe<PaymentReceived> { payments.add(it.amountCents) }

        bus.publish(OrderPlaced("ORD-1", 2))
        bus.publish(PaymentReceived("TXN-1", 5000L))
        bus.publish(OrderPlaced("ORD-2", 1))

        assertEquals(listOf("ORD-1", "ORD-2"), orders)
        assertEquals(listOf(5000L), payments)
    }

    @Test
    fun should_trackSubscriberCount_when_handlersRegistered() {
        val bus = EventBus()

        bus.subscribe<OrderPlaced> { }
        bus.subscribe<OrderPlaced> { }
        bus.subscribe<ShipmentDispatched> { }

        assertEquals(2, bus.subscriberCount(OrderPlaced::class))
        assertEquals(1, bus.subscriberCount(ShipmentDispatched::class))
        assertEquals(0, bus.subscriberCount(PaymentReceived::class))
    }
}
