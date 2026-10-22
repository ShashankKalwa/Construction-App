package com.example.contruction_management_app

import com.example.contruction_management_app.domain.model.Document
import com.example.contruction_management_app.domain.model.DocumentStatus
import com.example.contruction_management_app.domain.model.DocumentType
import com.example.contruction_management_app.domain.model.DocumentVersion
import org.junit.Assert.*
import org.junit.Test

class DocumentVersionUnitTest {

    @Test
    fun testFormattedSizeCalculations() {
        val mbVersion = DocumentVersion(
            id = "v1",
            documentId = "d1",
            versionNumber = "v1.0",
            fileName = "arch.pdf",
            fileSizeBytes = 17_300_000L,
            mimeType = "application/pdf",
            fileUrl = "http://example.com/arch.pdf",
            changeNotes = null,
            uploadedBy = "Rahul",
            uploadedAt = "2026-03-01"
        )
        assertEquals("16.5 MB", mbVersion.formattedSize)

        val kbVersion = mbVersion.copy(fileSizeBytes = 512_000L)
        assertEquals("500.0 KB", kbVersion.formattedSize)

        val byteVersion = mbVersion.copy(fileSizeBytes = 512L)
        assertEquals("512 B", byteVersion.formattedSize)
    }

    @Test
    fun testVersionBumpingLogic() {
        fun bump(ver: String): String {
            val cleaned = ver.removePrefix("v")
            val parts = cleaned.split(".")
            return if (parts.size == 2) {
                val major = parts[0].toInt()
                val minor = parts[1].toInt() + 1
                "v$major.$minor"
            } else {
                "v2.0"
            }
        }

        assertEquals("v1.1", bump("v1.0"))
        assertEquals("v2.2", bump("v2.1"))
        assertEquals("v10.5", bump("v10.4"))
    }

    @Test
    fun testDocumentTypeDisplayNames() {
        assertEquals("Architectural Drawing", DocumentType.DRAWING_ARCHITECTURAL.displayName)
        assertEquals("Structural Drawing", DocumentType.DRAWING_STRUCTURAL.displayName)
        assertEquals("Contract & Agreement", DocumentType.CONTRACT.displayName)
        assertEquals("Statutory Permit / RERA", DocumentType.PERMIT.displayName)
    }
}
