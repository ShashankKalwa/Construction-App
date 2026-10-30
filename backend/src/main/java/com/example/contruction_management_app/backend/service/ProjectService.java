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
import com.example.contruction_management_app.backend.dto.ProjectStatusUpdateDto;
import com.example.contruction_management_app.backend.dto.ProjectUpdateDto;
import com.example.contruction_management_app.backend.dto.UserSummaryDto;
import com.example.contruction_management_app.backend.exception.BadRequestException;
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

import java.math.BigDecimal;
import java.time.LocalDate;
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

        if (dto.projectType() != null && !dto.projectType().isBlank()) {
            project.setProjectType(dto.projectType());
        }
        if (dto.clientName() != null && !dto.clientName().isBlank()) {
            project.setClientName(dto.clientName());
        }
        if (dto.startDate() != null && !dto.startDate().isBlank()) {
            project.setStartDate(LocalDate.parse(dto.startDate()));
        }
        if (dto.endDate() != null && !dto.endDate().isBlank()) {
            project.setEndDate(LocalDate.parse(dto.endDate()));
        }
        project.setStatus("PLANNING");

        Project saved = projectRepository.save(project);

        User user = userRepository.findById(principal.getId()).orElseThrow();
        ProjectMember member = new ProjectMember(saved, user, principal.getRoleId(), organization, "ACTIVE");
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
            (int) totalTasks,
            project.getProjectType(),
            project.getClientName(),
            project.getStartDate() != null ? project.getStartDate().toString() : null,
            project.getEndDate() != null ? project.getEndDate().toString() : null
        );
    }

    @Transactional
    public ProjectResponseDto updateProject(String projectId, ProjectUpdateDto dto, UserPrincipal principal) {
        Project project = findAndValidateProject(projectId, principal);

        if (dto.name() != null && !dto.name().isBlank()) {
            project.setName(dto.name());
        }
        if (dto.location() != null && !dto.location().isBlank()) {
            project.setLocation(dto.location());
        }
        if (dto.budgetInr() != null) {
            if (dto.budgetInr().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Budget must be positive");
            }
            project.setBudgetInr(dto.budgetInr());
        }
        if (dto.projectType() != null) {
            project.setProjectType(dto.projectType());
        }
        if (dto.clientName() != null) {
            project.setClientName(dto.clientName());
        }
        if (dto.startDate() != null && !dto.startDate().isBlank()) {
            project.setStartDate(LocalDate.parse(dto.startDate()));
        }
        if (dto.endDate() != null && !dto.endDate().isBlank()) {
            project.setEndDate(LocalDate.parse(dto.endDate()));
        }

        Project saved = projectRepository.save(project);
        return toDto(saved);
    }

    @Transactional
    public ProjectResponseDto updateProjectStatus(String projectId, ProjectStatusUpdateDto dto, UserPrincipal principal) {
        Project project = findAndValidateProject(projectId, principal);

        String newStatus = dto.status().trim().toUpperCase();
        List<String> validStatuses = List.of("DRAFT", "PLANNING", "ACTIVE", "ON_HOLD", "COMPLETED", "ARCHIVED");
        if (!validStatuses.contains(newStatus)) {
            throw new BadRequestException("Invalid project status: " + dto.status());
        }

        if ("ACTIVE".equalsIgnoreCase(newStatus)) {
            if (project.getBudgetInr() == null || project.getBudgetInr().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Project cannot become ACTIVE without an approved budget");
            }
            long activeMembers = projectMemberRepository.countByProjectIdAndStatus(projectId, "ACTIVE");
            if (activeMembers == 0) {
                throw new BadRequestException("Project cannot become ACTIVE without at least one assigned team member");
            }
        }

        project.setStatus(newStatus);
        Project saved = projectRepository.save(project);
        return toDto(saved);
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
            member.setStatus("ACTIVE");
            member = projectMemberRepository.save(member);
        } else {
            member = new ProjectMember(project, targetUser, dto.assignedRole(), project.getOrganization(), "ACTIVE");
            member = projectMemberRepository.save(member);
        }

        return toMemberDto(member);
    }

    @Transactional
    public void removeMember(String projectId, String userId, UserPrincipal principal) {
        findAndValidateProject(projectId, principal);

        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Member assignment not found for user: " + userId));

        // Soft deactivation preserves historical logs and audit records
        member.setStatus("INACTIVE");
        projectMemberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public List<ProjectMemberDto> listProjectMembers(String projectId, UserPrincipal principal) {
        findAndValidateProject(projectId, principal);
        return projectMemberRepository.findByProjectId(projectId).stream()
            .map(this::toMemberDto)
            .collect(Collectors.toList());
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
            p.getOrganization().getId(),
            p.getProjectType(),
            p.getClientName(),
            p.getStartDate() != null ? p.getStartDate().toString() : null,
            p.getEndDate() != null ? p.getEndDate().toString() : null
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
            pm.getJoinedAt().toString(),
            pm.getStatus()
        );
    }
}
