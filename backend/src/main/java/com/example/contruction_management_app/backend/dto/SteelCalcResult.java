package com.example.contruction_management_app.backend.dto;

public record SteelCalcResult(
    Double diameterMm,
    Double lengthPerBarM,
    Integer numberOfBars,
    Double totalLengthM,
    Double unitWeightKgPerM,
    Double rawWeightKg,
    Double totalWeightKg,
    Double totalWeightTonnes,
    Integer bundleCount,
    Double wastagePct
) {}
