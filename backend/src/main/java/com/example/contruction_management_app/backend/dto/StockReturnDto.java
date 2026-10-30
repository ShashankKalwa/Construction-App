package com.example.contruction_management_app.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record StockReturnDto(
    @NotNull(message = "Return quantity is required")
    @DecimalMin(value = "0.01", message = "Return quantity must be greater than zero")
    BigDecimal quantity,

    String referenceId,
    String notes
) {}

