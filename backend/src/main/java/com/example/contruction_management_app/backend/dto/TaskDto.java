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
    Double progressPct
) {}
