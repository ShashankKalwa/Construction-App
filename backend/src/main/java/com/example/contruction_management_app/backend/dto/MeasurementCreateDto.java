package com.example.contruction_management_app.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record MeasurementCreateDto(
    String boqItemId,

    @NotBlank(message = "Item description is required")
    String itemDescription,

    @NotNull(message = "Number of units is required")
    @Positive(message = "Units must be positive")
    BigDecimal numberOfUnits,

    BigDecimal lengthM,
    BigDecimal widthM,
    BigDecimal depthM,
    BigDecimal deductionM3,

    @NotBlank(message = "Unit is required")
    String unit,

    String locationDetails
) {}
