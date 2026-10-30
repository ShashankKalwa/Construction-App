package com.example.contruction_management_app.domain.repository

import android.content.Context
import com.example.contruction_management_app.core.auth.SessionDataStore
import com.example.contruction_management_app.core.network.*
import com.example.contruction_management_app.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.math.BigDecimal

interface ProjectRepository {
    suspend fun getProjects(): Result<List<Project>>
    suspend fun getProjectDetail(projectId: String): Result<ProjectDetail>
    suspend fun createProject(
        name: String,
        code: String,
        location: String,
        budgetInr: BigDecimal,
        projectType: String? = "Commercial",
        clientName: String? = "Apex Developments",
        startDate: String? = null,
        endDate: String? = null
    ): Result<Project>
    suspend fun updateProject(
        id: String,
        name: String,
        location: String? = null,
        budgetInr: BigDecimal? = null,
        projectType: String? = null,
        clientName: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): Result<Project>
    suspend fun updateProjectStatus(id: String, status: String): Result<Project>
    suspend fun getProjectMembers(projectId: String): Result<List<ProjectMember>>
    suspend fun assignMember(projectId: String, userId: String, assignedRole: String): Result<ProjectMember>
    suspend fun removeMember(projectId: String, userId: String): Result<Unit>
    suspend fun getPhasesWithTasks(projectId: String): Result<List<Phase>>
    suspend fun createPhase(projectId: String, name: String, orderIndex: Int? = null, startDate: String? = null, endDate: String? = null): Result<Phase>
    suspend fun updateTaskProgress(taskId: String, progressPct: Double, status: TaskStatus?): Result<Task>
    suspend fun submitTaskReview(taskId: String, progressPct: Double?, notes: String?): Result<Task>
    suspend fun reviewTask(taskId: String, action: String, notes: String?): Result<Task>
    suspend fun getMilestones(projectId: String): Result<List<Milestone>>
    suspend fun createMilestone(projectId: String, title: String, dueDate: String?, targetCompletionPct: Double? = 0.0): Result<Milestone>
    suspend fun updateMilestoneStatus(milestoneId: String, status: String): Result<Milestone>
    suspend fun getDashboardSummary(projectId: String?): Result<DashboardSummary>
    fun getActiveProjectId(): Flow<String?>
    suspend fun setActiveProjectId(projectId: String)
    fun getCachedProjects(): List<Project>
    fun getCachedDashboardSummary(): DashboardSummary
}

class ProjectRepositoryImpl(
    private val context: Context,
    private val apiService: CmsApiService = NetworkClient.getApiService(context),
    private val sessionDataStore: SessionDataStore = SessionDataStore(context)
) : ProjectRepository {

    // In-memory offline cache fallback
    private var cachedProjects: MutableList<Project> = mutableListOf(
        Project(
            id = "prj_green_valley_residency",
            name = "Green Valley Residency",
            code = "GVR-01",
            location = "Sector 42, Gurgaon",
            budgetInr = BigDecimal("25000000.00"),
            status = "Active",
            progressPct = 35.0,
            organizationId = "org_apex_infra",
            projectType = "Residential",
            clientName = "Apex Developments",
            startDate = "2026-01-01",
            endDate = "2026-12-31"
        ),
        Project(
            id = "prj_commercial_tower_b",
            name = "Skyline Business Park - Tower B",
            code = "SBP-02",
            location = "Whitefield, Bangalore",
            budgetInr = BigDecimal("68000000.00"),
            status = "Active",
            progressPct = 18.0,
            organizationId = "org_apex_infra",
            projectType = "Commercial",
            clientName = "City Corp",
            startDate = "2026-02-15",
            endDate = "2027-06-30"
        )
    )

    private var cachedPhases: MutableList<Phase> = mutableListOf()
    private var cachedMilestones: MutableList<Milestone> = mutableListOf(
        Milestone("mls_01", "prj_green_valley_residency", "Foundation & Raft Concrete Completion", "2026-03-15", "REACHED", 25.0),
        Milestone("mls_02", "prj_green_valley_residency", "Tower A & B RCC Superstructure Top-out", "2026-07-30", "PENDING", 65.0),
        Milestone("mls_03", "prj_green_valley_residency", "External Facade & AAC Masonry Completion", "2026-10-15", "PENDING", 85.0)
    )

    override suspend fun getProjects(): Result<List<Project>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProjects()
            if (response.isSuccessful && response.body() != null) {
                val list = response.body()!!.map { it.toDomain() }
                if (list.isNotEmpty()) {
                    cachedProjects = list.toMutableList()
                }
                Result.success(cachedProjects)
            } else {
                Result.success(cachedProjects)
            }
        } catch (e: Exception) {
            Result.success(cachedProjects)
        }
    }

    override suspend fun getProjectDetail(projectId: String): Result<ProjectDetail> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProjectDetail(projectId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                fallbackProjectDetail(projectId)
            }
        } catch (e: Exception) {
            fallbackProjectDetail(projectId)
        }
    }

    override suspend fun createProject(
        name: String,
        code: String,
        location: String,
        budgetInr: BigDecimal,
        projectType: String?,
        clientName: String?,
        startDate: String?,
        endDate: String?
    ): Result<Project> = withContext(Dispatchers.IO) {
        try {
            val request = CreateProjectRequest(
                name = name,
                code = code,
                location = location,
                budgetInr = budgetInr,
                projectType = projectType,
                clientName = clientName,
                startDate = startDate,
                endDate = endDate
            )
            val response = apiService.createProject(request)
            if (response.isSuccessful && response.body() != null) {
                val created = response.body()!!.toDomain()
                cachedProjects.add(0, created)
                Result.success(created)
            } else {
                val local = Project(
                    id = "prj_${System.currentTimeMillis()}",
                    name = name,
                    code = code,
                    location = location,
                    budgetInr = budgetInr,
                    status = "Planning",
                    progressPct = 0.0,
                    organizationId = "org_apex_infra",
                    projectType = projectType ?: "Commercial",
                    clientName = clientName ?: "Apex Developments",
                    startDate = startDate,
                    endDate = endDate
                )
                cachedProjects.add(0, local)
                Result.success(local)
            }
        } catch (e: Exception) {
            val local = Project(
                id = "prj_${System.currentTimeMillis()}",
                name = name,
                code = code,
                location = location,
                budgetInr = budgetInr,
                status = "Planning",
                progressPct = 0.0,
                organizationId = "org_apex_infra",
                projectType = projectType ?: "Commercial",
                clientName = clientName ?: "Apex Developments",
                startDate = startDate,
                endDate = endDate
            )
            cachedProjects.add(0, local)
            Result.success(local)
        }
    }

    override suspend fun updateProject(
        id: String,
        name: String,
        location: String?,
        budgetInr: BigDecimal?,
        projectType: String?,
        clientName: String?,
        startDate: String?,
        endDate: String?
    ): Result<Project> = withContext(Dispatchers.IO) {
        try {
            val request = UpdateProjectRequest(name, location, budgetInr, projectType, clientName, startDate, endDate)
            val response = apiService.updateProject(id, request)
            if (response.isSuccessful && response.body() != null) {
                val updated = response.body()!!.toDomain()
                val idx = cachedProjects.indexOfFirst { it.id == id }
                if (idx != -1) cachedProjects[idx] = updated
                Result.success(updated)
            } else {
                val existing = cachedProjects.find { it.id == id }
                if (existing != null) {
                    val updated = existing.copy(
                        name = name,
                        location = location ?: existing.location,
                        budgetInr = budgetInr ?: existing.budgetInr,
                        projectType = projectType ?: existing.projectType,
                        clientName = clientName ?: existing.clientName,
                        startDate = startDate ?: existing.startDate,
                        endDate = endDate ?: existing.endDate
                    )
                    val idx = cachedProjects.indexOf(existing)
                    cachedProjects[idx] = updated
                    Result.success(updated)
                } else {
                    Result.failure(Exception("Project not found"))
                }
            }
        } catch (e: Exception) {
            val existing = cachedProjects.find { it.id == id }
            if (existing != null) Result.success(existing) else Result.failure(e)
        }
    }

    override suspend fun updateProjectStatus(id: String, status: String): Result<Project> = withContext(Dispatchers.IO) {
        try {
            val request = UpdateProjectStatusRequest(status)
            val response = apiService.updateProjectStatus(id, request)
            if (response.isSuccessful && response.body() != null) {
                val updated = response.body()!!.toDomain()
                val idx = cachedProjects.indexOfFirst { it.id == id }
                if (idx != -1) cachedProjects[idx] = updated
                Result.success(updated)
            } else {
                val existing = cachedProjects.find { it.id == id }
                if (existing != null) {
                    val updated = existing.copy(status = status)
                    val idx = cachedProjects.indexOf(existing)
                    cachedProjects[idx] = updated
                    Result.success(updated)
                } else {
                    Result.failure(Exception("Project not found"))
                }
            }
        } catch (e: Exception) {
            val existing = cachedProjects.find { it.id == id }
            if (existing != null) Result.success(existing) else Result.failure(e)
        }
    }

    override suspend fun getProjectMembers(projectId: String): Result<List<ProjectMember>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProjectMembers(projectId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.map { it.toDomain() })
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.success(emptyList())
        }
    }

    override suspend fun assignMember(projectId: String, userId: String, assignedRole: String): Result<ProjectMember> = withContext(Dispatchers.IO) {
        try {
            val request = AssignMemberRequest(userId, assignedRole)
            val response = apiService.assignProjectMember(projectId, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("Failed to assign member"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeMember(projectId: String, userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.removeProjectMember(projectId, userId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to remove member"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPhasesWithTasks(projectId: String): Result<List<Phase>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPhasesWithTasks(projectId)
            if (response.isSuccessful && response.body() != null) {
                val list = response.body()!!.map { it.toDomain() }
                if (list.isNotEmpty()) {
                    cachedPhases = list.toMutableList()
                }
                Result.success(cachedPhases)
            } else {
                Result.success(cachedPhases)
            }
        } catch (e: Exception) {
            Result.success(cachedPhases)
        }
    }

    override suspend fun createPhase(
        projectId: String,
        name: String,
        orderIndex: Int?,
        startDate: String?,
        endDate: String?
    ): Result<Phase> = withContext(Dispatchers.IO) {
        try {
            val request = CreatePhaseRequest(name, orderIndex, startDate, endDate)
            val response = apiService.createPhase(projectId, request)
            if (response.isSuccessful && response.body() != null) {
                val created = response.body()!!.toDomain()
                cachedPhases.add(created)
                Result.success(created)
            } else {
                val local = Phase(
                    id = "phs_${System.currentTimeMillis()}",
                    projectId = projectId,
                    name = name,
                    orderIndex = orderIndex ?: (cachedPhases.size + 1),
                    startDate = startDate,
                    endDate = endDate,
                    status = "Active",
                    progressPct = 0.0,
                    tasks = emptyList()
                )
                cachedPhases.add(local)
                Result.success(local)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTaskProgress(
        taskId: String,
        progressPct: Double,
        status: TaskStatus?
    ): Result<Task> = withContext(Dispatchers.IO) {
        try {
            val request = UpdateTaskProgressRequest(progressPct, status?.name)
            apiService.updateTaskProgress(taskId, request)
        } catch (_: Exception) {}

        for (phaseIndex in cachedPhases.indices) {
            val phase = cachedPhases[phaseIndex]
            val taskIndex = phase.tasks.indexOfFirst { it.id == taskId }
            if (taskIndex != -1) {
                val oldTask = phase.tasks[taskIndex]
                val newStatus = status ?: when {
                    progressPct >= 100.0 -> TaskStatus.COMPLETED
                    progressPct > 0.0 -> TaskStatus.IN_PROGRESS
                    else -> TaskStatus.PENDING
                }
                val updatedTask = oldTask.copy(progressPct = progressPct, status = newStatus)
                val newTasks = phase.tasks.toMutableList()
                newTasks[taskIndex] = updatedTask
                cachedPhases[phaseIndex] = phase.copy(tasks = newTasks)
                return@withContext Result.success(updatedTask)
            }
        }

        Result.failure(Exception("Task not found with id $taskId"))
    }

    override suspend fun submitTaskReview(taskId: String, progressPct: Double?, notes: String?): Result<Task> = withContext(Dispatchers.IO) {
        try {
            val request = TaskSubmitReviewRequest(progressPct, notes)
            val response = apiService.submitTaskReview(taskId, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                updateTaskProgress(taskId, progressPct ?: 100.0, TaskStatus.SUBMITTED_FOR_REVIEW)
            }
        } catch (e: Exception) {
            updateTaskProgress(taskId, progressPct ?: 100.0, TaskStatus.SUBMITTED_FOR_REVIEW)
        }
    }

    override suspend fun reviewTask(taskId: String, action: String, notes: String?): Result<Task> = withContext(Dispatchers.IO) {
        try {
            val request = TaskReviewRequest(action, notes)
            val response = apiService.reviewTask(taskId, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                val status = if ("APPROVE".equals(action, true)) TaskStatus.APPROVED else TaskStatus.CORRECTIONS_REQUIRED
                updateTaskProgress(taskId, 100.0, status)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMilestones(projectId: String): Result<List<Milestone>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMilestones(projectId)
            if (response.isSuccessful && response.body() != null) {
                val list = response.body()!!.map { it.toDomain() }
                if (list.isNotEmpty()) cachedMilestones = list.toMutableList()
                Result.success(cachedMilestones)
            } else {
                Result.success(cachedMilestones)
            }
        } catch (e: Exception) {
            Result.success(cachedMilestones)
        }
    }

    override suspend fun createMilestone(
        projectId: String,
        title: String,
        dueDate: String?,
        targetCompletionPct: Double?
    ): Result<Milestone> = withContext(Dispatchers.IO) {
        try {
            val request = CreateMilestoneRequest(title, dueDate, targetCompletionPct)
            val response = apiService.createMilestone(projectId, request)
            if (response.isSuccessful && response.body() != null) {
                val created = response.body()!!.toDomain()
                cachedMilestones.add(created)
                Result.success(created)
            } else {
                val local = Milestone("mls_${System.currentTimeMillis()}", projectId, title, dueDate, "PENDING", targetCompletionPct ?: 0.0)
                cachedMilestones.add(local)
                Result.success(local)
            }
        } catch (e: Exception) {
            val local = Milestone("mls_${System.currentTimeMillis()}", projectId, title, dueDate, "PENDING", targetCompletionPct ?: 0.0)
            cachedMilestones.add(local)
            Result.success(local)
        }
    }

    override suspend fun updateMilestoneStatus(milestoneId: String, status: String): Result<Milestone> = withContext(Dispatchers.IO) {
        try {
            val request = UpdateMilestoneStatusRequest(status)
            val response = apiService.updateMilestoneStatus(milestoneId, request)
            if (response.isSuccessful && response.body() != null) {
                val updated = response.body()!!.toDomain()
                val idx = cachedMilestones.indexOfFirst { it.id == milestoneId }
                if (idx != -1) cachedMilestones[idx] = updated
                Result.success(updated)
            } else {
                val existing = cachedMilestones.find { it.id == milestoneId }
                if (existing != null) {
                    val updated = existing.copy(status = status)
                    val idx = cachedMilestones.indexOf(existing)
                    cachedMilestones[idx] = updated
                    Result.success(updated)
                } else {
                    Result.failure(Exception("Milestone not found"))
                }
            }
        } catch (e: Exception) {
            val existing = cachedMilestones.find { it.id == milestoneId }
            if (existing != null) Result.success(existing) else Result.failure(e)
        }
    }

    override suspend fun getDashboardSummary(projectId: String?): Result<DashboardSummary> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDashboardSummary(projectId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.success(fallbackDashboardSummary())
            }
        } catch (e: Exception) {
            Result.success(fallbackDashboardSummary())
        }
    }

    override fun getActiveProjectId(): Flow<String?> = sessionDataStore.activeProjectIdFlow

    override suspend fun setActiveProjectId(projectId: String) {
        sessionDataStore.setActiveProjectId(projectId)
    }

    override fun getCachedProjects(): List<Project> = cachedProjects.toList()

    override fun getCachedDashboardSummary(): DashboardSummary = fallbackDashboardSummary()

    private fun fallbackProjectDetail(projectId: String): Result<ProjectDetail> {
        val project = cachedProjects.find { it.id == projectId } ?: cachedProjects.first()
        return Result.success(
            ProjectDetail(
                id = project.id,
                name = project.name,
                code = project.code,
                location = project.location,
                budgetInr = project.budgetInr,
                status = project.status,
                progressPct = project.progressPct,
                organizationId = project.organizationId,
                members = listOf(
                    ProjectMember("pm_1", UserSummary("usr_pm_rahul", "Rahul Verma", "rahul.verma@apexinfra.com"), "PROJECT_MANAGER", "2026-01-10", "ACTIVE"),
                    ProjectMember("pm_2", UserSummary("usr_eng_priya", "Priya Sharma", "priya.sharma@apexinfra.com"), "CIVIL_ENGINEER", "2026-01-12", "ACTIVE"),
                    ProjectMember("pm_3", UserSummary("usr_supervisor_05", "Amit Patel", "supervisor@apexinfra.com"), "SITE_SUPERVISOR", "2026-01-15", "ACTIVE")
                ),
                phasesCount = cachedPhases.size,
                tasksCount = cachedPhases.sumOf { it.tasks.size },
                projectType = project.projectType,
                clientName = project.clientName,
                startDate = project.startDate,
                endDate = project.endDate
            )
        )
    }

    private fun fallbackDashboardSummary(): DashboardSummary {
        val totalTasks = cachedPhases.flatMap { it.tasks }
        return DashboardSummary(
            activeProjectsCount = cachedProjects.size,
            totalBudgetDisplay = "₹2.50 Cr",
            overallProgressPct = 35.0,
            pendingTasksCount = totalTasks.count { it.status == TaskStatus.PENDING },
            inProgressTasksCount = totalTasks.count { it.status == TaskStatus.IN_PROGRESS },
            blockedTasksCount = totalTasks.count { it.status == TaskStatus.BLOCKED },
            completedTasksCount = totalTasks.count { it.status == TaskStatus.COMPLETED },
            roleMetrics = mapOf(
                "Active Projects" to "${cachedProjects.size}",
                "Total Budget" to "₹2.50 Cr",
                "Avg Progress" to "35%",
                "Blocked Tasks" to "${totalTasks.count { it.status == TaskStatus.BLOCKED }}"
            )
        )
    }

    private fun ProjectNetworkDto.toDomain() = Project(
        id = id,
        name = name,
        code = code,
        location = location,
        budgetInr = budgetInr,
        status = status,
        progressPct = progressPct,
        organizationId = organizationId,
        projectType = projectType ?: "Commercial",
        clientName = clientName ?: "Apex Developments",
        startDate = startDate,
        endDate = endDate
    )

    private fun ProjectDetailNetworkDto.toDomain() = ProjectDetail(
        id = id,
        name = name,
        code = code,
        location = location,
        budgetInr = budgetInr,
        status = status,
        progressPct = progressPct,
        organizationId = organizationId,
        members = members.map { it.toDomain() },
        phasesCount = phasesCount,
        tasksCount = tasksCount,
        projectType = projectType,
        clientName = clientName,
        startDate = startDate,
        endDate = endDate
    )

    private fun ProjectMemberNetworkDto.toDomain() = ProjectMember(
        id = id,
        user = UserSummary(user.id, user.fullName, user.email, user.avatarUrl),
        assignedRole = assignedRole,
        joinedAt = joinedAt,
        status = status
    )

    private fun PhaseNetworkDto.toDomain() = Phase(
        id = id,
        projectId = projectId,
        name = name,
        orderIndex = orderIndex,
        startDate = startDate,
        endDate = endDate,
        status = status,
        progressPct = progressPct,
        tasks = tasks.map { it.toDomain() }
    )

    private fun TaskNetworkDto.toDomain() = Task(
        id = id,
        phaseId = phaseId,
        phaseName = phaseName,
        projectId = projectId,
        title = title,
        description = description,
        assignee = assignee?.let { UserSummary(it.id, it.fullName, it.email, it.avatarUrl) },
        priority = TaskPriority.fromString(priority),
        status = TaskStatus.fromString(status),
        startDate = startDate,
        dueDate = dueDate,
        progressPct = progressPct,
        reviewStatus = reviewStatus,
        reviewedBy = reviewedBy?.let { UserSummary(it.id, it.fullName, it.email, it.avatarUrl) },
        reviewNotes = reviewNotes,
        reviewedAt = reviewedAt
    )

    private fun MilestoneNetworkDto.toDomain() = Milestone(
        id = id,
        projectId = projectId,
        title = title,
        dueDate = dueDate,
        status = status,
        targetCompletionPct = targetCompletionPct,
        createdAt = createdAt
    )

    private fun DashboardSummaryNetworkDto.toDomain() = DashboardSummary(
        activeProjectsCount = activeProjectsCount,
        totalBudgetDisplay = "₹%.2f Cr".format(totalBudgetInr.toDouble() / 10000000.0),
        overallProgressPct = overallProgressPct,
        pendingTasksCount = pendingTasksCount,
        inProgressTasksCount = inProgressTasksCount,
        blockedTasksCount = blockedTasksCount,
        completedTasksCount = completedTasksCount,
        roleMetrics = roleMetrics
    )
}
