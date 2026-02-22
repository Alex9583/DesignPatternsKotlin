package com.example.patterns.structural.proxy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DocumentServiceTest {
    private val sampleDoc =
        Document(
            id = "DOC-001",
            title = "Architecture Decision Record",
            content = "Initial content",
        )

    private fun serviceWithDoc(): InMemoryDocumentService {
        val service = InMemoryDocumentService()
        service.seed(sampleDoc)
        return service
    }

    @Test
    fun should_allowRead_when_userIsViewer() {
        val proxy =
            AccessControlProxy(
                service = serviceWithDoc(),
                currentUser = User("Alice", UserRole.VIEWER),
            )

        val doc = proxy.read("DOC-001")

        assertNotNull(doc)
        assertEquals("Architecture Decision Record", doc.title)
    }

    @Test
    fun should_denyWrite_when_userIsViewer() {
        val proxy =
            AccessControlProxy(
                service = serviceWithDoc(),
                currentUser = User("Alice", UserRole.VIEWER),
            )

        val exception =
            assertFailsWith<AccessDeniedException> {
                proxy.write("DOC-001", "Hacked content")
            }

        assertTrue(exception.message!!.contains("VIEWER"))
    }

    @Test
    fun should_allowWrite_when_userIsEditor() {
        val proxy =
            AccessControlProxy(
                service = serviceWithDoc(),
                currentUser = User("Bob", UserRole.EDITOR),
            )

        val success = proxy.write("DOC-001", "Updated by editor")

        assertTrue(success)
        assertEquals("Updated by editor", proxy.read("DOC-001")!!.content)
    }

    @Test
    fun should_denyDelete_when_userIsEditor() {
        val proxy =
            AccessControlProxy(
                service = serviceWithDoc(),
                currentUser = User("Bob", UserRole.EDITOR),
            )

        val exception =
            assertFailsWith<AccessDeniedException> {
                proxy.delete("DOC-001")
            }

        assertTrue(exception.message!!.contains("EDITOR"))
    }

    @Test
    fun should_allowAllOperations_when_userIsAdmin() {
        val service = serviceWithDoc()
        val proxy =
            AccessControlProxy(
                service = service,
                currentUser = User("Charlie", UserRole.ADMIN),
            )

        // Read
        assertNotNull(proxy.read("DOC-001"))

        // Write
        assertTrue(proxy.write("DOC-001", "Admin update"))
        assertEquals("Admin update", proxy.read("DOC-001")!!.content)

        // Delete
        assertTrue(proxy.delete("DOC-001"))
        assertEquals(null, proxy.read("DOC-001"))
    }
}
