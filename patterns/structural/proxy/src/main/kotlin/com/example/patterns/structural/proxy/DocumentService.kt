package com.example.patterns.structural.proxy

enum class UserRole {
    VIEWER,
    EDITOR,
    ADMIN,
}

data class User(
    val name: String,
    val role: UserRole,
)

data class Document(
    val id: String,
    val title: String,
    val content: String,
)

class AccessDeniedException(
    message: String,
) : RuntimeException(message)

interface DocumentService {
    fun read(documentId: String): Document?

    fun write(
        documentId: String,
        content: String,
    ): Boolean

    fun delete(documentId: String): Boolean
}

class InMemoryDocumentService : DocumentService {
    private val store = mutableMapOf<String, Document>()

    fun seed(document: Document) {
        store[document.id] = document
    }

    override fun read(documentId: String): Document? = store[documentId]

    override fun write(
        documentId: String,
        content: String,
    ): Boolean {
        val existing = store[documentId] ?: return false
        store[documentId] = existing.copy(content = content)
        return true
    }

    override fun delete(documentId: String): Boolean = store.remove(documentId) != null
}

class AccessControlProxy(
    private val service: DocumentService,
    private val currentUser: User,
) : DocumentService {
    override fun read(documentId: String): Document? {
        // All roles can read
        return service.read(documentId)
    }

    override fun write(
        documentId: String,
        content: String,
    ): Boolean {
        if (currentUser.role == UserRole.VIEWER) {
            throw AccessDeniedException(
                "User '${currentUser.name}' (VIEWER) is not allowed to write documents",
            )
        }
        return service.write(documentId, content)
    }

    override fun delete(documentId: String): Boolean {
        if (currentUser.role != UserRole.ADMIN) {
            throw AccessDeniedException(
                "User '${currentUser.name}' (${currentUser.role}) is not allowed to delete documents",
            )
        }
        return service.delete(documentId)
    }
}
