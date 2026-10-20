package com.example.contruction_management_app.backend.service

import com.example.contruction_management_app.backend.domain.*
import com.example.contruction_management_app.backend.dto.*
import com.example.contruction_management_app.backend.exception.ForbiddenException
import com.example.contruction_management_app.backend.exception.ResourceNotFoundException
import com.example.contruction_management_app.backend.exception.UnauthorizedException
import com.example.contruction_management_app.backend.repository.*
import com.example.contruction_management_app.backend.security.JwtTokenProvider
import com.example.contruction_management_app.backend.security.UserPrincipal
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenProvider: JwtTokenProvider
) {
    @Transactional(readOnly = true)
    fun login(request: LoginRequest): TokenResponse {
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { UnauthorizedException("Incorrect email or password") }

        if (!passwordEncoder.matches(request.password, user.hashedPassword)) {
            throw UnauthorizedException("Incorrect email or password")
        }

        if (!user.isActive) {
            throw UnauthorizedException("User account is deactivated")
        }

        val token = tokenProvider.generateToken(user.id)

        val roleDto = RoleDto(
            id = user.role.id,
            name = user.role.name,
            description = user.role.description,
            permissions = user.role.permissions.map { it.id }
        )

        val userDto = UserDto(
            id = user.id,
            email = user.email,
            fullName = user.fullName,
            avatarUrl = user.avatarUrl,
            organizationId = user.organization.id,
            organizationName = user.organization.name,
            role = roleDto
        )

        return TokenResponse(
            accessToken = token,
            tokenType = "bearer",
            user = userDto
        )
    }

    @Transactional(readOnly = true)
    fun getCurrentUser(principal: UserPrincipal): UserDto {
        val user = userRepository.findById(principal.id)
            .orElseThrow { ResourceNotFoundException("User not found") }

        return UserDto(
            id = user.id,
            email = user.email,
            fullName = user.fullName,
            avatarUrl = user.avatarUrl,
            organizationId = user.organization.id,
            organizationName = user.organization.name,
            role = RoleDto(
                id = user.role.id,
                name = user.role.name,
                description = user.role.description,
                permissions = user.role.permissions.map { it.id }
            )
        )
    }
}

@Service
class ProjectService(
    private val projectRepository: ProjectRepository,
    private val projectMemberRepository: ProjectMemberRepository,
    private val organizationRepository: OrganizationRepository,
    private val userRepository: UserRepository,
    private val phaseRepository: PhaseRepository,
    private val taskRepository: TaskRepository
) {
    @Transactional(readOnly = true)
    fun listProjects(principal: UserPrincipal): List<ProjectResponseDto> {
        val projects = if (principal.roleId in listOf("super_admin", "company_admin")) {
            projectRepository.findAllByOrganizationId(principal.organizationId)
        } else {
            projectRepository.findAllByOrganizationAndUser(principal.organizationId, principal.id)
        }

        return projects.map { it.toDto() }
    }

    @Transactional
    fun createProject(dto: ProjectCreateDto, principal: UserPrincipal): ProjectResponseDto {
        val organization = organizationRepository.findById(principal.organizationId)
            .orElseThrow { ResourceNotFoundException("Organization not found") }

        val project = Project(
            name = dto.name,
            code = dto.code,
            location = dto.location,
            budgetInr = dto.budgetInr,
            organization = organization
        )
        val saved = projectRepository.save(project)

        val user = userRepository.findById(principal.id).orElseThrow()
        val member = ProjectMember(
            project = saved,
            user = user,
            assignedRole = principal.roleId
        )
        projectMemberRepository.save(member)

        return saved.toDto()
    }

    @Transactional(readOnly = true)
    fun getProject(projectId: String, principal: UserPrincipal): ProjectResponseDto {
        val project = findAndValidateProject(projectId, principal)
        return project.toDto()
    }

    @Transactional(readOnly = true)
    fun getProjectDetail(projectId: String, principal: UserPrincipal): ProjectDetailDto {
        val project = findAndValidateProject(projectId, principal)
        val members = projectMemberRepository.findByProjectId(projectId).map { it.toDto() }
        val phases = phaseRepository.findByProjectIdOrderByOrderIndexAsc(projectId)
        val totalTasks = taskRepository.countByProjectId(projectId)

        return ProjectDetailDto(
            id = project.id,
            name = project.name,
            code = project.code,
            location = project.location,
            budgetInr = project.budgetInr,
            status = project.status,
            progressPct = project.progressPct,
            organizationId = project.organization.id,
            members = members,
            phasesCount = phases.size,
            tasksCount = totalTasks.toInt()
        )
    }

    @Transactional
    fun assignMember(projectId: String, dto: ProjectMemberAssignDto, principal: UserPrincipal): ProjectMemberDto {
        val project = findAndValidateProject(projectId, principal)
        val targetUser = userRepository.findById(dto.userId)
            .orElseThrow { ResourceNotFoundException("User not found: ${dto.userId}") }

        val existing = projectMemberRepository.findByProjectIdAndUserId(projectId, dto.userId)
        val member = if (existing.isPresent) {
            val m = existing.get()
            m.assignedRole = dto.assignedRole
            projectMemberRepository.save(m)
        } else {
            projectMemberRepository.save(
                ProjectMember(
                    project = project,
                    user = targetUser,
                    assignedRole = dto.assignedRole
                )
            )
        }
        return member.toDto()
    }

    private fun findAndValidateProject(projectId: String, principal: UserPrincipal): Project {
        val project = projectRepository.findById(projectId)
            .orElseThrow { ResourceNotFoundException("Project not found with id $projectId") }

        if (project.organization.id != principal.organizationId) {
            throw ForbiddenException("Access denied to project from another organization")
        }

        if (principal.roleId !in listOf("super_admin", "company_admin")) {
            val isMember = projectMemberRepository.existsByProjectIdAndUserId(projectId, principal.id)
            if (!isMember) {
                throw ForbiddenException("Access denied. You are not a member of project $projectId")
            }
        }
        return project
    }

    private fun Project.toDto() = ProjectResponseDto(
        id = this.id,
        name = this.name,
        code = this.code,
        location = this.location,
        budgetInr = this.budgetInr,
        status = this.status,
        progressPct = this.progressPct,
        organizationId = this.organization.id
    )

    private fun ProjectMember.toDto() = ProjectMemberDto(
        id = this.id,
        user = UserSummaryDto(
            id = this.user.id,
            fullName = this.user.fullName,
            email = this.user.email,
            avatarUrl = this.user.avatarUrl
        ),
        assignedRole = this.assignedRole,
        joinedAt = this.joinedAt.toString()
    )
}

@Service
class TaskService(
    private val projectRepository: ProjectRepository,
    private val phaseRepository: PhaseRepository,
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val projectMemberRepository: ProjectMemberRepository
) {
    @Transactional(readOnly = true)
    fun getPhasesWithTasks(projectId: String, principal: UserPrincipal): List<PhaseDto> {
        validateProjectAccess(projectId, principal)
        val phases = phaseRepository.findByProjectIdOrderByOrderIndexAsc(projectId)
        val tasks = taskRepository.findByProjectId(projectId)
        val tasksByPhase = tasks.groupBy { it.phase.id }

        return phases.map { phase ->
            val phaseTasks = tasksByPhase[phase.id] ?: emptyList()
            PhaseDto(
                id = phase.id,
                projectId = phase.project.id,
                name = phase.name,
                orderIndex = phase.orderIndex,
                startDate = phase.startDate?.toString(),
                endDate = phase.endDate?.toString(),
                status = phase.status,
                progressPct = phase.progressPct,
                tasks = phaseTasks.map { it.toDto() }
            )
        }
    }

    @Transactional
    fun createTask(projectId: String, dto: TaskCreateDto, principal: UserPrincipal): TaskDto {
        val project = validateProjectAccess(projectId, principal)
        val phase = phaseRepository.findById(dto.phaseId)
            .orElseThrow { ResourceNotFoundException("Phase not found: ${dto.phaseId}") }

        val assignee = dto.assigneeId?.let {
            userRepository.findById(it).orElse(null)
        }

        val task = Task(
            phase = phase,
            project = project,
            title = dto.title,
            description = dto.description,
            assignee = assignee,
            priority = dto.priority,
            startDate = dto.startDate?.let { java.time.LocalDate.parse(it) },
            dueDate = dto.dueDate?.let { java.time.LocalDate.parse(it) }
        )
        val saved = taskRepository.save(task)
        return saved.toDto()
    }

    @Transactional
    fun updateTaskProgress(taskId: String, dto: TaskProgressUpdateDto, principal: UserPrincipal): TaskDto {
        val task = taskRepository.findById(taskId)
            .orElseThrow { ResourceNotFoundException("Task not found: $taskId") }

        validateProjectAccess(task.project.id, principal)

        task.progressPct = dto.progressPct.coerceIn(0.0, 100.0)
        if (dto.status != null) {
            task.status = dto.status
        } else if (task.progressPct >= 100.0) {
            task.status = "COMPLETED"
        } else if (task.progressPct > 0.0 && task.status == "PENDING") {
            task.status = "IN_PROGRESS"
        }

        val saved = taskRepository.save(task)
        return saved.toDto()
    }

    private fun validateProjectAccess(projectId: String, principal: UserPrincipal): Project {
        val project = projectRepository.findById(projectId)
            .orElseThrow { ResourceNotFoundException("Project not found: $projectId") }

        if (project.organization.id != principal.organizationId) {
            throw ForbiddenException("Access denied to project from another organization")
        }

        if (principal.roleId !in listOf("super_admin", "company_admin")) {
            val isMember = projectMemberRepository.existsByProjectIdAndUserId(projectId, principal.id)
            if (!isMember) {
                throw ForbiddenException("Access denied. You are not a member of project $projectId")
            }
        }
        return project
    }

    private fun Task.toDto() = TaskDto(
        id = this.id,
        phaseId = this.phase.id,
        phaseName = this.phase.name,
        projectId = this.project.id,
        title = this.title,
        description = this.description,
        assignee = this.assignee?.let {
            UserSummaryDto(it.id, it.fullName, it.email, it.avatarUrl)
        },
        priority = this.priority,
        status = this.status,
        startDate = this.startDate?.toString(),
        dueDate = this.dueDate?.toString(),
        progressPct = this.progressPct
    )
}

@Service
class DashboardService(
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository
) {
    @Transactional(readOnly = true)
    fun getSummary(projectId: String?, principal: UserPrincipal): DashboardSummaryDto {
        val projects = if (principal.roleId in listOf("super_admin", "company_admin")) {
            projectRepository.findAllByOrganizationId(principal.organizationId)
        } else {
            projectRepository.findAllByOrganizationAndUser(principal.organizationId, principal.id)
        }

        val totalBudget = projects.fold(java.math.BigDecimal.ZERO) { acc, p -> acc.add(p.budgetInr) }
        val avgProgress = if (projects.isNotEmpty()) projects.map { it.progressPct }.average() else 0.0

        val targetProjectId = projectId ?: projects.firstOrNull()?.id
        val (pending, inProgress, blocked, completed) = if (targetProjectId != null) {
            listOf(
                taskRepository.countByProjectIdAndStatus(targetProjectId, "PENDING").toInt(),
                taskRepository.countByProjectIdAndStatus(targetProjectId, "IN_PROGRESS").toInt(),
                taskRepository.countByProjectIdAndStatus(targetProjectId, "BLOCKED").toInt(),
                taskRepository.countByProjectIdAndStatus(targetProjectId, "COMPLETED").toInt()
            )
        } else {
            listOf(0, 0, 0, 0)
        }

        val roleMetrics = mutableMapOf<String, String>()
        when (principal.roleId) {
            "project_manager", "company_admin", "super_admin" -> {
                roleMetrics["Active Projects"] = projects.size.toString()
                roleMetrics["Total Budget"] = "₹${(totalBudget.toDouble() / 10000000.0).let { "%.2f Cr".format(it) }}"
                roleMetrics["Avg Progress"] = "${avgProgress.toInt()}%"
                roleMetrics["Blocked Tasks"] = blocked.toString()
            }
            "site_supervisor" -> {
                roleMetrics["Today's Tasks"] = (pending + inProgress).toString()
                roleMetrics["In Progress"] = inProgress.toString()
                roleMetrics["Blocked Issues"] = blocked.toString()
                roleMetrics["Labor Present"] = "28 on-site"
            }
            "civil_engineer" -> {
                roleMetrics["Active Phases"] = "2 Ongoing"
                roleMetrics["Inspections Due"] = inProgress.toString()
                roleMetrics["Calculations Log"] = "14 Run"
                roleMetrics["BOQ Status"] = "Verified"
            }
            "accountant" -> {
                roleMetrics["Committed Budget"] = "₹${(totalBudget.toDouble() / 10000000.0).let { "%.2f Cr".format(it) }}"
                roleMetrics["Spend Actual"] = "₹87.5 L (35%)"
                roleMetrics["Pending Invoices"] = "3 Bills"
                roleMetrics["Variance"] = "+1.8%"
            }
            else -> {
                roleMetrics["Assigned Work"] = (pending + inProgress).toString()
                roleMetrics["Completed"] = completed.toString()
                roleMetrics["Project Status"] = if (projects.isNotEmpty()) "Active" else "None"
                roleMetrics["Due This Week"] = inProgress.toString()
            }
        }

        return DashboardSummaryDto(
            activeProjectsCount = projects.size,
            totalBudgetInr = totalBudget,
            overallProgressPct = avgProgress,
            pendingTasksCount = pending,
            inProgressTasksCount = inProgress,
            blockedTasksCount = blocked,
            completedTasksCount = completed,
            roleMetrics = roleMetrics
        )
    }
}
