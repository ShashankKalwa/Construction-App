package com.example.contruction_management_app.backend.dto;

import com.example.contruction_management_app.backend.domain.IssueSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SiteIssueCreateDto(
    String reportId,

    @NotBlank(message = "Title is required")
    String title,

    @NotBlank(message = "Description is required")
    String description,

    @NotNull(message = "Severity is required")
    IssueSeverity severity,

    String assignedTo
) {}
