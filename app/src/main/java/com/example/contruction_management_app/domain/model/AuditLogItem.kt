package com.example.contruction_management_app.domain.model

enum class AuditAction(val displayName: String) {
    CREATE("Created"),
    UPDATE("Updated"),
    DELETE("Deleted"),
    APPROVE("Approved"),
    REJECT("Rejected"),
    ISSUE_STOCK("Issued Stock"),
    PAY_BILL("Paid Bill")
}

enum class AuditEntityType(val displayName: String) {
    BOQ("BOQ Revision"),
    EXPENSE("Expense"),
    CONTRACTOR_BILL("Contractor Bill"),
    STOCK("Inventory Stock"),
    TASK("Task"),
    DOCUMENT("Document"),
    PROJECT("Project")
}

data class AuditLogItem(
    val id: String,
    val organizationId: String,
    val projectId: String?,
    val userId: String,
    val userName: String,
    val action: AuditAction,
    val entityType: AuditEntityType,
    val entityId: String,
    val details: String?,
    val ipAddress: String?,
    val createdAt: String
)

data class AuditSummary(
    val totalEvents: Long,
    val approvalsCount: Long,
    val stockIssuesCount: Long,
    val documentsCount: Long,
    val paymentsCount: Long
)
