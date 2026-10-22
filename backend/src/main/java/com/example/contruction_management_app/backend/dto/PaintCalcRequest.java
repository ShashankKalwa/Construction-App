package com.example.contruction_management_app.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaintCalcRequest(
    @NotNull(message = "Room length in meters is required")
    @Positive(message = "Length must be positive")
    Double lengthM,

    @NotNull(message = "Room width in meters is required")
    @Positive(message = "Width must be positive")
    Double widthM,

    @NotNull(message = "Room height in meters is required")
    @Positive(message = "Height must be positive")
    Double heightM,

    Double openingsAreaSqM,         // Door & window openings in sq.m (default 0)
    Boolean includeCeiling,         // Include ceiling in painting (default true)
    Integer numberOfCoats,          // 1, 2, or 3 (default 2)
    Boolean includePrimer,          // default true
    Double coverageRateSqMPerLiter, // default 10.0 sq.m / liter
    Double wastagePct               // default 5.0%
) {}
