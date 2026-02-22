package com.example.patterns.advanced.eventbus

import kotlin.reflect.KClass

sealed interface DomainEvent

data class OrderPlaced(
    val orderId: String,
    val totalItems: Int,
) : DomainEvent

data class ShipmentDispatched(
    val shipmentId: String,
    val destination: String,
) : DomainEvent

data class PaymentReceived(
    val transactionId: String,
    val amountCents: Long,
) : DomainEvent

fun interface EventHandler<T : DomainEvent> {
    fun handle(event: T)
}

class EventBus {
    private val handlers = mutableMapOf<KClass<*>, MutableList<EventHandler<*>>>()

    inline fun <reified T : DomainEvent> subscribe(handler: EventHandler<T>) {
        subscribe(T::class, handler)
    }

    @PublishedApi
    internal fun <T : DomainEvent> subscribe(
        type: KClass<T>,
        handler: EventHandler<T>,
    ) {
        handlers.getOrPut(type) { mutableListOf() }.add(handler)
    }

    @Suppress("UNCHECKED_CAST")
    fun publish(event: DomainEvent) {
        val registered = handlers[event::class] ?: return
        val errors = mutableListOf<Exception>()
        for (handler in registered) {
            try {
                (handler as EventHandler<DomainEvent>).handle(event)
            } catch (e: Exception) {
                errors.add(e)
            }
        }
        if (errors.isNotEmpty()) {
            throw EventDeliveryException(
                "Failed to deliver ${event::class.simpleName} to ${errors.size} handler(s)",
                errors,
            )
        }
    }

    fun subscriberCount(type: KClass<*>): Int = handlers[type]?.size ?: 0
}

class EventDeliveryException(
    message: String,
    val handlerErrors: List<Exception>,
) : RuntimeException(message, handlerErrors.firstOrNull())
