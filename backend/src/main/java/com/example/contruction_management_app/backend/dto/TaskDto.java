package com.example.contruction_management_app.backend.dto;

public record TaskDto(
    String id,
    String phaseId,
    String phaseName,
    String projectId,
    String title,
    String description,
    UserSummaryDto assignee,
    String priority,
    String status,
    String startDate,
    String dueDate,
    Double progressPct,
    String reviewStatus,
    UserSummaryDto reviewedBy,
    String reviewNotes,
    String reviewedAt
) {
    public TaskDto(
        String id,
        String phaseId,
        String phaseName,
        String projectId,
        String title,
        String description,
        UserSummaryDto assignee,
        String priority,
        String status,
        String startDate,
        String dueDate,
        Double progressPct
    ) {
        this(id, phaseId, phaseName, projectId, title, description, assignee, priority, status, startDate, dueDate, progressPct, "NONE", null, null, null);
    }
}
