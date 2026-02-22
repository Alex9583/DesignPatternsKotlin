package com.example.patterns.behavioral.command

enum class OrderStatus {
    CREATED,
    CONFIRMED,
    SHIPPED,
    CANCELLED,
}

data class Order(
    val id: String,
    val product: String,
    val quantity: Int,
    val status: OrderStatus = OrderStatus.CREATED,
)

interface Command {
    fun execute()

    fun undo()
}

class OrderBook {
    private val orders = mutableMapOf<String, Order>()

    fun add(order: Order) {
        orders[order.id] = order
    }

    fun remove(orderId: String): Order? = orders.remove(orderId)

    fun get(orderId: String): Order? = orders[orderId]

    fun updateStatus(
        orderId: String,
        status: OrderStatus,
    ) {
        val existing = orders[orderId] ?: return
        orders[orderId] = existing.copy(status = status)
    }

    fun contains(orderId: String): Boolean = orderId in orders
}

class PlaceOrderCommand(
    val orderBook: OrderBook,
    val order: Order,
) : Command {
    override fun execute() {
        orderBook.add(order)
    }

    override fun undo() {
        orderBook.remove(order.id)
    }
}

class CancelOrderCommand(
    val orderBook: OrderBook,
    val orderId: String,
) : Command {
    private var previousOrder: Order? = null

    override fun execute() {
        previousOrder = orderBook.get(orderId)?.copy()
        orderBook.remove(orderId)
    }

    override fun undo() {
        previousOrder?.let { orderBook.add(it) }
    }
}

class OrderCommandInvoker {
    private val history = ArrayDeque<Command>()

    fun execute(command: Command) {
        command.execute()
        history.addLast(command)
    }

    fun undoLast(): Boolean {
        if (history.isEmpty()) return false
        history.removeLast().undo()
        return true
    }

    fun historySize(): Int = history.size
}
