package com.example.contruction_management_app.backend.dto;

import com.example.contruction_management_app.backend.domain.DefectStatus;
import com.example.contruction_management_app.backend.domain.IssueSeverity;
import com.example.contruction_management_app.backend.domain.SiteDefect;

import java.time.Instant;
import java.time.LocalDate;

public record SiteDefectDto(
    String id,
    String projectId,
    String inspectionId,
    String title,
    String description,
    String location,
    IssueSeverity severity,
    DefectStatus status,
    String assignedTo,
    LocalDate deadline,
    String createdBy,
    Instant createdAt,
    Instant resolvedAt,
    String reInspectedBy,
    Instant reInspectedAt,
    String reInspectionNotes
) {
    public static SiteDefectDto fromEntity(SiteDefect d) {
        return new SiteDefectDto(
            d.getId(),
            d.getProject().getId(),
            d.getInspection() != null ? d.getInspection().getId() : null,
            d.getTitle(),
            d.getDescription(),
            d.getLocation(),
            d.getSeverity(),
            d.getStatus(),
            d.getAssignedTo(),
            d.getDeadline(),
            d.getCreatedBy(),
            d.getCreatedAt(),
            d.getResolvedAt(),
            d.getReInspectedBy(),
            d.getReInspectedAt(),
            d.getReInspectionNotes()
        );
    }
}
