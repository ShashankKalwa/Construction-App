package com.example.contruction_management_app.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record ProjectCreateDto(
    @NotBlank(message = "Project name is required")
    String name,

    @NotBlank(message = "Project code is required")
    String code,

    @NotBlank(message = "Location is required")
    String location,

    @NotNull(message = "Budget is required")
    @Positive(message = "Budget must be positive")
    BigDecimal budgetInr
) {}
