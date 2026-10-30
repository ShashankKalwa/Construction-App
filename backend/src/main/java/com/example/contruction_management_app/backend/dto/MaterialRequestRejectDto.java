package com.example.contruction_management_app.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record MaterialRequestRejectDto(
    @NotBlank(message = "Rejection reason is required")
    String reason
) {}

