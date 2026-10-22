package com.example.contruction_management_app.backend.dto;

import com.example.contruction_management_app.backend.domain.DocumentVersion;

import java.time.Instant;

public record DocumentVersionDto(
    String id,
    String documentId,
    String versionNumber,
    String fileName,
    Long fileSizeBytes,
    String mimeType,
    String fileUrl,
    String changeNotes,
    String uploadedBy,
    Instant uploadedAt
) {
    public static DocumentVersionDto fromEntity(DocumentVersion v) {
        return new DocumentVersionDto(
            v.getId(),
            v.getDocument() != null ? v.getDocument().getId() : null,
            v.getVersionNumber(),
            v.getFileName(),
            v.getFileSizeBytes(),
            v.getMimeType(),
            v.getFileUrl(),
            v.getChangeNotes(),
            v.getUploadedBy(),
            v.getUploadedAt()
        );
    }
}
