package com.example.contruction_management_app.backend.dto;

import com.example.contruction_management_app.backend.domain.Document;
import com.example.contruction_management_app.backend.domain.DocumentStatus;
import com.example.contruction_management_app.backend.domain.DocumentType;

import java.time.Instant;
import java.util.List;

public record DocumentDto(
    String id,
    String projectId,
    String organizationId,
    String title,
    DocumentType documentType,
    String category,
    String latestVersion,
    DocumentStatus status,
    String createdBy,
    Instant createdAt,
    Instant updatedAt,
    List<DocumentVersionDto> versions
) {
    public static DocumentDto fromEntity(Document d) {
        List<DocumentVersionDto> versionDtos = d.getVersions() != null
            ? d.getVersions().stream().map(DocumentVersionDto::fromEntity).toList()
            : List.of();
        return new DocumentDto(
            d.getId(),
            d.getProject() != null ? d.getProject().getId() : null,
            d.getOrganization() != null ? d.getOrganization().getId() : null,
            d.getTitle(),
            d.getDocumentType(),
            d.getCategory(),
            d.getLatestVersion(),
            d.getStatus(),
            d.getCreatedBy(),
            d.getCreatedAt(),
            d.getUpdatedAt(),
            versionDtos
        );
    }
}
