package com.example.contruction_management_app.domain.model

enum class NotificationType(val displayName: String) {
    LOW_STOCK("Low Stock Alert"),
    APPROVAL_REQUIRED("Approval Required"),
    BILL_SUBMITTED("Bill Submitted"),
    TASK_ASSIGNED("Task Assigned"),
    SAFETY_ALERT("Safety Hazard Alert"),
    DOCUMENT_UPLOADED("Document Uploaded")
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
