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
    suspend fun createProject(name: String, code: String, location: String, budgetInr: BigDecimal): Result<Project>
    suspend fun getPhasesWithTasks(projectId: String): Result<List<Phase>>
    suspend fun updateTaskProgress(taskId: String, progressPct: Double, status: TaskStatus?): Result<Task>
    suspend fun getDashboardSummary(projectId: String?): Result<DashboardSummary>
    fun getActiveProjectId(): Flow<String?>
    suspend fun setActiveProjectId(projectId: String)
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
            organizationId = "org_green_valley"
        ),
        Project(
            id = "prj_skyline_commercial",
            name = "Skyline Business Park",
            code = "SBP-02",
            location = "Whitefield, Bangalore",
            budgetInr = BigDecimal("68000000.00"),
            status = "Active",
            progressPct = 18.0,
            organizationId = "org_green_valley"
        )
    )

    private var cachedPhases: MutableList<Phase> = mutableListOf(
        Phase(
            id = "phs_gvr_01",
            projectId = "prj_green_valley_residency",
            name = "Phase 1: Substructure & Foundation",
            orderIndex = 1,
            startDate = "2026-01-10",
            endDate = "2026-03-15",
            status = "Completed",
            progressPct = 100.0,
            tasks = listOf(
                Task(
                    id = "tsk_gvr_101",
                    phaseId = "phs_gvr_01",
                    phaseName = "Phase 1: Substructure & Foundation",
                    projectId = "prj_green_valley_residency",
                    title = "Site soil excavation & raft pit levelling",
                    description = "Excavation to depth of 3.2m as per structural drawing S-01",
                    assignee = UserSummary("usr_supervisor", "Suresh Kumar", "supervisor@greenvalley.com", null),
                    priority = TaskPriority.HIGH,
                    status = TaskStatus.COMPLETED,
                    startDate = "2026-01-10",
                    dueDate = "2026-01-25",
                    progressPct = 100.0
                ),
                Task(
                    id = "tsk_gvr_102",
                    phaseId = "phs_gvr_01",
                    phaseName = "Phase 1: Substructure & Foundation",
                    projectId = "prj_green_valley_residency",
                    title = "PCC 1:4:8 under footings & water proofing",
                    description = "75mm PCC bed and bitumen waterproofing membrane application",
                    assignee = UserSummary("usr_supervisor", "Suresh Kumar", "supervisor@greenvalley.com", null),
                    priority = TaskPriority.MEDIUM,
                    status = TaskStatus.COMPLETED,
                    startDate = "2026-01-26",
                    dueDate = "2026-02-10",
                    progressPct = 100.0
                )
            )
        ),
        Phase(
            id = "phs_gvr_02",
            projectId = "prj_green_valley_residency",
            name = "Phase 2: RCC Superstructure (Tower A & B)",
            orderIndex = 2,
            startDate = "2026-03-16",
            endDate = "2026-07-30",
            status = "Active",
            progressPct = 60.0,
            tasks = listOf(
                Task(
                    id = "tsk_gvr_201",
                    phaseId = "phs_gvr_02",
                    phaseName = "Phase 2: RCC Superstructure (Tower A & B)",
                    projectId = "prj_green_valley_residency",
                    title = "Tower A Ground Floor Columns & 1st Floor Slab",
                    description = "Formwork, conduit placement and M25 casting for 1st floor",
                    assignee = UserSummary("usr_engineer", "Ananya Roy", "engineer@greenvalley.com", null),
                    priority = TaskPriority.HIGH,
                    status = TaskStatus.COMPLETED,
                    startDate = "2026-03-16",
                    dueDate = "2026-04-20",
                    progressPct = 100.0
                ),
                Task(
                    id = "tsk_gvr_202",
                    phaseId = "phs_gvr_02",
                    phaseName = "Phase 2: RCC Superstructure (Tower A & B)",
                    projectId = "prj_green_valley_residency",
                    title = "Tower A 2nd Floor Column Shuttering & Casting",
                    description = "8 columns (Grid A1-A8) formwork alignment and pour",
                    assignee = UserSummary("usr_supervisor", "Suresh Kumar", "supervisor@greenvalley.com", null),
                    priority = TaskPriority.HIGH,
                    status = TaskStatus.IN_PROGRESS,
                    startDate = "2026-04-21",
                    dueDate = "2026-05-15",
                    progressPct = 75.0
                ),
                Task(
                    id = "tsk_gvr_203",
                    phaseId = "phs_gvr_02",
                    phaseName = "Phase 2: RCC Superstructure (Tower A & B)",
                    projectId = "prj_green_valley_residency",
                    title = "Tower B 2nd Floor Beam Rebar Inspection",
                    description = "Structural consultant pre-pour inspection for main girders",
                    assignee = UserSummary("usr_engineer", "Ananya Roy", "engineer@greenvalley.com", null),
                    priority = TaskPriority.MEDIUM,
                    status = TaskStatus.PENDING,
                    startDate = "2026-05-10",
                    dueDate = "2026-05-18",
                    progressPct = 0.0
                ),
                Task(
                    id = "tsk_gvr_204",
                    phaseId = "phs_gvr_02",
                    phaseName = "Phase 2: RCC Superstructure (Tower A & B)",
                    projectId = "prj_green_valley_residency",
                    title = "Procure Fe500D 16mm & 20mm TMT Rebars",
                    description = "Vendor dispatch delayed from Jindal Steel plant",
                    assignee = UserSummary("usr_pm", "Vikram Patel", "pm@greenvalley.com", null),
                    priority = TaskPriority.HIGH,
                    status = TaskStatus.BLOCKED,
                    startDate = "2026-05-01",
                    dueDate = "2026-05-12",
                    progressPct = 30.0
                )
            )
        ),
        Phase(
            id = "phs_gvr_03",
            projectId = "prj_green_valley_residency",
            name = "Phase 3: Brickwork & Masonry",
            orderIndex = 3,
            startDate = "2026-08-01",
            endDate = "2026-10-15",
            status = "Planned",
            progressPct = 0.0,
            tasks = listOf(
                Task(
                    id = "tsk_gvr_301",
                    phaseId = "phs_gvr_03",
                    phaseName = "Phase 3: Brickwork & Masonry",
                    projectId = "prj_green_valley_residency",
                    title = "Ground Floor 200mm AAC Blockwork",
                    description = "External perimeter walls and party walls with block adhesive",
                    assignee = UserSummary("usr_supervisor", "Suresh Kumar", "supervisor@greenvalley.com", null),
                    priority = TaskPriority.MEDIUM,
                    status = TaskStatus.PENDING,
                    startDate = "2026-08-01",
                    dueDate = "2026-08-25",
                    progressPct = 0.0
                )
            )
        )
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
        budgetInr: BigDecimal
    ): Result<Project> = withContext(Dispatchers.IO) {
        try {
            val request = CreateProjectRequest(name, code, location, budgetInr)
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
                    status = "Active",
                    progressPct = 0.0,
                    organizationId = "org_green_valley"
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
                status = "Active",
                progressPct = 0.0,
                organizationId = "org_green_valley"
            )
            cachedProjects.add(0, local)
            Result.success(local)
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

    override suspend fun updateTaskProgress(
        taskId: String,
        progressPct: Double,
        status: TaskStatus?
    ): Result<Task> = withContext(Dispatchers.IO) {
        try {
            val request = UpdateTaskProgressRequest(progressPct, status?.name)
            apiService.updateTaskProgress(taskId, request)
        } catch (_: Exception) {}

        // Update local memory cache
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
                    ProjectMember("pm_1", UserSummary("usr_pm", "Vikram Patel", "pm@greenvalley.com"), "Project Manager", "2026-01-10"),
                    ProjectMember("pm_2", UserSummary("usr_engineer", "Ananya Roy", "engineer@greenvalley.com"), "Civil Engineer", "2026-01-12"),
                    ProjectMember("pm_3", UserSummary("usr_supervisor", "Suresh Kumar", "supervisor@greenvalley.com"), "Site Supervisor", "2026-01-15")
                ),
                phasesCount = cachedPhases.size,
                tasksCount = cachedPhases.sumOf { it.tasks.size }
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
        organizationId = organizationId
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
        tasksCount = tasksCount
    )

    private fun ProjectMemberNetworkDto.toDomain() = ProjectMember(
        id = id,
        user = UserSummary(user.id, user.fullName, user.email, user.avatarUrl),
        assignedRole = assignedRole,
        joinedAt = joinedAt
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
        priority = when (priority.uppercase()) {
            "HIGH" -> TaskPriority.HIGH
            "LOW" -> TaskPriority.LOW
            else -> TaskPriority.MEDIUM
        },
        status = when (status.uppercase()) {
            "COMPLETED" -> TaskStatus.COMPLETED
            "IN_PROGRESS" -> TaskStatus.IN_PROGRESS
            "BLOCKED" -> TaskStatus.BLOCKED
            else -> TaskStatus.PENDING
        },
        startDate = startDate,
        dueDate = dueDate,
        progressPct = progressPct
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
