package com.example.contruction_management_app.domain.model

import java.math.BigDecimal

enum class DashboardPeriod {
    WEEK,
    MONTH,
    QUARTER,
    TOTAL
}

data class CategoryCost(
    val category: String,
    val categoryName: String,
    val budgetAmount: BigDecimal,
    val actualAmount: BigDecimal,
    val percentage: BigDecimal
)

data class MaterialConsumption(
    val materialId: String,
    val materialName: String,
    val unit: String,
    val quantityConsumed: BigDecimal,
    val totalCost: BigDecimal,
    val consumptionPercentage: BigDecimal
)

data class CostDashboardSummary(
    val projectId: String,
    val projectName: String,
    val period: DashboardPeriod,
    val totalBudget: BigDecimal,
    val actualCost: BigDecimal,
    val budgetUsedPct: BigDecimal,
    val committedCost: BigDecimal,
    val remainingBudget: BigDecimal,
    val projectedFinalCost: BigDecimal,
    val costBreakdown: List<CategoryCost> = emptyList(),
    val topMaterialConsumption: List<MaterialConsumption> = emptyList()
)
