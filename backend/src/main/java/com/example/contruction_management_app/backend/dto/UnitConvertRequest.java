package com.example.contruction_management_app.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UnitConvertRequest(
    @NotBlank(message = "Category is required (LINEAR, AREA, VOLUME, MASS)")
    String category,

    @NotNull(message = "Value is required")
    Double value,

    @NotBlank(message = "From unit is required")
    String fromUnit,

    @NotBlank(message = "To unit is required")
    String toUnit
) {}
