package com.example.contruction_management_app.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record MilestoneCreateDto(
    @NotBlank String title,
    String dueDate,
    Double targetCompletionPct
) {}
