package com.example.contruction_management_app.backend.service;

import com.example.contruction_management_app.backend.domain.Milestone;
import com.example.contruction_management_app.backend.domain.Project;
import com.example.contruction_management_app.backend.dto.MilestoneCreateDto;
import com.example.contruction_management_app.backend.dto.MilestoneDto;
import com.example.contruction_management_app.backend.dto.MilestoneStatusUpdateDto;
import com.example.contruction_management_app.backend.exception.BadRequestException;
import com.example.contruction_management_app.backend.exception.ForbiddenException;
import com.example.contruction_management_app.backend.exception.ResourceNotFoundException;
import com.example.contruction_management_app.backend.repository.MilestoneRepository;
import com.example.contruction_management_app.backend.repository.ProjectMemberRepository;
import com.example.contruction_management_app.backend.repository.ProjectRepository;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public MilestoneService(
        MilestoneRepository milestoneRepository,
        ProjectRepository projectRepository,
        ProjectMemberRepository projectMemberRepository
    ) {
        this.milestoneRepository = milestoneRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Transactional(readOnly = true)
    public List<MilestoneDto> getMilestones(String projectId, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);
        return milestoneRepository.findByProjectIdOrderByDueDateAsc(projectId).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public MilestoneDto createMilestone(String projectId, MilestoneCreateDto dto, UserPrincipal principal) {
        Project project = validateProjectAccess(projectId, principal);

        LocalDate dueDate = dto.dueDate() != null && !dto.dueDate().isBlank() ? LocalDate.parse(dto.dueDate()) : null;
        Milestone milestone = new Milestone(
            project,
            project.getOrganization(),
            dto.title(),
            dueDate,
            dto.targetCompletionPct()
        );

        Milestone saved = milestoneRepository.save(milestone);
        return toDto(saved);
    }

    @Transactional
    public MilestoneDto updateMilestoneStatus(String milestoneId, MilestoneStatusUpdateDto dto, UserPrincipal principal) {
        Milestone milestone = milestoneRepository.findById(milestoneId)
            .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with id " + milestoneId));

        validateProjectAccess(milestone.getProject().getId(), principal);

        String newStatus = dto.status().trim().toUpperCase();
        List<String> valid = List.of("PENDING", "REACHED", "DELAYED");
        if (!valid.contains(newStatus)) {
            throw new BadRequestException("Invalid milestone status: " + dto.status());
        }

        milestone.setStatus(newStatus);
        Milestone saved = milestoneRepository.save(milestone);
        return toDto(saved);
    }

    private Project validateProjectAccess(String projectId, UserPrincipal principal) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

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

    private MilestoneDto toDto(Milestone m) {
        return new MilestoneDto(
            m.getId(),
            m.getProject().getId(),
            m.getTitle(),
            m.getDueDate() != null ? m.getDueDate().toString() : null,
            m.getStatus(),
            m.getTargetCompletionPct(),
            m.getCreatedAt() != null ? m.getCreatedAt().toString() : null
        );
    }
}
