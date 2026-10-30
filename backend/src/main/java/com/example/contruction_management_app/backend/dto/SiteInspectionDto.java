package com.example.contruction_management_app.backend.dto;

import com.example.contruction_management_app.backend.domain.InspectionStatus;
import com.example.contruction_management_app.backend.domain.InspectionType;
import com.example.contruction_management_app.backend.domain.SiteInspection;

import java.time.Instant;
import java.time.LocalDate;

public record SiteInspectionDto(
    String id,
    String projectId,
    String title,
    InspectionType inspectionType,
    InspectionStatus status,
    String checklistData,
    String inspectorId,
    LocalDate inspectionDate,
    String location,
    String notes,
    Instant createdAt
) {
    public static SiteInspectionDto fromEntity(SiteInspection i) {
        return new SiteInspectionDto(
            i.getId(),
            i.getProject().getId(),
            i.getTitle(),
            i.getInspectionType(),
            i.getStatus(),
            i.getChecklistData(),
            i.getInspectorId(),
            i.getInspectionDate(),
            i.getLocation(),
            i.getNotes(),
            i.getCreatedAt()
        );
    }
}
