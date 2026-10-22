package com.example.contruction_management_app.backend.dto;

import com.example.contruction_management_app.backend.domain.MaterialCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record MaterialCreateDto(
    @NotBlank(message = "Material code is required")
    String code,

    @NotBlank(message = "Material name is required")
    String name,

    @NotNull(message = "Category is required")
    MaterialCategory category,

    @NotBlank(message = "Unit is required")
    String unit,

    @PositiveOrZero(message = "Initial stock must be zero or positive")
    BigDecimal initialStock,

    @PositiveOrZero(message = "Minimum stock threshold must be zero or positive")
    BigDecimal minStockThreshold,

    @PositiveOrZero(message = "Unit cost must be zero or positive")
    BigDecimal unitCost
) {}
