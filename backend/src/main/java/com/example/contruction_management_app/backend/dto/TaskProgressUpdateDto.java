package com.example.contruction_management_app.backend.dto;

public record TaskProgressUpdateDto(
    Double progressPct,
    String status
) {}
