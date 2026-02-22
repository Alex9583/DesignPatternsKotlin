package com.example.patterns.creational.builder

import java.math.BigDecimal

enum class ShippingPriority { STANDARD, EXPRESS, OVERNIGHT }

data class Address(
    val street: String,
    val city: String,
    val postalCode: String,
    val country: String,
)

data class ShipmentItem(
    val name: String,
    val quantity: Int,
    val weightKg: BigDecimal,
)

data class ShipmentOrder(
    val reference: String,
    val sender: Address,
    val recipient: Address,
    val items: List<ShipmentItem>,
    val priority: ShippingPriority,
    val fragile: Boolean,
    val insuranceValue: BigDecimal?,
    val notes: String?,
) {
    init {
        require(reference.isNotBlank()) { "Reference must not be blank" }
        require(items.isNotEmpty()) { "Shipment must contain at least one item" }
    }

    val totalWeightKg: BigDecimal
        get() =
            items.fold(BigDecimal.ZERO) { acc, item ->
                acc + item.weightKg * BigDecimal(item.quantity)
            }

    companion object {
        fun builder(reference: String): ShipmentOrderBuilder = ShipmentOrderBuilder(reference)
    }
}

class ShipmentOrderBuilder(
    private val reference: String,
) {
    private var sender: Address? = null
    private var recipient: Address? = null
    private val items: MutableList<ShipmentItem> = mutableListOf()
    private var priority: ShippingPriority = ShippingPriority.STANDARD
    private var fragile: Boolean = false
    private var insuranceValue: BigDecimal? = null
    private var notes: String? = null

    fun withSender(sender: Address): ShipmentOrderBuilder = apply { this.sender = sender }

    fun withRecipient(recipient: Address): ShipmentOrderBuilder = apply { this.recipient = recipient }

    fun withItem(
        name: String,
        quantity: Int = 1,
        weightKg: BigDecimal,
    ): ShipmentOrderBuilder =
        apply {
            require(quantity > 0) { "Quantity must be positive" }
            items.add(ShipmentItem(name, quantity, weightKg))
        }

    fun withPriority(priority: ShippingPriority): ShipmentOrderBuilder = apply { this.priority = priority }

    fun withFragile(fragile: Boolean = true): ShipmentOrderBuilder = apply { this.fragile = fragile }

    fun withInsurance(value: BigDecimal): ShipmentOrderBuilder = apply { this.insuranceValue = value }

    fun withNotes(notes: String): ShipmentOrderBuilder = apply { this.notes = notes }

    fun build(): ShipmentOrder {
        val validatedSender = requireNotNull(sender) { "Sender address is required" }
        val validatedRecipient = requireNotNull(recipient) { "Recipient address is required" }
        return ShipmentOrder(
            reference = reference,
            sender = validatedSender,
            recipient = validatedRecipient,
            items = items.toList(),
            priority = priority,
            fragile = fragile,
            insuranceValue = insuranceValue,
            notes = notes,
        )
    }
}
