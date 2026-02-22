package com.example.patterns.structural.flyweight

data class RouteMetadata(
    val zone: String,
    val distanceKm: Int,
    val roadType: String,
)

data class DeliveryEvent(
    val orderId: String,
    val route: RouteMetadata,
    val timestamp: Long,
)

class RouteMetadataFactory {
    private val cache = mutableMapOf<String, RouteMetadata>()

    fun getRoute(
        zone: String,
        distanceKm: Int,
        roadType: String,
    ): RouteMetadata {
        val key = "$zone|$distanceKm|$roadType"
        return cache.getOrPut(key) {
            RouteMetadata(zone, distanceKm, roadType)
        }
    }

    fun cacheSize(): Int = cache.size
}
