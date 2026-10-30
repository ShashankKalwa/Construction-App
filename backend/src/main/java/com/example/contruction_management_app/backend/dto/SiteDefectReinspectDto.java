package com.example.contruction_management_app.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record SiteDefectReinspectDto(
    @NotBlank String action, // CLOSE, REJECT
    String notes
) {}
