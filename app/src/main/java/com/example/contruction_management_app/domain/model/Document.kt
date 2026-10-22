package com.example.contruction_management_app.domain.model

enum class DocumentType(val displayName: String) {
    DRAWING_ARCHITECTURAL("Architectural Drawing"),
    DRAWING_STRUCTURAL("Structural Drawing"),
    CONTRACT("Contract & Agreement"),
    INSPECTION_REPORT("Inspection & Testing"),
    PERMIT("Statutory Permit / RERA"),
    SAFETY_CLEARANCE("Safety Clearance")
}

enum class DocumentStatus(val displayName: String) {
    DRAFT("Draft"),
    APPROVED("Approved"),
    SUPERSEDED("Superseded"),
    ARCHIVED("Archived")
}

data class DocumentVersion(
    val id: String,
    val documentId: String,
    val versionNumber: String,
    val fileName: String,
    val fileSizeBytes: Long,
    val mimeType: String,
    val fileUrl: String,
    val changeNotes: String?,
    val uploadedBy: String,
    val uploadedAt: String
) {
    val formattedSize: String
        get() = when {
            fileSizeBytes >= 1024 * 1024 -> String.format("%.1f MB", fileSizeBytes / (1024.0 * 1024.0))
            fileSizeBytes >= 1024 -> String.format("%.1f KB", fileSizeBytes / 1024.0)
            else -> "$fileSizeBytes B"
        }
}

data class Document(
    val id: String,
    val projectId: String,
    val organizationId: String,
    val title: String,
    val documentType: DocumentType,
    val category: String?,
    val latestVersion: String,
    val status: DocumentStatus,
    val createdBy: String,
    val createdAt: String,
    val updatedAt: String,
    val versions: List<DocumentVersion> = emptyList()
)
