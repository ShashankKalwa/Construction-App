package com.example.contruction_management_app.backend.dto;

import java.math.BigDecimal;
import java.util.Map;

public record DashboardSummaryDto(
    int activeProjectsCount,
    BigDecimal totalBudgetInr,
    Double overallProgressPct,
    int pendingTasksCount,
    int inProgressTasksCount,
    int blockedTasksCount,
    int completedTasksCount,
    Map<String, String> roleMetrics
) {}
