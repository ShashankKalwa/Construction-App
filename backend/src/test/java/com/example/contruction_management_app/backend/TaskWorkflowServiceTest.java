package com.example.contruction_management_app.backend;

import com.example.contruction_management_app.backend.domain.Organization;
import com.example.contruction_management_app.backend.domain.Phase;
import com.example.contruction_management_app.backend.domain.Project;
import com.example.contruction_management_app.backend.domain.Role;
import com.example.contruction_management_app.backend.domain.Task;
import com.example.contruction_management_app.backend.domain.User;
import com.example.contruction_management_app.backend.dto.TaskDto;
import com.example.contruction_management_app.backend.dto.TaskReviewDto;
import com.example.contruction_management_app.backend.exception.ForbiddenException;
import com.example.contruction_management_app.backend.repository.PhaseRepository;
import com.example.contruction_management_app.backend.repository.ProjectMemberRepository;
import com.example.contruction_management_app.backend.repository.ProjectRepository;
import com.example.contruction_management_app.backend.repository.TaskRepository;
import com.example.contruction_management_app.backend.repository.UserRepository;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import com.example.contruction_management_app.backend.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class TaskWorkflowServiceTest {

    private TaskRepository taskRepository;
    private ProjectRepository projectRepository;
    private PhaseRepository phaseRepository;
    private UserRepository userRepository;
    private ProjectMemberRepository projectMemberRepository;
    private TaskService taskService;

    private Organization org;
    private Project project;
    private Phase phase;
    private User worker;
    private User manager;
    private Task task;

    @BeforeEach
    void setUp() {
        taskRepository = Mockito.mock(TaskRepository.class);
        projectRepository = Mockito.mock(ProjectRepository.class);
        phaseRepository = Mockito.mock(PhaseRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        projectMemberRepository = Mockito.mock(ProjectMemberRepository.class);

        taskService = new TaskService(
            projectRepository,
            phaseRepository,
            taskRepository,
            userRepository,
            projectMemberRepository
        );

        org = new Organization();
        org.setId("org_test");
        org.setName("Apex Infra");

        project = new Project("Tower A", "PRJ-TWR-A", "Bangalore", BigDecimal.valueOf(1000000), org);
        project.setId("prj_001");

        phase = new Phase(project, "Superstructure", 1, LocalDate.now(), LocalDate.now().plusMonths(2));
        phase.setId("phs_001");

        Role roleWorker = new Role();
        roleWorker.setId("site_supervisor");
        worker = new User("worker@test.com", "hash", "Worker Amit", org, roleWorker);
        worker.setId("usr_worker");

        Role roleManager = new Role();
        roleManager.setId("project_manager");
        manager = new User("manager@test.com", "hash", "Manager Rahul", org, roleManager);
        manager.setId("usr_manager");

        task = new Task(phase, project, "Cast 2nd Floor Columns", "Pour M25 concrete", worker, "HIGH", LocalDate.now(), LocalDate.now().plusDays(5));
        task.setId("tsk_001");
        task.setStatus("SUBMITTED_FOR_REVIEW");
        task.setReviewStatus("PENDING_REVIEW");
        task.setProgressPct(100.0);

        when(projectRepository.findById("prj_001")).thenReturn(Optional.of(project));
        when(taskRepository.findById("tsk_001")).thenReturn(Optional.of(task));
        when(userRepository.findById("usr_manager")).thenReturn(Optional.of(manager));
        when(userRepository.findById("usr_worker")).thenReturn(Optional.of(worker));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void testSelfReviewIsForbidden() {
        // Worker tries to review their own task
        UserPrincipal workerPrincipal = new UserPrincipal(
            "usr_worker",
            "worker@test.com",
            "hash",
            "org_test",
            "site_supervisor",
            Collections.emptyList()
        );

        when(projectMemberRepository.existsByProjectIdAndUserId("prj_001", "usr_worker")).thenReturn(true);

        TaskReviewDto dto = new TaskReviewDto("APPROVE", "Looks good");

        assertThrows(ForbiddenException.class, () -> {
            taskService.reviewTask("tsk_001", dto, workerPrincipal);
        });
    }

    @Test
    void testManagerReviewApproval() {
        UserPrincipal managerPrincipal = new UserPrincipal(
            "usr_manager",
            "manager@test.com",
            "hash",
            "org_test",
            "project_manager",
            Collections.emptyList()
        );

        when(projectMemberRepository.existsByProjectIdAndUserId("prj_001", "usr_manager")).thenReturn(true);

        TaskReviewDto dto = new TaskReviewDto("APPROVE", "Rebar alignment and concrete cube test passed");
        TaskDto result = taskService.reviewTask("tsk_001", dto, managerPrincipal);

        assertEquals("COMPLETED", result.status());
        assertEquals("APPROVED", result.reviewStatus());
        assertNotNull(result.reviewedBy());
        assertEquals("usr_manager", result.reviewedBy().id());
    }

    @Test
    void testManagerRequestCorrections() {
        UserPrincipal managerPrincipal = new UserPrincipal(
            "usr_manager",
            "manager@test.com",
            "hash",
            "org_test",
            "project_manager",
            Collections.emptyList()
        );

        when(projectMemberRepository.existsByProjectIdAndUserId("prj_001", "usr_manager")).thenReturn(true);

        TaskReviewDto dto = new TaskReviewDto("REQUEST_CORRECTIONS", "Honeycombing detected on column C3, patch and cure before approval");
        TaskDto result = taskService.reviewTask("tsk_001", dto, managerPrincipal);

        assertEquals("CORRECTIONS_REQUIRED", result.status());
        assertEquals("CORRECTIONS_REQUIRED", result.reviewStatus());
        assertEquals("Honeycombing detected on column C3, patch and cure before approval", result.reviewNotes());
    }
}
