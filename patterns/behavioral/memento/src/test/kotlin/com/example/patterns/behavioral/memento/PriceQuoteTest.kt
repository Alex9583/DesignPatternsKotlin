package com.example.patterns.behavioral.memento

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PriceQuoteTest {
    @Test
    fun should_saveAndRestoreState_when_mementoUsed() {
        val quote = PriceQuote("Laptop Stand", BigDecimal("49.99"), 2)
        val snapshot = quote.save()

        // Mutate the originator
        quote.productName = "Monitor Arm"
        quote.unitPrice = BigDecimal("89.99")
        quote.quantity = 1

        // Restore
        quote.restore(snapshot)

        assertEquals("Laptop Stand", quote.productName)
        assertEquals(BigDecimal("49.99"), quote.unitPrice)
        assertEquals(2, quote.quantity)
        assertEquals(BigDecimal.ZERO, quote.discount)
    }

    @Test
    fun should_undoMultipleSteps_when_historyContainsSnapshots() {
        val quote = PriceQuote("Keyboard", BigDecimal("120.00"), 1)
        val history = QuoteHistory()

        // Step 1 — save initial state, then change
        history.push(quote.save())
        quote.quantity = 5

        // Step 2 — save again, then apply discount
        history.push(quote.save())
        quote.discount = BigDecimal("10")

        // Undo step 2 → quantity 5, no discount
        quote.restore(history.pop()!!)
        assertEquals(5, quote.quantity)
        assertEquals(BigDecimal.ZERO, quote.discount)

        // Undo step 1 → quantity 1
        quote.restore(history.pop()!!)
        assertEquals(1, quote.quantity)
    }

    @Test
    fun should_trackHistorySize_when_snapshotsPushed() {
        val history = QuoteHistory()
        val quote = PriceQuote("Mouse", BigDecimal("25.00"), 1)

        assertEquals(0, history.size)

        history.push(quote.save())
        assertEquals(1, history.size)

        history.push(quote.save())
        assertEquals(2, history.size)

        history.pop()
        assertEquals(1, history.size)
    }

    @Test
    fun should_notAffectHistory_when_stateRestored() {
        val quote = PriceQuote("Webcam", BigDecimal("75.00"), 3)
        val history = QuoteHistory()

        history.push(quote.save())
        quote.quantity = 10
        history.push(quote.save())

        quote.restore(history.peek()!!)
        assertEquals(2, history.size)
    }

    @Test
    fun should_returnNull_when_historyIsEmpty() {
        val history = QuoteHistory()

        assertNull(history.pop())
        assertNull(history.peek())
    }

    @Test
    fun should_computeTotalPrice_when_discountApplied() {
        // 50.00 × 4 = 200.00, 15 % off → 170.00
        val quote =
            PriceQuote(
                productName = "USB Hub",
                unitPrice = BigDecimal("50.00"),
                quantity = 4,
                discount = BigDecimal("15"),
            )

        assertEquals(BigDecimal("170.00"), quote.totalPrice())
    }
}
