package com.example.contruction_management_app.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProjectDetailDto(
    String id,
    String name,
    String code,
    String location,
    BigDecimal budgetInr,
    String status,
    Double progressPct,
    String organizationId,
    List<ProjectMemberDto> members,
    int phasesCount,
    int tasksCount
) {}
