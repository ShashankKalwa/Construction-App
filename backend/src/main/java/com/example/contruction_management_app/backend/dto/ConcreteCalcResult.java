package com.example.contruction_management_app.backend.dto;

public record ConcreteCalcResult(
    Double wetVolumeM3,
    Double dryVolumeM3,
    String grade,
    String mixRatio,
    Double cementBags,
    Double cementKg,
    Double sandTonnes,
    Double sandCft,
    Double aggregateTonnes,
    Double aggregateCft,
    Double waterLiters,
    Double wastagePct
) {}
