package com.example.contruction_management_app.backend.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

data class LoginRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Password is required")
    val password: String
)

data class RoleDto(
    val id: String,
    val name: String,
    val description: String?,
    val permissions: List<String>
)

data class UserDto(
    val id: String,
    val email: String,
    val fullName: String,
    val avatarUrl: String?,
    val organizationId: String,
    val organizationName: String?,
    val role: RoleDto
)

data class TokenResponse(
    val accessToken: String,
    val tokenType: String = "bearer",
    val user: UserDto
)

data class ProjectCreateDto(
    @field:NotBlank(message = "Project name is required")
    val name: String,

    @field:NotBlank(message = "Project code is required")
    val code: String,

    @field:NotBlank(message = "Location is required")
    val location: String,

    @field:Positive(message = "Budget must be positive")
    val budgetInr: BigDecimal
)

data class ProjectResponseDto(
    val id: String,
    val name: String,
    val code: String,
    val location: String,
    val budgetInr: BigDecimal,
    val status: String,
    val progressPct: Double,
    val organizationId: String
)

data class HealthResponse(
    val status: String,
    val service: String,
    val version: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class UserSummaryDto(
    val id: String,
    val fullName: String,
    val email: String,
    val avatarUrl: String?
)

data class ProjectMemberDto(
    val id: String,
    val user: UserSummaryDto,
    val assignedRole: String?,
    val joinedAt: String
)

data class ProjectDetailDto(
    val id: String,
    val name: String,
    val code: String,
    val location: String,
    val budgetInr: BigDecimal,
    val status: String,
    val progressPct: Double,
    val organizationId: String,
    val members: List<ProjectMemberDto>,
    val phasesCount: Int,
    val tasksCount: Int
)

data class TaskDto(
    val id: String,
    val phaseId: String,
    val phaseName: String?,
    val projectId: String,
    val title: String,
    val description: String?,
    val assignee: UserSummaryDto?,
    val priority: String,
    val status: String,
    val startDate: String?,
    val dueDate: String?,
    val progressPct: Double
)

data class PhaseDto(
    val id: String,
    val projectId: String,
    val name: String,
    val orderIndex: Int,
    val startDate: String?,
    val endDate: String?,
    val status: String,
    val progressPct: Double,
    val tasks: List<TaskDto> = emptyList()
)

data class TaskCreateDto(
    @field:NotBlank(message = "Title is required")
    val title: String,
    val description: String? = null,
    @field:NotBlank(message = "Phase ID is required")
    val phaseId: String,
    val assigneeId: String? = null,
    val priority: String = "MEDIUM",
    val startDate: String? = null,
    val dueDate: String? = null
)

data class TaskProgressUpdateDto(
    val progressPct: Double,
    val status: String? = null
)

data class ProjectMemberAssignDto(
    @field:NotBlank(message = "User ID is required")
    val userId: String,
    @field:NotBlank(message = "Assigned role is required")
    val assignedRole: String
)

data class DashboardSummaryDto(
    val activeProjectsCount: Int,
    val totalBudgetInr: BigDecimal,
    val overallProgressPct: Double,
    val pendingTasksCount: Int,
    val inProgressTasksCount: Int,
    val blockedTasksCount: Int,
    val completedTasksCount: Int,
    val roleMetrics: Map<String, String>
)
