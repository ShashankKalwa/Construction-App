package com.example.contruction_management_app.domain.model

enum class NotificationType(val displayName: String) {
    LOW_STOCK("Low Stock Alert"),
    STOCK_ALERT("Stock Alert"),
    APPROVAL_REQUIRED("Approval Required"),
    APPROVAL_DECISION("Approval Decision"),
    BILL_SUBMITTED("Bill Submitted"),
    TASK_ASSIGNED("Task Assigned"),
    TASK_REVIEW("Task Progress Review"),
    SAFETY_ALERT("Safety Hazard Alert"),
    DOCUMENT_UPLOADED("Document Uploaded"),
    DOCUMENT_UPDATED("Document Updated"),
    PAYMENT_DISBURSED("Payment Disbursed"),
    REPORT_READY("Report Ready")
}

data class NotificationItem(
    val id: String,
    val organizationId: String,
    val userId: String,
    val projectId: String?,
    val title: String,
    val message: String,
    val type: NotificationType,
    val isRead: Boolean,
    val entityId: String?,
    val actionUrl: String?,
    val createdAt: String
)
