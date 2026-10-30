package com.example.contruction_management_app.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record PhaseCreateDto(
    @NotBlank String name,
    Integer orderIndex,
    String startDate,
    String endDate
) {}
