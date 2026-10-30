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

enum class DsrStatus {
    DRAFT,
    SUBMITTED,
    APPROVED,
    REJECTED
}

data class DailySiteReport(
    val id: String,
    val projectId: String,
    val reportDate: String,
    val weather: WeatherCondition,
    val workSummary: String,
    val plannedTomorrow: String? = null,
    val status: DsrStatus = DsrStatus.SUBMITTED,
    val approvedBy: String? = null,
    val approvedAt: String? = null,
    val approvalNotes: String? = null,
    val createdBy: String? = null,
    val laborAttendances: List<LaborAttendance> = emptyList(),
    val totalHeadcount: Int,
    val createdAt: String
)

enum class WorkProgressStatus {
    REPORTED,
    VERIFIED,
    REJECTED
}

data class WorkProgress(
    val id: String,
    val projectId: String,
    val taskId: String? = null,
    val reportId: String? = null,
    val itemName: String,
    val unit: String,
    val plannedQuantity: BigDecimal,
    val reportedQuantity: BigDecimal,
    val verifiedQuantity: BigDecimal = BigDecimal.ZERO,
    val status: WorkProgressStatus = WorkProgressStatus.REPORTED,
    val reportedBy: String,
    val verifiedBy: String? = null,
    val verificationNotes: String? = null,
    val recordedAt: String,
    val verifiedAt: String? = null
)

enum class InspectionType {
    SAFETY,
    QUALITY,
    ENVIRONMENTAL,
    STRUCTURAL,
    MEP
}

enum class InspectionStatus {
    PASSED,
    FAILED,
    CONDITIONALLY_PASSED
}

data class SiteInspection(
    val id: String,
    val projectId: String,
    val title: String,
    val inspectionType: InspectionType,
    val status: InspectionStatus,
    val checklistData: String = "[]",
    val inspectorId: String,
    val inspectionDate: String,
    val location: String? = null,
    val notes: String? = null,
    val createdAt: String
)

enum class DefectStatus {
    OPEN,
    RESOLVED,
    CLOSED
}

data class SiteDefect(
    val id: String,
    val projectId: String,
    val inspectionId: String? = null,
    val title: String,
    val description: String,
    val location: String? = null,
    val severity: IssueSeverity = IssueSeverity.MEDIUM,
    val status: DefectStatus = DefectStatus.OPEN,
    val assignedTo: String? = null,
    val deadline: String? = null,
    val createdBy: String,
    val createdAt: String,
    val resolvedAt: String? = null,
    val reInspectedBy: String? = null,
    val reInspectedAt: String? = null,
    val reInspectionNotes: String? = null
)

data class SitePhoto(
    val id: String,
    val projectId: String,
    val reportId: String? = null,
    val taskId: String? = null,
    val defectId: String? = null,
    val photoUrl: String,
    val caption: String? = null,
    val locationTag: String? = null,
    val createdAt: String
)

// Legacy compatibility
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
