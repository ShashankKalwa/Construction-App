package com.example.contruction_management_app.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ExcavationCalcRequest(
    @NotNull(message = "Excavation length in meters is required")
    @Positive(message = "Length must be positive")
    Double lengthM,

    @NotNull(message = "Excavation width in meters is required")
    @Positive(message = "Width must be positive")
    Double widthM,

    @NotNull(message = "Excavation depth in meters is required")
    @Positive(message = "Depth must be positive")
    Double depthM,

    Double bulkingFactor,       // Soil swell factor: default 1.25 (range 1.15 - 1.40)
    Double compactionFactor,    // Soil compaction factor: default 0.85
    Double truckCapacityM3      // Tipper truck volume capacity: default 10.0 m³
) {}
