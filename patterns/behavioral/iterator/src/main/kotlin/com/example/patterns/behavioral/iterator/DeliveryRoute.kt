package com.example.patterns.behavioral.iterator

data class Waypoint(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val deliveryOrder: Int,
)

class DeliveryRoute(
    private val waypoints: List<Waypoint>,
) : Iterable<Waypoint> {
    private val sorted: List<Waypoint> = waypoints.sortedBy { it.deliveryOrder }

    override fun iterator(): Iterator<Waypoint> = DeliveryRouteIterator(sorted)

    fun waypointAt(index: Int): Waypoint = sorted[index]

    fun size(): Int = sorted.size
}

class DeliveryRouteIterator(
    private val waypoints: List<Waypoint>,
) : Iterator<Waypoint> {
    private var currentIndex = 0

    override fun hasNext(): Boolean = currentIndex < waypoints.size

    override fun next(): Waypoint {
        if (!hasNext()) {
            throw NoSuchElementException("No more waypoints in the delivery route")
        }
        return waypoints[currentIndex++]
    }
}

fun DeliveryRoute.pending(currentIndex: Int): List<Waypoint> = this.drop(currentIndex + 1)

val DeliveryRoute.totalStops: Int
    get() = this.size()
