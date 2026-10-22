package com.example.contruction_management_app.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ConcreteCalcRequest(
    @NotNull(message = "Length is required")
    @Positive(message = "Length must be positive")
    Double lengthM,

    @NotNull(message = "Width is required")
    @Positive(message = "Width must be positive")
    Double widthM,

    @NotNull(message = "Depth/Thickness is required")
    @Positive(message = "Depth must be positive")
    Double depthM,

    String grade,           // M10, M15, M20, M25 (default M20)
    Double wastagePct,      // default 5.0%
    String elementType      // SLAB, BEAM, COLUMN, FOOTING, STAIRCASE
) {}
