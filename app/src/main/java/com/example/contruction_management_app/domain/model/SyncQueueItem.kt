package com.example.contruction_management_app.domain.model

enum class SyncStatus(val displayName: String) {
    PENDING_SYNC("Pending Sync"),
    SYNCING("Syncing..."),
    CONFIRMED("Confirmed"),
    FAILED("Sync Failed")
}

enum class SyncActionType(val displayName: String) {
    CREATE_DSR("Daily Site Report"),
    CREATE_TASK("New Task"),
    UPDATE_TASK_PROGRESS("Task Progress"),
    LOG_MEASUREMENT("Measurement Entry"),
    SUBMIT_EXPENSE("Site Expense Draft"),
    REQUEST_MATERIAL("Material Requisition")
}

data class SyncQueueItem(
    val id: String,
    val projectId: String,
    val actionType: SyncActionType,
    val payloadJson: String,
    val title: String,
    val subtitle: String,
    val timestamp: Long,
    val status: SyncStatus,
    val retryCount: Int = 0,
    val errorMessage: String? = null
)
