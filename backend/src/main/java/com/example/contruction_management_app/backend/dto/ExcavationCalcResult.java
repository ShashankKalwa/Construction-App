package com.example.contruction_management_app.backend.dto;

public record ExcavationCalcResult(
    Double pitVolumeM3,
    Double looseVolumeM3,
    Double bulkingFactor,
    Double compactedBackfillM3,
    Double compactionFactor,
    Double truckCapacityM3,
    Integer truckTripsRequired
) {}
