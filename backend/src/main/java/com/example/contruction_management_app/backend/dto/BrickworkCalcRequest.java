package com.example.contruction_management_app.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BrickworkCalcRequest(
    @NotNull(message = "Wall length in meters is required")
    @Positive(message = "Wall length must be positive")
    Double lengthM,

    @NotNull(message = "Wall height in meters is required")
    @Positive(message = "Wall height must be positive")
    Double heightM,

    @NotNull(message = "Wall thickness in meters is required (e.g., 0.23 for 9-inch, 0.115 for 4.5-inch)")
    @Positive(message = "Wall thickness must be positive")
    Double thicknessM,

    Double openingsAreaSqM, // Total area of doors, windows to deduct (default 0)
    String mortarRatio,     // 1:4 or 1:6 (default 1:6)
    Double wastagePct       // default 5.0%
) {}
