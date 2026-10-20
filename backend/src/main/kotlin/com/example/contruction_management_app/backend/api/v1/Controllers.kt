package com.example.contruction_management_app.backend.api.v1

import com.example.contruction_management_app.backend.dto.*
import com.example.contruction_management_app.backend.security.UserPrincipal
import com.example.contruction_management_app.backend.service.*
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/health")
class HealthController(
    private val jdbcTemplate: org.springframework.jdbc.core.JdbcTemplate? = null
) {

    @GetMapping
    fun health(): ResponseEntity<HealthResponse> {
        val dbAlive = try {
            jdbcTemplate?.queryForObject("SELECT 1", Integer::class.java) != null
        } catch (e: Exception) {
            false
        }

        return ResponseEntity.ok(
            HealthResponse(
                status = if (dbAlive || jdbcTemplate == null) "UP" else "DEGRADED",
                service = "Construction Management System (CMS) Backend",
                version = "1.0.0"
            )
        )
    }
}

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<TokenResponse> {
        val response = authService.login(request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/me")
    fun getMe(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<UserDto> {
        val user = authService.getCurrentUser(principal)
        return ResponseEntity.ok(user)
    }
}

@RestController
@RequestMapping("/api/v1/projects")
class ProjectController(
    private val projectService: ProjectService
) {

    @GetMapping
    fun listProjects(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<List<ProjectResponseDto>> {
        val projects = projectService.listProjects(principal)
        return ResponseEntity.ok(projects)
    }

    @PostMapping
    @PreAuthorize("hasAuthority('projects:create') or hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    fun createProject(
        @Valid @RequestBody dto: ProjectCreateDto,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ProjectResponseDto> {
        val created = projectService.createProject(dto, principal)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @GetMapping("/{id}")
    fun getProject(
        @PathVariable id: String,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ProjectResponseDto> {
        val project = projectService.getProject(id, principal)
        return ResponseEntity.ok(project)
    }

    @GetMapping("/{id}/detail")
    fun getProjectDetail(
        @PathVariable id: String,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ProjectDetailDto> {
        val detail = projectService.getProjectDetail(id, principal)
        return ResponseEntity.ok(detail)
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasAuthority('projects:edit') or hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    fun assignMember(
        @PathVariable id: String,
        @Valid @RequestBody dto: ProjectMemberAssignDto,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ProjectMemberDto> {
        val member = projectService.assignMember(id, dto, principal)
        return ResponseEntity.status(HttpStatus.CREATED).body(member)
    }
}

@RestController
@RequestMapping("/api/v1")
class TaskController(
    private val taskService: TaskService
) {

    @GetMapping("/projects/{projectId}/phases")
    fun getPhasesWithTasks(
        @PathVariable projectId: String,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<PhaseDto>> {
        val phases = taskService.getPhasesWithTasks(projectId, principal)
        return ResponseEntity.ok(phases)
    }

    @PostMapping("/projects/{projectId}/tasks")
    @PreAuthorize("hasAuthority('tasks:manage') or hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    fun createTask(
        @PathVariable projectId: String,
        @Valid @RequestBody dto: TaskCreateDto,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<TaskDto> {
        val task = taskService.createTask(projectId, dto, principal)
        return ResponseEntity.status(HttpStatus.CREATED).body(task)
    }

    @PatchMapping("/tasks/{taskId}/progress")
    @PreAuthorize("hasAuthority('tasks:update') or hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    fun updateTaskProgress(
        @PathVariable taskId: String,
        @Valid @RequestBody dto: TaskProgressUpdateDto,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<TaskDto> {
        val task = taskService.updateTaskProgress(taskId, dto, principal)
        return ResponseEntity.ok(task)
    }
}

@RestController
@RequestMapping("/api/v1/dashboard")
class DashboardController(
    private val dashboardService: DashboardService
) {

    @GetMapping("/summary")
    fun getSummary(
        @RequestParam(required = false) projectId: String?,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<DashboardSummaryDto> {
        val summary = dashboardService.getSummary(projectId, principal)
        return ResponseEntity.ok(summary)
    }
}
