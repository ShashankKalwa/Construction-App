package com.example.contruction_management_app.backend.dto;

import com.example.contruction_management_app.backend.domain.IssueStatus;
import jakarta.validation.constraints.NotNull;

public record SiteIssueStatusUpdateDto(
    @NotNull(message = "Status is required (OPEN, IN_PROGRESS, RESOLVED)")
    IssueStatus status
) {}
