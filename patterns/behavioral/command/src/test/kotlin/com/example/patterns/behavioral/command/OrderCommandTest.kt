package com.example.patterns.behavioral.command

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OrderCommandTest {
    private val orderBook = OrderBook()
    private val invoker = OrderCommandInvoker()

    @Test
    fun should_addOrderToBook_when_placeOrderExecuted() {
        val order = Order("ORD-001", "Ergonomic Chair", 1)

        invoker.execute(PlaceOrderCommand(orderBook, order))

        val stored = orderBook.get("ORD-001")
        assertNotNull(stored)
        assertEquals("Ergonomic Chair", stored.product)
        assertEquals(OrderStatus.CREATED, stored.status)
    }

    @Test
    fun should_removeOrderFromBook_when_cancelOrderExecuted() {
        val order = Order("ORD-002", "Standing Desk", 1)
        orderBook.add(order)

        invoker.execute(CancelOrderCommand(orderBook, "ORD-002"))

        assertNull(orderBook.get("ORD-002"))
    }

    @Test
    fun should_removeOrder_when_placeOrderUndone() {
        val order = Order("ORD-003", "Monitor Arm", 2)
        invoker.execute(PlaceOrderCommand(orderBook, order))

        assertTrue(orderBook.contains("ORD-003"))

        invoker.undoLast()

        assertFalse(orderBook.contains("ORD-003"))
    }

    @Test
    fun should_restoreOrder_when_cancelOrderUndone() {
        val order = Order("ORD-004", "Laptop Stand", 1, status = OrderStatus.CONFIRMED)
        orderBook.add(order)

        invoker.execute(CancelOrderCommand(orderBook, "ORD-004"))
        assertNull(orderBook.get("ORD-004"))

        invoker.undoLast()

        val restored = orderBook.get("ORD-004")
        assertNotNull(restored)
        assertEquals(OrderStatus.CONFIRMED, restored.status)
    }

    @Test
    fun should_undoMultipleCommandsInOrder_when_undoCalledRepeatedly() {
        val order1 = Order("ORD-010", "Webcam", 1)
        val order2 = Order("ORD-011", "Headset", 1)

        invoker.execute(PlaceOrderCommand(orderBook, order1))
        invoker.execute(PlaceOrderCommand(orderBook, order2))

        assertTrue(orderBook.contains("ORD-010"))
        assertTrue(orderBook.contains("ORD-011"))

        // Undo last (order2) first
        invoker.undoLast()
        assertTrue(orderBook.contains("ORD-010"))
        assertFalse(orderBook.contains("ORD-011"))

        // Undo order1
        invoker.undoLast()
        assertFalse(orderBook.contains("ORD-010"))
    }

    @Test
    fun should_returnFalse_when_undoCalledOnEmptyHistory() {
        val result = invoker.undoLast()

        assertFalse(result)
    }
}
