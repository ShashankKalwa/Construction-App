package com.example.contruction_management_app.backend.dto;

import com.example.contruction_management_app.backend.domain.AuditAction;
import com.example.contruction_management_app.backend.domain.AuditEntityType;
import com.example.contruction_management_app.backend.domain.AuditLog;

import java.time.Instant;

public record AuditLogDto(
    String id,
    String organizationId,
    String projectId,
    String userId,
    String userName,
    AuditAction action,
    AuditEntityType entityType,
    String entityId,
    String details,
    String ipAddress,
    Instant createdAt
) {
    public static AuditLogDto fromEntity(AuditLog a) {
        return new AuditLogDto(
            a.getId(),
            a.getOrganization() != null ? a.getOrganization().getId() : null,
            a.getProject() != null ? a.getProject().getId() : null,
            a.getUserId(),
            a.getUserName(),
            a.getAction(),
            a.getEntityType(),
            a.getEntityId(),
            a.getDetails(),
            a.getIpAddress(),
            a.getCreatedAt()
        );
    }
}
