package com.example.contruction_management_app.backend.service;

import com.example.contruction_management_app.backend.domain.Organization;
import com.example.contruction_management_app.backend.domain.Phase;
import com.example.contruction_management_app.backend.domain.Project;
import com.example.contruction_management_app.backend.domain.ProjectMember;
import com.example.contruction_management_app.backend.domain.User;
import com.example.contruction_management_app.backend.dto.ProjectCreateDto;
import com.example.contruction_management_app.backend.dto.ProjectDetailDto;
import com.example.contruction_management_app.backend.dto.ProjectMemberAssignDto;
import com.example.contruction_management_app.backend.dto.ProjectMemberDto;
import com.example.contruction_management_app.backend.dto.ProjectResponseDto;
import com.example.contruction_management_app.backend.dto.UserSummaryDto;
import com.example.contruction_management_app.backend.exception.ForbiddenException;
import com.example.contruction_management_app.backend.exception.ResourceNotFoundException;
import com.example.contruction_management_app.backend.repository.OrganizationRepository;
import com.example.contruction_management_app.backend.repository.PhaseRepository;
import com.example.contruction_management_app.backend.repository.ProjectMemberRepository;
import com.example.contruction_management_app.backend.repository.ProjectRepository;
import com.example.contruction_management_app.backend.repository.TaskRepository;
import com.example.contruction_management_app.backend.repository.UserRepository;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final PhaseRepository phaseRepository;
    private final TaskRepository taskRepository;

    public ProjectService(
        ProjectRepository projectRepository,
        ProjectMemberRepository projectMemberRepository,
        OrganizationRepository organizationRepository,
        UserRepository userRepository,
        PhaseRepository phaseRepository,
        TaskRepository taskRepository
    ) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.phaseRepository = phaseRepository;
        this.taskRepository = taskRepository;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponseDto> listProjects(UserPrincipal principal) {
        List<Project> projects;
        if ("super_admin".equals(principal.getRoleId()) || "company_admin".equals(principal.getRoleId())) {
            projects = projectRepository.findAllByOrganizationId(principal.getOrganizationId());
        } else {
            projects = projectRepository.findAllByOrganizationAndUser(principal.getOrganizationId(), principal.getId());
        }

        return projects.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public ProjectResponseDto createProject(ProjectCreateDto dto, UserPrincipal principal) {
        Organization organization = organizationRepository.findById(principal.getOrganizationId())
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        Project project = new Project(
            dto.name(),
            dto.code(),
            dto.location(),
            dto.budgetInr(),
            organization
        );
        Project saved = projectRepository.save(project);

        User user = userRepository.findById(principal.getId()).orElseThrow();
        ProjectMember member = new ProjectMember(saved, user, principal.getRoleId());
        projectMemberRepository.save(member);

        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public ProjectResponseDto getProject(String projectId, UserPrincipal principal) {
        Project project = findAndValidateProject(projectId, principal);
        return toDto(project);
    }

    @Transactional(readOnly = true)
    public ProjectDetailDto getProjectDetail(String projectId, UserPrincipal principal) {
        Project project = findAndValidateProject(projectId, principal);
        List<ProjectMemberDto> members = projectMemberRepository.findByProjectId(projectId).stream()
            .map(this::toMemberDto)
            .collect(Collectors.toList());
        List<Phase> phases = phaseRepository.findByProjectIdOrderByOrderIndexAsc(projectId);
        long totalTasks = taskRepository.countByProjectId(projectId);

        return new ProjectDetailDto(
            project.getId(),
            project.getName(),
            project.getCode(),
            project.getLocation(),
            project.getBudgetInr(),
            project.getStatus(),
            project.getProgressPct(),
            project.getOrganization().getId(),
            members,
            phases.size(),
            (int) totalTasks
        );
    }

    @Transactional
    public ProjectMemberDto assignMember(String projectId, ProjectMemberAssignDto dto, UserPrincipal principal) {
        Project project = findAndValidateProject(projectId, principal);
        User targetUser = userRepository.findById(dto.userId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + dto.userId()));

        Optional<ProjectMember> existing = projectMemberRepository.findByProjectIdAndUserId(projectId, dto.userId());
        ProjectMember member;
        if (existing.isPresent()) {
            member = existing.get();
            member.setAssignedRole(dto.assignedRole());
            member = projectMemberRepository.save(member);
        } else {
            member = new ProjectMember(project, targetUser, dto.assignedRole());
            member = projectMemberRepository.save(member);
        }

        return toMemberDto(member);
    }

    private Project findAndValidateProject(String projectId, UserPrincipal principal) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with id " + projectId));

        if (!project.getOrganization().getId().equals(principal.getOrganizationId())) {
            throw new ForbiddenException("Access denied to project from another organization");
        }

        if (!"super_admin".equals(principal.getRoleId()) && !"company_admin".equals(principal.getRoleId())) {
            boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(projectId, principal.getId());
            if (!isMember) {
                throw new ForbiddenException("Access denied. You are not a member of project " + projectId);
            }
        }
        return project;
    }

    private ProjectResponseDto toDto(Project p) {
        return new ProjectResponseDto(
            p.getId(),
            p.getName(),
            p.getCode(),
            p.getLocation(),
            p.getBudgetInr(),
            p.getStatus(),
            p.getProgressPct(),
            p.getOrganization().getId()
        );
    }

    private ProjectMemberDto toMemberDto(ProjectMember pm) {
        UserSummaryDto userSummary = new UserSummaryDto(
            pm.getUser().getId(),
            pm.getUser().getFullName(),
            pm.getUser().getEmail(),
            pm.getUser().getAvatarUrl()
        );
        return new ProjectMemberDto(
            pm.getId(),
            userSummary,
            pm.getAssignedRole(),
            pm.getJoinedAt().toString()
        );
    }
}
