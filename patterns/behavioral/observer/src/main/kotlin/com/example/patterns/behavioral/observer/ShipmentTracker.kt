package com.example.patterns.behavioral.observer

data class TrackingEvent(
    val shipmentId: String,
    val status: String,
    val location: String,
    val timestamp: Long = System.currentTimeMillis(),
)

fun interface TrackingObserver {
    fun onEvent(event: TrackingEvent)
}

class ObserverNotificationException(
    message: String,
    val observerErrors: List<Exception>,
) : RuntimeException(message, observerErrors.firstOrNull())

class ShipmentTracker {
    private val observers = mutableListOf<TrackingObserver>()

    fun subscribe(observer: TrackingObserver) {
        observers.add(observer)
    }

    fun unsubscribe(observer: TrackingObserver) {
        observers.remove(observer)
    }

    fun updateStatus(
        shipmentId: String,
        status: String,
        location: String,
    ) {
        val event =
            TrackingEvent(
                shipmentId = shipmentId,
                status = status,
                location = location,
            )
        val errors = mutableListOf<Exception>()
        for (observer in observers) {
            try {
                observer.onEvent(event)
            } catch (e: Exception) {
                errors.add(e)
            }
        }
        if (errors.isNotEmpty()) {
            throw ObserverNotificationException(
                "Failed to notify ${errors.size} observer(s) for shipment $shipmentId",
                errors,
            )
        }
    }
}
