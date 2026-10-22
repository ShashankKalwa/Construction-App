package com.example.contruction_management_app.backend.dto;

public record PlasterCalcResult(
    Double surfaceAreaSqM,
    Double wetVolumeM3,
    Double dryVolumeM3,
    String mortarRatio,
    Double cementBags,
    Double cementKg,
    Double sandTonnes,
    Double sandCft,
    Double wastagePct
) {}
