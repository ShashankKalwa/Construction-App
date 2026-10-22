package com.example.contruction_management_app.backend.dto;

import com.example.contruction_management_app.backend.domain.Notification;
import com.example.contruction_management_app.backend.domain.NotificationType;

import java.time.Instant;

public record NotificationDto(
    String id,
    String organizationId,
    String userId,
    String projectId,
    String title,
    String message,
    NotificationType type,
    Boolean isRead,
    String entityId,
    String actionUrl,
    Instant createdAt
) {
    public static NotificationDto fromEntity(Notification n) {
        return new NotificationDto(
            n.getId(),
            n.getOrganization() != null ? n.getOrganization().getId() : null,
            n.getUser() != null ? n.getUser().getId() : null,
            n.getProject() != null ? n.getProject().getId() : null,
            n.getTitle(),
            n.getMessage(),
            n.getType(),
            n.getIsRead(),
            n.getEntityId(),
            n.getActionUrl(),
            n.getCreatedAt()
        );
    }
}
