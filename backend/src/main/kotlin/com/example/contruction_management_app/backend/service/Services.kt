package com.example.contruction_management_app.backend.service

import com.example.contruction_management_app.backend.domain.Project
import com.example.contruction_management_app.backend.domain.ProjectMember
import com.example.contruction_management_app.backend.domain.User
import com.example.contruction_management_app.backend.dto.*
import com.example.contruction_management_app.backend.exception.ForbiddenException
import com.example.contruction_management_app.backend.exception.ResourceNotFoundException
import com.example.contruction_management_app.backend.exception.UnauthorizedException
import com.example.contruction_management_app.backend.repository.OrganizationRepository
import com.example.contruction_management_app.backend.repository.ProjectMemberRepository
import com.example.contruction_management_app.backend.repository.ProjectRepository
import com.example.contruction_management_app.backend.repository.UserRepository
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
    private val userRepository: UserRepository
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
        val project = projectRepository.findById(projectId)
            .orElseThrow { ResourceNotFoundException("Project not found with id $projectId") }

        // Multi-tenant check
        if (project.organization.id != principal.organizationId) {
            throw ForbiddenException("Access denied to project from another organization")
        }

        // Project membership check for non-admins
        if (principal.roleId !in listOf("super_admin", "company_admin")) {
            val isMember = projectMemberRepository.existsByProjectIdAndUserId(projectId, principal.id)
            if (!isMember) {
                throw ForbiddenException("Access denied. You are not a member of project $projectId")
            }
        }

        return project.toDto()
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
}
