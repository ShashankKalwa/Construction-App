package com.example.contruction_management_app.backend.dto;

import java.math.BigDecimal;

public record ProjectResponseDto(
    String id,
    String name,
    String code,
    String location,
    BigDecimal budgetInr,
    String status,
    Double progressPct,
    String organizationId
) {}
