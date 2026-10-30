package com.example.contruction_management_app.backend.security;

import com.example.contruction_management_app.backend.domain.Project;
import com.example.contruction_management_app.backend.domain.ProjectMember;
import com.example.contruction_management_app.backend.domain.Role;
import com.example.contruction_management_app.backend.repository.ProjectMemberRepository;
import com.example.contruction_management_app.backend.repository.ProjectRepository;
import com.example.contruction_management_app.backend.repository.RoleRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("projectAccess")
public class ProjectSecurityService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final RoleRepository roleRepository;

    public ProjectSecurityService(
        ProjectRepository projectRepository,
        ProjectMemberRepository projectMemberRepository,
        RoleRepository roleRepository
    ) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.roleRepository = roleRepository;
    }

    /**
     * Checks if the currently authenticated user can perform an action requiring {@code permission}
     * on the specified project.
     *
     * Verification flow:
     * 1. Check user has valid authenticated principal.
     * 2. Verify project exists AND belongs to the user's company (organization).
     * 3. If user is Company Admin / Super Admin for this company, verify role has permission.
     * 4. Otherwise, verify active project membership on this project AND that their project role has permission.
     */
    public boolean can(String projectId, String permission) {
        if (projectId == null || permission == null) {
            return false;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            return false;
        }

        // 1. Verify project exists
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            return false;
        }
        Project project = projectOpt.get();

        // 2. Check project belongs to user's company (organization)
        if (!project.getOrganization().getId().equals(principal.getOrganizationId())) {
            return false;
        }

        String normalizedPermission = normalizePermission(permission);

        // 3. Super Admin and Company Admin have company-wide authority
        if ("super_admin".equalsIgnoreCase(principal.getRoleId()) || "company_admin".equalsIgnoreCase(principal.getRoleId())) {
            return hasPermission(principal.getRoleId(), normalizedPermission);
        }

        // 4. Verify user has an active ProjectMembership on this specific project
        Optional<ProjectMember> memberOpt = projectMemberRepository.findByProjectIdAndUserId(projectId, principal.getId());
        if (memberOpt.isEmpty()) {
            return false;
        }

        ProjectMember member = memberOpt.get();
        if (member.getStatus() != null && !"ACTIVE".equalsIgnoreCase(member.getStatus())) {
            return false;
        }

        // 5. Check if member's assigned project role (or global fallback role) grants the requested permission
        String roleId = mapAssignedRoleToRoleId(member.getAssignedRole(), principal.getRoleId());
        return hasPermission(roleId, normalizedPermission);
    }

    /**
     * Checks if the user has an organization-level permission.
     */
    public boolean canInCompany(String permission) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            return false;
        }
        String normalized = normalizePermission(permission);
        return hasPermission(principal.getRoleId(), normalized);
    }

    /**
     * Verifies the user has company administrative authority.
     */
    public boolean isCompanyAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            return false;
        }
        return "super_admin".equalsIgnoreCase(principal.getRoleId()) || "company_admin".equalsIgnoreCase(principal.getRoleId());
    }

    /**
     * Ensures an approver cannot be the same user who submitted the request.
     */
    public boolean cannotApproveOwn(String submitterId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            return false;
        }
        return submitterId == null || !principal.getId().equals(submitterId);
    }

    private boolean hasPermission(String roleId, String permission) {
        if (roleId == null) return false;
        Optional<Role> roleOpt = roleRepository.findById(roleId.toLowerCase());
        if (roleOpt.isEmpty()) {
            return false;
        }
        return roleOpt.get().getPermissions().stream()
            .anyMatch(p -> p.getId().equalsIgnoreCase(permission));
    }

    private String normalizePermission(String permission) {
        String p = permission.trim().toLowerCase();
        return p.replace('_', ':');
    }

    private String mapAssignedRoleToRoleId(String assignedRole, String fallbackRoleId) {
        if (assignedRole == null || assignedRole.isBlank()) {
            return fallbackRoleId;
        }
        return switch (assignedRole.trim().toLowerCase()) {
            case "project manager", "project_manager" -> "project_manager";
            case "civil engineer", "civil_engineer" -> "civil_engineer";
            case "site supervisor", "site_supervisor" -> "site_supervisor";
            case "storekeeper" -> "storekeeper";
            case "accountant" -> "accountant";
            case "contractor" -> "contractor";
            case "client", "client owner", "client_owner" -> "client_owner";
            default -> assignedRole.toLowerCase().replace(' ', '_');
        };
    }
}
