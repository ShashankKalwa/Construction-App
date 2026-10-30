package com.example.contruction_management_app.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record WorkProgressVerifyDto(
    @NotBlank String action, // VERIFY, REJECT
    @NotNull BigDecimal verifiedQuantity,
    String notes
) {}
