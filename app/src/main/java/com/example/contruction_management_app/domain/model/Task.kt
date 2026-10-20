package com.example.contruction_management_app.domain.model

enum class TaskPriority(val displayName: String) {
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low")
}

enum class TaskStatus(val displayName: String) {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    BLOCKED("Blocked"),
    COMPLETED("Completed")
}

data class Task(
    val id: String,
    val phaseId: String,
    val phaseName: String?,
    val projectId: String,
    val title: String,
    val description: String?,
    val assignee: UserSummary?,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val status: TaskStatus = TaskStatus.PENDING,
    val startDate: String?,
    val dueDate: String?,
    val progressPct: Double = 0.0
)

data class Phase(
    val id: String,
    val projectId: String,
    val name: String,
    val orderIndex: Int,
    val startDate: String?,
    val endDate: String?,
    val status: String = "Active",
    val progressPct: Double = 0.0,
    val tasks: List<Task> = emptyList()
)

data class DashboardSummary(
    val activeProjectsCount: Int = 1,
    val totalBudgetDisplay: String = "₹2.50 Cr",
    val overallProgressPct: Double = 35.0,
    val pendingTasksCount: Int = 2,
    val inProgressTasksCount: Int = 2,
    val blockedTasksCount: Int = 1,
    val completedTasksCount: Int = 4,
    val roleMetrics: Map<String, String> = emptyMap()
)
