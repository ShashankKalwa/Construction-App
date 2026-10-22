package com.example.contruction_management_app.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record BoqItemCreateDto(
    @NotBlank(message = "Item code is required")
    String itemCode,

    @NotBlank(message = "Description is required")
    String description,

    @NotBlank(message = "Category is required")
    String category,

    @NotBlank(message = "Unit is required")
    String unit,

    @NotNull(message = "Estimated quantity is required")
    @Positive(message = "Estimated quantity must be positive")
    BigDecimal estimatedQuantity,

    @NotNull(message = "Unit rate is required")
    @Positive(message = "Unit rate must be positive")
    BigDecimal unitRate,

    String calculationId
) {}
