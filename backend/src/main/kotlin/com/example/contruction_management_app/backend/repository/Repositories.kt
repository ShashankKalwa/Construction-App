package com.example.contruction_management_app.backend.repository

import com.example.contruction_management_app.backend.domain.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<User, String> {
    fun findByEmail(email: String): Optional<User>
    fun existsByEmail(email: String): Boolean
}

@Repository
interface RoleRepository : JpaRepository<Role, String>

@Repository
interface PermissionRepository : JpaRepository<Permission, String>

@Repository
interface OrganizationRepository : JpaRepository<Organization, String> {
    fun findBySlug(slug: String): Optional<Organization>
}

@Repository
interface ProjectRepository : JpaRepository<Project, String> {
    fun findAllByOrganizationId(organizationId: String): List<Project>

    @Query("""
        SELECT p FROM Project p 
        JOIN ProjectMember pm ON p.id = pm.project.id 
        WHERE p.organization.id = :orgId AND pm.user.id = :userId
    """)
    fun findAllByOrganizationAndUser(
        @Param("orgId") orgId: String,
        @Param("userId") userId: String
    ): List<Project>
}

@Repository
interface ProjectMemberRepository : JpaRepository<ProjectMember, String> {
    fun existsByProjectIdAndUserId(projectId: String, userId: String): Boolean
    fun findByProjectIdAndUserId(projectId: String, userId: String): Optional<ProjectMember>
    fun findByProjectId(projectId: String): List<ProjectMember>
}

@Repository
interface PhaseRepository : JpaRepository<Phase, String> {
    fun findByProjectIdOrderByOrderIndexAsc(projectId: String): List<Phase>
}

@Repository
interface TaskRepository : JpaRepository<Task, String> {
    fun findByProjectId(projectId: String): List<Task>
    fun findByPhaseId(phaseId: String): List<Task>
    fun findByAssigneeId(assigneeId: String): List<Task>
    fun countByProjectId(projectId: String): Long
    fun countByProjectIdAndStatus(projectId: String, status: String): Long
}
