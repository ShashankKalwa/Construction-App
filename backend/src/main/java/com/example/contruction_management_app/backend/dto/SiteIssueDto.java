package com.example.contruction_management_app.backend.dto;

import com.example.contruction_management_app.backend.domain.IssueSeverity;
import com.example.contruction_management_app.backend.domain.IssueStatus;
import com.example.contruction_management_app.backend.domain.SiteIssue;

import java.time.Instant;

public record SiteIssueDto(
    String id,
    String projectId,
    String reportId,
    String title,
    String description,
    IssueSeverity severity,
    IssueStatus status,
    String assignedTo,
    String createdBy,
    Instant createdAt,
    Instant resolvedAt
) {
    public static SiteIssueDto fromEntity(SiteIssue issue) {
        return new SiteIssueDto(
            issue.getId(),
            issue.getProject().getId(),
            issue.getReport() != null ? issue.getReport().getId() : null,
            issue.getTitle(),
            issue.getDescription(),
            issue.getSeverity(),
            issue.getStatus(),
            issue.getAssignedTo(),
            issue.getCreatedBy(),
            issue.getCreatedAt(),
            issue.getResolvedAt()
        );
    }
}
