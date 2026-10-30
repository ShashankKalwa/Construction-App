package com.example.contruction_management_app.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record TaskReviewDto(
    @NotBlank String action, // "APPROVE" or "REQUEST_CORRECTIONS"
    String notes
) {}
