package com.example.contruction_management_app.backend.dto;

public record BrickworkCalcResult(
    Double grossWallVolumeM3,
    Double deductionsVolumeM3,
    Double netWallVolumeM3,
    Double brickCount,
    Double mortarDryVolumeM3,
    String mortarRatio,
    Double cementBags,
    Double cementKg,
    Double sandTonnes,
    Double sandCft,
    Double wastagePct
) {}
