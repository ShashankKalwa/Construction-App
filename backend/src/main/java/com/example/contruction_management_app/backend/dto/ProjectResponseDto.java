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
    String organizationId,
    String projectType,
    String clientName,
    String startDate,
    String endDate
) {
    public ProjectResponseDto(
        String id,
        String name,
        String code,
        String location,
        BigDecimal budgetInr,
        String status,
        Double progressPct,
        String organizationId
    ) {
        this(id, name, code, location, budgetInr, status, progressPct, organizationId, "Commercial", "Chaitanya Developments", null, null);
    }
}
