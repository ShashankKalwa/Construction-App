package com.example.contruction_management_app.backend.dto;

public record PaintCalcResult(
    Double wallAreaSqM,
    Double ceilingAreaSqM,
    Double totalAreaSqM,
    Integer numberOfCoats,
    Double primerLiters,
    Double paintLiters,
    Integer cans20L,
    Integer cans4L,
    Integer cans1L,
    Double wastagePct
) {}
