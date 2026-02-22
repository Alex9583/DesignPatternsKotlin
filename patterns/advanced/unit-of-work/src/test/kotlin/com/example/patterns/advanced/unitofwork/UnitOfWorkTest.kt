package com.example.patterns.advanced.unitofwork

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UnitOfWorkTest {
    private val store = InMemoryLedgerStore()
    private val uow = UnitOfWork(store)

    private val entry1 = LedgerEntry("L1", "Payment received", BigDecimal("250.00"))
    private val entry2 = LedgerEntry("L2", "Refund issued", BigDecimal("-50.00"))

    @Test
    fun should_persistEntries_when_commitCalled() {
        uow.registerInsert(entry1)
        uow.registerInsert(entry2)

        uow.commit()

        assertEquals(entry1, store.findById("L1"))
        assertEquals(entry2, store.findById("L2"))
    }

    @Test
    fun should_notPersist_when_rollbackCalled() {
        uow.registerInsert(entry1)
        uow.registerInsert(entry2)

        uow.rollback()

        assertNull(store.findById("L1"))
        assertNull(store.findById("L2"))
        assertTrue(uow.pendingChanges().isEmpty())
    }

    @Test
    fun should_trackAllChangeTypes_when_insertUpdateDeleteRegistered() {
        uow.registerInsert(entry1)
        val updated = entry1.copy(amount = BigDecimal("300.00"))
        uow.registerUpdate(updated)
        uow.registerDelete(entry2)

        val changes = uow.pendingChanges()

        assertEquals(3, changes.size)
        assertTrue(changes[0] is Change.Insert)
        assertTrue(changes[1] is Change.Update)
        assertTrue(changes[2] is Change.Delete)
    }

    @Test
    fun should_throwException_when_commitCalledTwice() {
        uow.registerInsert(entry1)
        uow.commit()

        assertFailsWith<IllegalStateException> {
            uow.commit()
        }
    }

    @Test
    fun should_throwException_when_registerAfterCommit() {
        uow.commit()

        assertFailsWith<IllegalStateException> {
            uow.registerInsert(entry1)
        }
    }

    @Test
    fun should_applyUpdateCorrectly_when_entryModified() {
        store.applyChanges(listOf(Change.Insert(entry1)))
        val updated = entry1.copy(amount = BigDecimal("999.99"))

        val uow2 = UnitOfWork(store)
        uow2.registerUpdate(updated)
        uow2.commit()

        assertEquals(BigDecimal("999.99"), store.findById("L1")?.amount)
    }
}
