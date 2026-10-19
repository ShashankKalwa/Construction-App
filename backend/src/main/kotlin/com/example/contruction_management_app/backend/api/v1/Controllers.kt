package com.example.contruction_management_app.backend.api.v1

import com.example.contruction_management_app.backend.dto.*
import com.example.contruction_management_app.backend.security.UserPrincipal
import com.example.contruction_management_app.backend.service.AuthService
import com.example.contruction_management_app.backend.service.ProjectService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/health")
class HealthController {

    @GetMapping
    fun health(): ResponseEntity<HealthResponse> {
        return ResponseEntity.ok(
            HealthResponse(
                status = "UP",
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
}
