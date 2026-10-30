package com.example.contruction_management_app.backend.dto;

public record MilestoneDto(
    String id,
    String projectId,
    String title,
    String dueDate,
    String status,
    Double targetCompletionPct,
    String createdAt
) {}
