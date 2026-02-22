package com.example.patterns.advanced.unitofwork

import java.math.BigDecimal

data class LedgerEntry(
    val id: String,
    val description: String,
    val amount: BigDecimal,
)

sealed interface Change {
    val entry: LedgerEntry

    data class Insert(
        override val entry: LedgerEntry,
    ) : Change

    data class Update(
        override val entry: LedgerEntry,
    ) : Change

    data class Delete(
        override val entry: LedgerEntry,
    ) : Change
}

interface LedgerStore {
    fun findById(id: String): LedgerEntry?

    fun findAll(): List<LedgerEntry>

    fun applyChanges(changes: List<Change>)
}

class InMemoryLedgerStore(
    private val entries: MutableMap<String, LedgerEntry> = mutableMapOf(),
) : LedgerStore {
    override fun findById(id: String): LedgerEntry? = entries[id]

    override fun findAll(): List<LedgerEntry> = entries.values.toList()

    override fun applyChanges(changes: List<Change>) {
        for (change in changes) {
            when (change) {
                is Change.Insert -> entries[change.entry.id] = change.entry
                is Change.Update -> entries[change.entry.id] = change.entry
                is Change.Delete -> entries.remove(change.entry.id)
            }
        }
    }
}

class UnitOfWork(
    private val store: LedgerStore,
) {
    private val changes = mutableListOf<Change>()
    private var committed = false

    fun registerInsert(entry: LedgerEntry) {
        check(!committed) { "UnitOfWork already committed" }
        changes.add(Change.Insert(entry))
    }

    fun registerUpdate(entry: LedgerEntry) {
        check(!committed) { "UnitOfWork already committed" }
        changes.add(Change.Update(entry))
    }

    fun registerDelete(entry: LedgerEntry) {
        check(!committed) { "UnitOfWork already committed" }
        changes.add(Change.Delete(entry))
    }

    fun pendingChanges(): List<Change> = changes.toList()

    fun commit() {
        check(!committed) { "UnitOfWork already committed" }
        store.applyChanges(changes)
        committed = true
    }

    fun rollback() {
        check(!committed) { "Cannot rollback after commit" }
        changes.clear()
    }
}
