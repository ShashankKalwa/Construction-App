package com.example.contruction_management_app.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record TaskCreateDto(
    @NotBlank(message = "Title is required")
    String title,
    String description,
    @NotBlank(message = "Phase ID is required")
    String phaseId,
    String assigneeId,
    String priority,
    String startDate,
    String dueDate
) {}
