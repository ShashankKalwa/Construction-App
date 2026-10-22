package com.example.contruction_management_app.backend.dto;

import com.example.contruction_management_app.backend.domain.CalculatorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CalculationSaveRequest(
    @NotBlank(message = "Project ID is required")
    String projectId,

    @NotNull(message = "Calculator type is required")
    CalculatorType calculatorType,

    @NotBlank(message = "Calculation title is required")
    String title,

    String elementType,

    @NotBlank(message = "Input parameters JSON string is required")
    String inputParameters,

    @NotBlank(message = "Output results JSON string is required")
    String outputResults,

    @NotNull(message = "Primary quantity is required")
    Double primaryQuantity,

    @NotBlank(message = "Primary unit is required")
    String primaryUnit
) {}
