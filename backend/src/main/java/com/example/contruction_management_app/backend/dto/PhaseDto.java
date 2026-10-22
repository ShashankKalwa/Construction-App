package com.example.contruction_management_app.backend.dto;

import java.util.List;

public record PhaseDto(
    String id,
    String projectId,
    String name,
    int orderIndex,
    String startDate,
    String endDate,
    String status,
    Double progressPct,
    List<TaskDto> tasks
) {}
