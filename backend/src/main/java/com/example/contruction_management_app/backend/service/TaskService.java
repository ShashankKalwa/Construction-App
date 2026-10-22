package com.example.contruction_management_app.backend.service;

import com.example.contruction_management_app.backend.domain.Phase;
import com.example.contruction_management_app.backend.domain.Project;
import com.example.contruction_management_app.backend.domain.Task;
import com.example.contruction_management_app.backend.domain.User;
import com.example.contruction_management_app.backend.dto.PhaseDto;
import com.example.contruction_management_app.backend.dto.TaskCreateDto;
import com.example.contruction_management_app.backend.dto.TaskDto;
import com.example.contruction_management_app.backend.dto.TaskProgressUpdateDto;
import com.example.contruction_management_app.backend.dto.UserSummaryDto;
import com.example.contruction_management_app.backend.exception.ForbiddenException;
import com.example.contruction_management_app.backend.exception.ResourceNotFoundException;
import com.example.contruction_management_app.backend.repository.PhaseRepository;
import com.example.contruction_management_app.backend.repository.ProjectMemberRepository;
import com.example.contruction_management_app.backend.repository.ProjectRepository;
import com.example.contruction_management_app.backend.repository.TaskRepository;
import com.example.contruction_management_app.backend.repository.UserRepository;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final ProjectRepository projectRepository;
    private final PhaseRepository phaseRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public TaskService(
        ProjectRepository projectRepository,
        PhaseRepository phaseRepository,
        TaskRepository taskRepository,
        UserRepository userRepository,
        ProjectMemberRepository projectMemberRepository
    ) {
        this.projectRepository = projectRepository;
        this.phaseRepository = phaseRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Transactional(readOnly = true)
    public List<PhaseDto> getPhasesWithTasks(String projectId, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);
        List<Phase> phases = phaseRepository.findByProjectIdOrderByOrderIndexAsc(projectId);
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        Map<String, List<Task>> tasksByPhase = tasks.stream()
            .collect(Collectors.groupingBy(t -> t.getPhase().getId()));

        return phases.stream().map(phase -> {
            List<Task> phaseTasks = tasksByPhase.getOrDefault(phase.getId(), new ArrayList<>());
            List<TaskDto> taskDtos = phaseTasks.stream().map(this::toDto).collect(Collectors.toList());
            return new PhaseDto(
                phase.getId(),
                phase.getProject().getId(),
                phase.getName(),
                phase.getOrderIndex(),
                phase.getStartDate() != null ? phase.getStartDate().toString() : null,
                phase.getEndDate() != null ? phase.getEndDate().toString() : null,
                phase.getStatus(),
                phase.getProgressPct(),
                taskDtos
            );
        }).collect(Collectors.toList());
    }

    @Transactional
    public TaskDto createTask(String projectId, TaskCreateDto dto, UserPrincipal principal) {
        Project project = validateProjectAccess(projectId, principal);
        Phase phase = phaseRepository.findById(dto.phaseId())
            .orElseThrow(() -> new ResourceNotFoundException("Phase not found: " + dto.phaseId()));

        User assignee = null;
        if (dto.assigneeId() != null) {
            assignee = userRepository.findById(dto.assigneeId()).orElse(null);
        }

        LocalDate startDate = dto.startDate() != null ? LocalDate.parse(dto.startDate()) : null;
        LocalDate dueDate = dto.dueDate() != null ? LocalDate.parse(dto.dueDate()) : null;

        Task task = new Task(
            phase,
            project,
            dto.title(),
            dto.description(),
            assignee,
            dto.priority(),
            startDate,
            dueDate
        );
        Task saved = taskRepository.save(task);
        return toDto(saved);
    }

    @Transactional
    public TaskDto updateTaskProgress(String taskId, TaskProgressUpdateDto dto, UserPrincipal principal) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        validateProjectAccess(task.getProject().getId(), principal);

        double progress = Math.max(0.0, Math.min(100.0, dto.progressPct()));
        task.setProgressPct(progress);

        if (dto.status() != null && !dto.status().isBlank()) {
            task.setStatus(dto.status());
        } else if (task.getProgressPct() >= 100.0) {
            task.setStatus("COMPLETED");
        } else if (task.getProgressPct() > 0.0 && "PENDING".equalsIgnoreCase(task.getStatus())) {
            task.setStatus("IN_PROGRESS");
        }

        Task saved = taskRepository.save(task);
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

    private TaskDto toDto(Task t) {
        UserSummaryDto userSummary = null;
        if (t.getAssignee() != null) {
            userSummary = new UserSummaryDto(
                t.getAssignee().getId(),
                t.getAssignee().getFullName(),
                t.getAssignee().getEmail(),
                t.getAssignee().getAvatarUrl()
            );
        }
        return new TaskDto(
            t.getId(),
            t.getPhase().getId(),
            t.getPhase().getName(),
            t.getProject().getId(),
            t.getTitle(),
            t.getDescription(),
            userSummary,
            t.getPriority(),
            t.getStatus(),
            t.getStartDate() != null ? t.getStartDate().toString() : null,
            t.getDueDate() != null ? t.getDueDate().toString() : null,
            t.getProgressPct()
        );
    }
}
