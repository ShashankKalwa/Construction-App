package com.example.contruction_management_app.domain.model

import java.math.BigDecimal

enum class WeatherCondition {
    SUNNY,
    RAINY,
    CLOUDY,
    STORMY,
    WINDY
}

enum class LaborTrade {
    MASON,
    CARPENTER,
    STEEL_FIXER,
    ELECTRICIAN,
    PLUMBER,
    HELPER,
    OPERATOR,
    PAINTER
}

data class LaborAttendance(
    val id: String? = null,
    val trade: LaborTrade,
    val headcount: Int,
    val hoursWorked: BigDecimal? = null,
    val contractorName: String? = null
)

data class DailySiteReport(
    val id: String,
    val projectId: String,
    val reportDate: String,
    val weather: WeatherCondition,
    val workSummary: String,
    val plannedTomorrow: String? = null,
    val createdBy: String? = null,
    val laborAttendances: List<LaborAttendance> = emptyList(),
    val totalHeadcount: Int,
    val createdAt: String
)

enum class IssueSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class IssueStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED
}

data class SiteIssue(
    val id: String,
    val projectId: String,
    val reportId: String? = null,
    val title: String,
    val description: String? = null,
    val severity: IssueSeverity,
    val status: IssueStatus,
    val assignedTo: String? = null,
    val createdBy: String? = null,
    val createdAt: String,
    val resolvedAt: String? = null
)
