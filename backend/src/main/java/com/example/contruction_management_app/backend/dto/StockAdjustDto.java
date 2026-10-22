package com.example.contruction_management_app.backend.dto;

import com.example.contruction_management_app.backend.domain.MovementType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record StockAdjustDto(
    @NotNull(message = "Movement type is required")
    MovementType movementType,

    @NotNull(message = "Quantity is required")
    BigDecimal quantity,

    String referenceId,
    String notes
) {}
