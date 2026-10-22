package com.example.contruction_management_app.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostDashboardSummaryDto {
    private String projectId;
    private String projectName;
    private String period; // WEEK, MONTH, QUARTER, TOTAL
    private BigDecimal totalBudget;
    private BigDecimal actualCost;
    private BigDecimal budgetUsedPct;
    private BigDecimal committedCost;
    private BigDecimal remainingBudget;
    private BigDecimal projectedFinalCost;
    @Builder.Default
    private List<CategoryCostDto> costBreakdown = new ArrayList<>();
    @Builder.Default
    private List<MaterialConsumptionDto> topMaterialConsumption = new ArrayList<>();
}
