package com.example.contruction_management_app.backend;

import com.example.contruction_management_app.backend.domain.*;
import com.example.contruction_management_app.backend.dto.AuditSummaryDto;
import com.example.contruction_management_app.backend.repository.*;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import com.example.contruction_management_app.backend.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class AuditLogServiceTest {

    private AuditLogRepository auditLogRepository;
    private ProjectRepository projectRepository;
    private OrganizationRepository organizationRepository;
    private ProjectMemberRepository projectMemberRepository;
    private AuditLogService auditLogService;

    private UserPrincipal testPrincipal;
    private Project mockProject;
    private Organization mockOrg;

    @BeforeEach
    void setUp() {
        auditLogRepository = Mockito.mock(AuditLogRepository.class);
        projectRepository = Mockito.mock(ProjectRepository.class);
        organizationRepository = Mockito.mock(OrganizationRepository.class);
        projectMemberRepository = Mockito.mock(ProjectMemberRepository.class);

        auditLogService = new AuditLogService(
            auditLogRepository,
            projectRepository,
            organizationRepository,
            projectMemberRepository
        );

        testPrincipal = new UserPrincipal(
            "usr_pm_rahul", "rahul@apex.com", "hash",
            "org_apex_infra", "pm", List.of(), true
        );

        mockOrg = new Organization("Apex Infrastructure", "apex-infra");
        mockOrg.setId("org_apex_infra");

        mockProject = new Project("Commercial Tower B", "CTB-01", "Mumbai", new BigDecimal("100000000"), mockOrg);
        mockProject.setId("prj_commercial_tower_b");

        when(projectMemberRepository.existsByProjectIdAndUserId("prj_commercial_tower_b", "usr_pm_rahul")).thenReturn(true);
        when(projectRepository.findById("prj_commercial_tower_b")).thenReturn(Optional.of(mockProject));
        when(organizationRepository.findById("org_apex_infra")).thenReturn(Optional.of(mockOrg));
    }

    @Test
    void testRecordLogAndRetrieveSummary() {
        AuditLog log1 = AuditLog.builder()
            .id("a1")
            .organization(mockOrg)
            .project(mockProject)
            .userId("usr_pm_rahul")
            .userName("Rahul Verma")
            .action(AuditAction.APPROVE)
            .entityType(AuditEntityType.CONTRACTOR_BILL)
            .entityId("bill_001")
            .details("Approved RA Bill #4")
            .createdAt(Instant.now())
            .build();

        AuditLog log2 = AuditLog.builder()
            .id("a2")
            .organization(mockOrg)
            .project(mockProject)
            .userId("usr_pm_rahul")
            .userName("Rahul Verma")
            .action(AuditAction.ISSUE_STOCK)
            .entityType(AuditEntityType.STOCK)
            .entityId("mat_cement")
            .details("Issued 50 bags")
            .createdAt(Instant.now())
            .build();

        AuditLog log3 = AuditLog.builder()
            .id("a3")
            .organization(mockOrg)
            .project(mockProject)
            .userId("usr_eng_priya")
            .userName("Priya Sharma")
            .action(AuditAction.CREATE)
            .entityType(AuditEntityType.DOCUMENT)
            .entityId("doc_arch_001")
            .details("Uploaded drawing v2.1")
            .createdAt(Instant.now())
            .build();

        when(auditLogRepository.findByProjectIdOrderByCreatedAtDesc("prj_commercial_tower_b"))
            .thenReturn(List.of(log1, log2, log3));

        AuditSummaryDto summary = auditLogService.getAuditSummary("prj_commercial_tower_b", testPrincipal);

        assertEquals(3L, summary.totalEvents());
        assertEquals(1L, summary.approvalsCount());
        assertEquals(1L, summary.stockIssuesCount());
        assertEquals(1L, summary.documentsCount());
    }
}
