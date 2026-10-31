package com.example.contruction_management_app.backend;

import com.example.contruction_management_app.backend.domain.*;
import com.example.contruction_management_app.backend.dto.AuditSummaryDto;
import com.example.contruction_management_app.backend.dto.DashboardSummaryDto;
import com.example.contruction_management_app.backend.exception.ForbiddenException;
import com.example.contruction_management_app.backend.repository.*;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import com.example.contruction_management_app.backend.service.AuditLogService;
import com.example.contruction_management_app.backend.service.DashboardService;
import com.example.contruction_management_app.backend.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class Phase6SecurityAndAuditIntegrationTest {

    private AuditLogRepository auditLogRepository;
    private ProjectRepository projectRepository;
    private OrganizationRepository organizationRepository;
    private ProjectMemberRepository projectMemberRepository;
    private NotificationRepository notificationRepository;
    private UserRepository userRepository;
    private TaskRepository taskRepository;

    private AuditLogService auditLogService;
    private NotificationService notificationService;
    private DashboardService dashboardService;

    private Organization orgApex;
    private Organization orgCompetitor;
    private Project projectCtb;
    private Project projectCompetitor;
    private User pmRahul;
    private UserPrincipal principalRahul;
    private UserPrincipal principalCompetitor;

    @BeforeEach
    void setUp() {
        auditLogRepository = mock(AuditLogRepository.class);
        projectRepository = mock(ProjectRepository.class);
        organizationRepository = mock(OrganizationRepository.class);
        projectMemberRepository = mock(ProjectMemberRepository.class);
        notificationRepository = mock(NotificationRepository.class);
        userRepository = mock(UserRepository.class);
        taskRepository = mock(TaskRepository.class);

        auditLogService = new AuditLogService(
            auditLogRepository,
            projectRepository,
            organizationRepository,
            projectMemberRepository
        );

        notificationService = new NotificationService(
            notificationRepository,
            userRepository,
            organizationRepository,
            projectRepository
        );

        dashboardService = new DashboardService(projectRepository, taskRepository);

        orgApex = new Organization("Apex Infra Ltd", "apex-infra");
        orgApex.setId("org_apex_infra");

        orgCompetitor = new Organization("Competitor Builders", "comp-builders");
        orgCompetitor.setId("org_competitor");

        projectCtb = new Project("Commercial Tower B", "CTB-02", "Bangalore", new BigDecimal("54000000.00"), orgApex);
        projectCtb.setId("prj_commercial_tower_b");
        projectCtb.setProgressPct(42.0);

        projectCompetitor = new Project("Competitor Tower", "COMP-01", "Delhi", new BigDecimal("30000000.00"), orgCompetitor);
        projectCompetitor.setId("prj_competitor_tower");

        pmRahul = new User();
        pmRahul.setId("usr_pm_rahul");
        pmRahul.setEmail("rahul@apex.com");
        pmRahul.setFullName("Rahul Verma");
        pmRahul.setOrganization(orgApex);

        principalRahul = new UserPrincipal(
            "usr_pm_rahul", "rahul@apex.com", "hash",
            "org_apex_infra", "project_manager",
            List.of(new SimpleGrantedAuthority("ROLE_PROJECT_MANAGER")), true
        );

        principalCompetitor = new UserPrincipal(
            "usr_comp_pm", "pm@competitor.com", "hash",
            "org_competitor", "project_manager",
            List.of(new SimpleGrantedAuthority("ROLE_PROJECT_MANAGER")), true
        );
    }

    @Test
    void testMultiTenantIsolationOnAuditLogs() {
        when(projectMemberRepository.existsByProjectIdAndUserId("prj_commercial_tower_b", "usr_pm_rahul")).thenReturn(true);
        when(projectMemberRepository.existsByProjectIdAndUserId("prj_commercial_tower_b", "usr_comp_pm")).thenReturn(false);

        // Rahul can access CTB audit logs
        assertDoesNotThrow(() -> auditLogService.getAuditLogsByProject("prj_commercial_tower_b", null, principalRahul));

        // Competitor PM cannot access CTB audit logs
        assertThrows(ForbiddenException.class, () ->
            auditLogService.getAuditLogsByProject("prj_commercial_tower_b", null, principalCompetitor));
    }

    @Test
    void testAuditLogRecordingAndSummaryAggregation() {
        when(projectMemberRepository.existsByProjectIdAndUserId("prj_commercial_tower_b", "usr_pm_rahul")).thenReturn(true);
        when(organizationRepository.findById("org_apex_infra")).thenReturn(Optional.of(orgApex));
        when(projectRepository.findById("prj_commercial_tower_b")).thenReturn(Optional.of(projectCtb));

        // 1. Record log
        auditLogService.recordLog(
            "org_apex_infra",
            "prj_commercial_tower_b",
            "usr_pm_rahul",
            "Rahul Verma",
            AuditAction.APPROVE,
            AuditEntityType.EXPENSE,
            "exp_001",
            "Approved site rig expense ₹120,000"
        );

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog saved = captor.getValue();
        assertEquals("org_apex_infra", saved.getOrganization().getId());
        assertEquals("prj_commercial_tower_b", saved.getProject().getId());
        assertEquals(AuditAction.APPROVE, saved.getAction());
        assertEquals(AuditEntityType.EXPENSE, saved.getEntityType());

        // 2. Summary aggregation
        AuditLog l1 = AuditLog.builder()
            .action(AuditAction.APPROVE)
            .entityType(AuditEntityType.EXPENSE)
            .createdAt(Instant.now())
            .build();
        AuditLog l2 = AuditLog.builder()
            .action(AuditAction.ISSUE_STOCK)
            .entityType(AuditEntityType.STOCK)
            .createdAt(Instant.now())
            .build();
        AuditLog l3 = AuditLog.builder()
            .action(AuditAction.CREATE)
            .entityType(AuditEntityType.DOCUMENT)
            .createdAt(Instant.now())
            .build();
        AuditLog l4 = AuditLog.builder()
            .action(AuditAction.PAY_BILL)
            .entityType(AuditEntityType.CONTRACTOR_BILL)
            .createdAt(Instant.now())
            .build();

        when(auditLogRepository.findByProjectIdOrderByCreatedAtDesc("prj_commercial_tower_b"))
            .thenReturn(List.of(l1, l2, l3, l4));

        AuditSummaryDto summary = auditLogService.getAuditSummary("prj_commercial_tower_b", principalRahul);
        assertEquals(4, summary.totalEvents());
        assertEquals(1, summary.approvalsCount());
        assertEquals(1, summary.stockIssuesCount());
        assertEquals(1, summary.documentsCount());
        assertEquals(1, summary.paymentsCount());
    }

    @Test
    void testNotificationCreationAndDispatch() {
        when(userRepository.findById("usr_pm_rahul")).thenReturn(Optional.of(pmRahul));
        when(organizationRepository.findById("org_apex_infra")).thenReturn(Optional.of(orgApex));
        when(projectRepository.findById("prj_commercial_tower_b")).thenReturn(Optional.of(projectCtb));

        notificationService.sendNotification(
            "org_apex_infra",
            "usr_pm_rahul",
            "prj_commercial_tower_b",
            "Low Stock Alert: 12mm Rebar",
            "Stock below 6 MT",
            NotificationType.STOCK_ALERT,
            "mat_steel_12",
            "/inventory"
        );

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification n = captor.getValue();
        assertEquals("usr_pm_rahul", n.getUser().getId());
        assertEquals(NotificationType.STOCK_ALERT, n.getType());
        assertFalse(n.getIsRead());
    }

    @Test
    void testDashboardPortfolioVsProjectSpecificTelemetry() {
        when(projectRepository.findAllByOrganizationAndUser("org_apex_infra", "usr_pm_rahul"))
            .thenReturn(List.of(projectCtb));
        when(taskRepository.countByProjectIdAndStatus("prj_commercial_tower_b", "PENDING")).thenReturn(3L);
        when(taskRepository.countByProjectIdAndStatus("prj_commercial_tower_b", "IN_PROGRESS")).thenReturn(5L);
        when(taskRepository.countByProjectIdAndStatus("prj_commercial_tower_b", "BLOCKED")).thenReturn(1L);
        when(taskRepository.countByProjectIdAndStatus("prj_commercial_tower_b", "COMPLETED")).thenReturn(10L);

        // Project specific summary
        DashboardSummaryDto projectSummary = dashboardService.getSummary("prj_commercial_tower_b", principalRahul);
        assertEquals(1, projectSummary.activeProjectsCount());
        assertEquals(new BigDecimal("54000000.00"), projectSummary.totalBudgetInr());
        assertEquals(3, projectSummary.pendingTasksCount());
        assertEquals(5, projectSummary.inProgressTasksCount());
        assertEquals(1, projectSummary.blockedTasksCount());
        assertEquals(10, projectSummary.completedTasksCount());

        // Portfolio summary (null projectId)
        DashboardSummaryDto portfolioSummary = dashboardService.getSummary(null, principalRahul);
        assertEquals(1, portfolioSummary.activeProjectsCount());
        assertEquals(new BigDecimal("54000000.00"), portfolioSummary.totalBudgetInr());
        assertEquals(3, portfolioSummary.pendingTasksCount());
    }
}

