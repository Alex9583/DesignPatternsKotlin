package com.example.patterns.advanced.specification

fun interface Specification<T> {
    fun isSatisfiedBy(candidate: T): Boolean
}

infix fun <T> Specification<T>.and(other: Specification<T>): Specification<T> =
    Specification { candidate -> this.isSatisfiedBy(candidate) && other.isSatisfiedBy(candidate) }

infix fun <T> Specification<T>.or(other: Specification<T>): Specification<T> =
    Specification { candidate -> this.isSatisfiedBy(candidate) || other.isSatisfiedBy(candidate) }

fun <T> Specification<T>.not(): Specification<T> = Specification { candidate -> !this.isSatisfiedBy(candidate) }

fun <T> List<T>.matching(spec: Specification<T>): List<T> = filter { spec.isSatisfiedBy(it) }

data class Delivery(
    val id: String,
    val region: String,
    val weightKg: Double,
    val fragile: Boolean,
    val distanceKm: Int,
)

class RegionSpec(
    val region: String,
) : Specification<Delivery> {
    override fun isSatisfiedBy(candidate: Delivery): Boolean = candidate.region == region
}

class MaxWeightSpec(
    val maxKg: Double,
) : Specification<Delivery> {
    override fun isSatisfiedBy(candidate: Delivery): Boolean = candidate.weightKg <= maxKg
}

class FragileSpec : Specification<Delivery> {
    override fun isSatisfiedBy(candidate: Delivery): Boolean = candidate.fragile
}

class MaxDistanceSpec(
    val maxKm: Int,
) : Specification<Delivery> {
    override fun isSatisfiedBy(candidate: Delivery): Boolean = candidate.distanceKm <= maxKm
}
