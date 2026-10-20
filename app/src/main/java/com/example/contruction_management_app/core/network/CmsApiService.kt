package com.example.contruction_management_app.core.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*
import java.math.BigDecimal

data class ProjectNetworkDto(
    val id: String,
    val name: String,
    val code: String,
    val location: String,
    val budgetInr: BigDecimal,
    val status: String,
    val progressPct: Double,
    val organizationId: String
)

data class UserSummaryNetworkDto(
    val id: String,
    val fullName: String,
    val email: String,
    val avatarUrl: String?
)

data class ProjectMemberNetworkDto(
    val id: String,
    val user: UserSummaryNetworkDto,
    val assignedRole: String?,
    val joinedAt: String
)

data class ProjectDetailNetworkDto(
    val id: String,
    val name: String,
    val code: String,
    val location: String,
    val budgetInr: BigDecimal,
    val status: String,
    val progressPct: Double,
    val organizationId: String,
    val members: List<ProjectMemberNetworkDto>,
    val phasesCount: Int,
    val tasksCount: Int
)

data class TaskNetworkDto(
    val id: String,
    val phaseId: String,
    val phaseName: String?,
    val projectId: String,
    val title: String,
    val description: String?,
    val assignee: UserSummaryNetworkDto?,
    val priority: String,
    val status: String,
    val startDate: String?,
    val dueDate: String?,
    val progressPct: Double
)

data class PhaseNetworkDto(
    val id: String,
    val projectId: String,
    val name: String,
    val orderIndex: Int,
    val startDate: String?,
    val endDate: String?,
    val status: String,
    val progressPct: Double,
    val tasks: List<TaskNetworkDto> = emptyList()
)

data class CreateProjectRequest(
    val name: String,
    val code: String,
    val location: String,
    val budgetInr: BigDecimal
)

data class CreateTaskRequest(
    val title: String,
    val description: String? = null,
    val phaseId: String,
    val assigneeId: String? = null,
    val priority: String = "MEDIUM",
    val startDate: String? = null,
    val dueDate: String? = null
)

data class UpdateTaskProgressRequest(
    val progressPct: Double,
    val status: String? = null
)

data class DashboardSummaryNetworkDto(
    val activeProjectsCount: Int,
    val totalBudgetInr: BigDecimal,
    val overallProgressPct: Double,
    val pendingTasksCount: Int,
    val inProgressTasksCount: Int,
    val blockedTasksCount: Int,
    val completedTasksCount: Int,
    val roleMetrics: Map<String, String>
)

interface CmsApiService {
    @GET("projects")
    suspend fun getProjects(): Response<List<ProjectNetworkDto>>

    @POST("projects")
    suspend fun createProject(@Body request: CreateProjectRequest): Response<ProjectNetworkDto>

    @GET("projects/{id}/detail")
    suspend fun getProjectDetail(@Path("id") id: String): Response<ProjectDetailNetworkDto>

    @GET("projects/{projectId}/phases")
    suspend fun getPhasesWithTasks(@Path("projectId") projectId: String): Response<List<PhaseNetworkDto>>

    @POST("projects/{projectId}/tasks")
    suspend fun createTask(
        @Path("projectId") projectId: String,
        @Body request: CreateTaskRequest
    ): Response<TaskNetworkDto>

    @PATCH("tasks/{taskId}/progress")
    suspend fun updateTaskProgress(
        @Path("taskId") taskId: String,
        @Body request: UpdateTaskProgressRequest
    ): Response<TaskNetworkDto>

    @GET("dashboard/summary")
    suspend fun getDashboardSummary(
        @Query("projectId") projectId: String? = null
    ): Response<DashboardSummaryNetworkDto>
}
