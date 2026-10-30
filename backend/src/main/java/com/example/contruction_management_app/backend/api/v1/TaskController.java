package com.example.contruction_management_app.backend.api.v1;

import com.example.contruction_management_app.backend.dto.PhaseCreateDto;
import com.example.contruction_management_app.backend.dto.PhaseDto;
import com.example.contruction_management_app.backend.dto.TaskCreateDto;
import com.example.contruction_management_app.backend.dto.TaskDto;
import com.example.contruction_management_app.backend.dto.TaskProgressUpdateDto;
import com.example.contruction_management_app.backend.dto.TaskReviewDto;
import com.example.contruction_management_app.backend.dto.TaskSubmitReviewDto;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import com.example.contruction_management_app.backend.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/projects/{projectId}/phases")
    public ResponseEntity<List<PhaseDto>> getPhasesWithTasks(
        @PathVariable String projectId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<PhaseDto> phases = taskService.getPhasesWithTasks(projectId, principal);
        return ResponseEntity.ok(phases);
    }

    @PostMapping("/projects/{projectId}/phases")
    @PreAuthorize("@projectAccess.can(#projectId, 'tasks:manage') or hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<PhaseDto> createPhase(
        @PathVariable String projectId,
        @Valid @RequestBody PhaseCreateDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        PhaseDto phase = taskService.createPhase(projectId, dto, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(phase);
    }

    @PostMapping("/projects/{projectId}/tasks")
    @PreAuthorize("hasAuthority('tasks:manage') or hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<TaskDto> createTask(
        @PathVariable String projectId,
        @Valid @RequestBody TaskCreateDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        TaskDto task = taskService.createTask(projectId, dto, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @PatchMapping("/tasks/{taskId}/progress")
    @PreAuthorize("hasAuthority('tasks:update') or hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<TaskDto> updateTaskProgress(
        @PathVariable String taskId,
        @Valid @RequestBody TaskProgressUpdateDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        TaskDto task = taskService.updateTaskProgress(taskId, dto, principal);
        return ResponseEntity.ok(task);
    }

    @PostMapping("/tasks/{taskId}/submit-review")
    @PreAuthorize("hasAuthority('tasks:update') or hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<TaskDto> submitForReview(
        @PathVariable String taskId,
        @Valid @RequestBody TaskSubmitReviewDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        TaskDto task = taskService.submitForReview(taskId, dto, principal);
        return ResponseEntity.ok(task);
    }

    @PostMapping("/tasks/{taskId}/review")
    @PreAuthorize("hasAuthority('tasks:manage') or hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<TaskDto> reviewTask(
        @PathVariable String taskId,
        @Valid @RequestBody TaskReviewDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        TaskDto task = taskService.reviewTask(taskId, dto, principal);
        return ResponseEntity.ok(task);
    }
}
