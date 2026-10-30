package com.example.contruction_management_app.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record MilestoneStatusUpdateDto(
    @NotBlank String status
) {}
