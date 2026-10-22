package com.example.contruction_management_app.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SteelCalcRequest(
    @NotNull(message = "Bar diameter in mm is required")
    @Positive(message = "Diameter must be positive")
    Double diameterMm,

    @NotNull(message = "Length per bar in meters is required")
    @Positive(message = "Length must be positive")
    Double lengthM,

    @NotNull(message = "Number of bars is required")
    @Positive(message = "Number of bars must be positive")
    Integer numberOfBars,

    Double wastagePct,      // default 3.0%
    String elementType      // COLUMN, BEAM, SLAB, FOOTING, TIE_BEAM
) {}
