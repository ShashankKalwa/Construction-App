package com.example.contruction_management_app.backend;

import com.example.contruction_management_app.backend.domain.Organization;
import com.example.contruction_management_app.backend.domain.Project;
import com.example.contruction_management_app.backend.domain.ProjectMember;
import com.example.contruction_management_app.backend.domain.Role;
import com.example.contruction_management_app.backend.domain.User;
import com.example.contruction_management_app.backend.dto.ProjectResponseDto;
import com.example.contruction_management_app.backend.dto.ProjectStatusUpdateDto;
import com.example.contruction_management_app.backend.exception.BadRequestException;
import com.example.contruction_management_app.backend.repository.OrganizationRepository;
import com.example.contruction_management_app.backend.repository.PhaseRepository;
import com.example.contruction_management_app.backend.repository.ProjectMemberRepository;
import com.example.contruction_management_app.backend.repository.ProjectRepository;
import com.example.contruction_management_app.backend.repository.TaskRepository;
import com.example.contruction_management_app.backend.repository.UserRepository;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import com.example.contruction_management_app.backend.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ProjectLifecycleServiceTest {

    private ProjectRepository projectRepository;
    private ProjectMemberRepository projectMemberRepository;
    private OrganizationRepository organizationRepository;
    private UserRepository userRepository;
    private PhaseRepository phaseRepository;
    private TaskRepository taskRepository;
    private ProjectService projectService;

    private Organization org;
    private Project project;
    private User admin;
    private UserPrincipal adminPrincipal;

    @BeforeEach
    void setUp() {
        projectRepository = Mockito.mock(ProjectRepository.class);
        projectMemberRepository = Mockito.mock(ProjectMemberRepository.class);
        organizationRepository = Mockito.mock(OrganizationRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        phaseRepository = Mockito.mock(PhaseRepository.class);
        taskRepository = Mockito.mock(TaskRepository.class);

        projectService = new ProjectService(
            projectRepository,
            projectMemberRepository,
            organizationRepository,
            userRepository,
            phaseRepository,
            taskRepository
        );

        org = new Organization();
        org.setId("org_apex");
        org.setName("Apex Infra");

        project = new Project("Metro Station 4", "PRJ-METRO-04", "Hyderabad", BigDecimal.valueOf(50000000), org);
        project.setId("prj_metro");
        project.setStatus("PLANNING");

        Role roleAdmin = new Role();
        roleAdmin.setId("company_admin");
        admin = new User("admin@apex.com", "hash", "Rajesh Sharma", org, roleAdmin);
        admin.setId("usr_admin");

        adminPrincipal = new UserPrincipal(
            "usr_admin",
            "admin@apex.com",
            "hash",
            "org_apex",
            "company_admin",
            Collections.emptyList()
        );

        when(projectRepository.findById("prj_metro")).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void testActiveTransitionFailsWithoutActiveMembers() {
        when(projectMemberRepository.countByProjectIdAndStatus("prj_metro", "ACTIVE")).thenReturn(0L);

        ProjectStatusUpdateDto dto = new ProjectStatusUpdateDto("ACTIVE");

        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            projectService.updateProjectStatus("prj_metro", dto, adminPrincipal);
        });
        assertTrue(ex.getMessage().contains("without at least one assigned team member"));
    }

    @Test
    void testActiveTransitionSucceedsWithBudgetAndMembers() {
        when(projectMemberRepository.countByProjectIdAndStatus("prj_metro", "ACTIVE")).thenReturn(2L);

        ProjectStatusUpdateDto dto = new ProjectStatusUpdateDto("ACTIVE");
        ProjectResponseDto result = projectService.updateProjectStatus("prj_metro", dto, adminPrincipal);

        assertEquals("ACTIVE", result.status());
    }

    @Test
    void testRemoveMemberSetsStatusInactive() {
        User worker = new User();
        worker.setId("usr_worker_01");
        ProjectMember member = new ProjectMember(project, worker, "site_supervisor", org, "ACTIVE");

        when(projectMemberRepository.findByProjectIdAndUserId("prj_metro", "usr_worker_01"))
            .thenReturn(Optional.of(member));
        when(projectMemberRepository.save(any(ProjectMember.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        projectService.removeMember("prj_metro", "usr_worker_01", adminPrincipal);

        assertEquals("INACTIVE", member.getStatus());
    }
}
