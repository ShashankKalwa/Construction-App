package com.example.contruction_management_app.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PlasterCalcRequest(
    @NotNull(message = "Length in meters is required")
    @Positive(message = "Length must be positive")
    Double lengthM,

    @NotNull(message = "Height in meters is required")
    @Positive(message = "Height must be positive")
    Double heightM,

    @NotNull(message = "Plaster thickness in mm is required (e.g. 12, 15, 20)")
    @Positive(message = "Thickness must be positive")
    Double thicknessMm,

    Double openingsAreaSqM,     // Openings/deductions in sq.m (default 0)
    String mortarRatio,         // 1:3, 1:4, 1:6 (default 1:4)
    Double wastagePct           // default 5.0%
) {}
