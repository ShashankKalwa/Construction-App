package com.example.contruction_management_app.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MaterialRequestCreateDto(
    @NotBlank(message = "Material ID is required")
    String materialId,

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    BigDecimal quantity,

    LocalDate requiredByDate,
    String phaseOrTaskDesc
) {}
