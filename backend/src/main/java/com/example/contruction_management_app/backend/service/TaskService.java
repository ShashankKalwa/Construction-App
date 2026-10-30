package com.example.contruction_management_app.backend.service;

import com.example.contruction_management_app.backend.domain.Phase;
import com.example.contruction_management_app.backend.domain.Project;
import com.example.contruction_management_app.backend.domain.Task;
import com.example.contruction_management_app.backend.domain.User;
import com.example.contruction_management_app.backend.dto.PhaseCreateDto;
import com.example.contruction_management_app.backend.dto.PhaseDto;
import com.example.contruction_management_app.backend.dto.TaskCreateDto;
import com.example.contruction_management_app.backend.dto.TaskDto;
import com.example.contruction_management_app.backend.dto.TaskProgressUpdateDto;
import com.example.contruction_management_app.backend.dto.TaskReviewDto;
import com.example.contruction_management_app.backend.dto.TaskSubmitReviewDto;
import com.example.contruction_management_app.backend.dto.UserSummaryDto;
import com.example.contruction_management_app.backend.exception.BadRequestException;
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

import java.time.Instant;
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
    public PhaseDto createPhase(String projectId, PhaseCreateDto dto, UserPrincipal principal) {
        Project project = validateProjectAccess(projectId, principal);
        int orderIndex = dto.orderIndex() != null ? dto.orderIndex() : (int) phaseRepository.findByProjectIdOrderByOrderIndexAsc(projectId).size() + 1;
        LocalDate start = dto.startDate() != null && !dto.startDate().isBlank() ? LocalDate.parse(dto.startDate()) : null;
        LocalDate end = dto.endDate() != null && !dto.endDate().isBlank() ? LocalDate.parse(dto.endDate()) : null;

        Phase phase = new Phase(project, dto.name(), orderIndex, start, end);
        phase.setCompany(project.getOrganization());
        Phase saved = phaseRepository.save(phase);

        return new PhaseDto(
            saved.getId(),
            saved.getProject().getId(),
            saved.getName(),
            saved.getOrderIndex(),
            saved.getStartDate() != null ? saved.getStartDate().toString() : null,
            saved.getEndDate() != null ? saved.getEndDate().toString() : null,
            saved.getStatus(),
            saved.getProgressPct(),
            List.of()
        );
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
        task.setCompany(project.getOrganization());
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

    @Transactional
    public TaskDto submitForReview(String taskId, TaskSubmitReviewDto dto, UserPrincipal principal) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        validateProjectAccess(task.getProject().getId(), principal);

        if (dto.progressPct() != null) {
            double progress = Math.max(0.0, Math.min(100.0, dto.progressPct()));
            task.setProgressPct(progress);
        }
        task.setStatus("SUBMITTED_FOR_REVIEW");
        task.setReviewStatus("PENDING_REVIEW");
        if (dto.notes() != null) {
            task.setReviewNotes(dto.notes());
        }

        Task saved = taskRepository.save(task);
        return toDto(saved);
    }

    @Transactional
    public TaskDto reviewTask(String taskId, TaskReviewDto dto, UserPrincipal principal) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        validateProjectAccess(task.getProject().getId(), principal);

        // Self-review restriction: Assignee cannot review their own task
        if (task.getAssignee() != null && task.getAssignee().getId().equals(principal.getId())) {
            throw new ForbiddenException("Self-review is prohibited: you cannot review or approve your own assigned task");
        }

        User reviewer = userRepository.findById(principal.getId()).orElse(null);
        String action = dto.action().trim().toUpperCase();

        if ("APPROVE".equals(action)) {
            task.setStatus(task.getProgressPct() >= 100.0 ? "COMPLETED" : "APPROVED");
            task.setReviewStatus("APPROVED");
            task.setReviewedBy(reviewer);
            task.setReviewedAt(Instant.now());
            if (dto.notes() != null && !dto.notes().isBlank()) {
                task.setReviewNotes(dto.notes());
            }
        } else if ("REQUEST_CORRECTIONS".equals(action) || "REJECT".equals(action)) {
            task.setStatus("CORRECTIONS_REQUIRED");
            task.setReviewStatus("CORRECTIONS_REQUIRED");
            task.setReviewedBy(reviewer);
            task.setReviewedAt(Instant.now());
            task.setReviewNotes(dto.notes() != null ? dto.notes() : "Corrections required");
        } else {
            throw new BadRequestException("Invalid review action: " + dto.action());
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

        UserSummaryDto reviewerSummary = null;
        if (t.getReviewedBy() != null) {
            reviewerSummary = new UserSummaryDto(
                t.getReviewedBy().getId(),
                t.getReviewedBy().getFullName(),
                t.getReviewedBy().getEmail(),
                t.getReviewedBy().getAvatarUrl()
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
            t.getProgressPct(),
            t.getReviewStatus() != null ? t.getReviewStatus() : "NONE",
            reviewerSummary,
            t.getReviewNotes(),
            t.getReviewedAt() != null ? t.getReviewedAt().toString() : null
        );
    }
}
