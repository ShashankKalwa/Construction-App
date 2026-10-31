package com.example.contruction_management_app.backend;

import com.example.contruction_management_app.backend.domain.*;
import com.example.contruction_management_app.backend.dto.*;
import com.example.contruction_management_app.backend.repository.*;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import com.example.contruction_management_app.backend.service.SiteInspectionService;
import com.example.contruction_management_app.backend.service.SiteOperationsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SiteOperationsWorkflowTest {

    @Mock
    private DailySiteReportRepository reportRepository;
    @Mock
    private SiteIssueRepository issueRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectMemberRepository projectMemberRepository;
    @Mock
    private WorkProgressRepository workProgressRepository;
    @Mock
    private SitePhotoRepository photoRepository;
    @Mock
    private SiteInspectionRepository inspectionRepository;
    @Mock
    private SiteDefectRepository defectRepository;

    private SiteOperationsService siteOperationsService;
    private SiteInspectionService siteInspectionService;

    private Project mockProject;
    private Organization mockOrg;
    private UserPrincipal supervisorUser;
    private UserPrincipal engineerUser;

    @BeforeEach
    void setUp() {
        siteOperationsService = new SiteOperationsService(
            reportRepository,
            issueRepository,
            projectRepository,
            projectMemberRepository,
            workProgressRepository,
            photoRepository
        );

        siteInspectionService = new SiteInspectionService(
            inspectionRepository,
            defectRepository,
            projectRepository,
            projectMemberRepository
        );

        mockOrg = new Organization("Apex Infra", "apex-infra");
        mockOrg.setId("org_apex");

        mockProject = new Project("Skyline Tower", "SK-01", "Whitefield", new BigDecimal("50000000"), mockOrg);
        mockProject.setId("prj_01");

        supervisorUser = new UserPrincipal(
            "usr_sup",
            "supervisor@apex.com",
            "hash",
            "org_apex",
            "site_supervisor",
            List.of(new SimpleGrantedAuthority("reports:daily_site"))
        );

        engineerUser = new UserPrincipal(
            "usr_eng",
            "engineer@apex.com",
            "hash",
            "org_apex",
            "civil_engineer",
            List.of(new SimpleGrantedAuthority("tasks:manage"), new SimpleGrantedAuthority("reports:daily_site"))
        );

        lenient().when(projectRepository.findById("prj_01")).thenReturn(Optional.of(mockProject));
        lenient().when(projectMemberRepository.existsByProjectIdAndUserId("prj_01", "usr_sup")).thenReturn(true);
        lenient().when(projectMemberRepository.existsByProjectIdAndUserId("prj_01", "usr_eng")).thenReturn(true);
    }

    @Test
    void testDailySiteReportApproval_EnforcesSelfApprovalRestriction() {
        DailySiteReport report = DailySiteReport.builder()
            .id("dsr_01")
            .project(mockProject)
            .reportDate(LocalDate.now())
            .weather(WeatherCondition.SUNNY)
            .workSummary("Excavation completed")
            .status(DsrStatus.SUBMITTED)
            .createdBy("usr_sup")
            .build();

        when(reportRepository.findById("dsr_01")).thenReturn(Optional.of(report));

        // Submitter cannot approve their own DSR
        DsrApprovalDto approveDto = new DsrApprovalDto("APPROVE", "Looks good");
        assertThrows(AccessDeniedException.class, () ->
            siteOperationsService.approveDailyReport("prj_01", "dsr_01", approveDto, supervisorUser)
        );

        // Civil Engineer can approve
        when(reportRepository.save(any(DailySiteReport.class))).thenAnswer(invocation -> invocation.getArgument(0));
        DailySiteReportDto approved = siteOperationsService.approveDailyReport("prj_01", "dsr_01", approveDto, engineerUser);

        assertEquals(DsrStatus.APPROVED, approved.status());
        assertEquals("usr_eng", approved.approvedBy());
        assertEquals("Looks good", approved.approvalNotes());
    }

    @Test
    void testWorkProgressVerification_EnforcesDualControl() {
        WorkProgressRecord record = WorkProgressRecord.builder()
            .id("wp_01")
            .project(mockProject)
            .companyId("org_apex")
            .itemName("Concrete M30 Raft")
            .unit("m3")
            .plannedQuantity(new BigDecimal("200.00"))
            .reportedQuantity(new BigDecimal("180.00"))
            .status(WorkProgressStatus.REPORTED)
            .reportedBy("usr_sup")
            .build();

        when(workProgressRepository.findById("wp_01")).thenReturn(Optional.of(record));

        // Reporter cannot self-verify
        WorkProgressVerifyDto verifyDto = new WorkProgressVerifyDto("VERIFY", new BigDecimal("175.00"), "Core test verified");
        assertThrows(AccessDeniedException.class, () ->
            siteOperationsService.verifyWorkProgress("prj_01", "wp_01", verifyDto, supervisorUser)
        );

        // Engineer can verify and set physical verified quantity
        when(workProgressRepository.save(any(WorkProgressRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));
        WorkProgressDto verified = siteOperationsService.verifyWorkProgress("prj_01", "wp_01", verifyDto, engineerUser);

        assertEquals(WorkProgressStatus.VERIFIED, verified.status());
        assertEquals(new BigDecimal("175.00"), verified.verifiedQuantity());
        assertEquals("usr_eng", verified.verifiedBy());
        assertEquals("Core test verified", verified.verificationNotes());
    }

    @Test
    void testInspectionCreation_GeneratesDefectOnFailure() {
        SiteInspectionCreateDto dto = new SiteInspectionCreateDto(
            "Column C4 Reinforcement",
            InspectionType.REBAR,
            InspectionStatus.FAILED,
            "[{\"item\": \"Stirrups spacing\", \"passed\": false}]",
            LocalDate.now(),
            "Tower B - 2nd Floor",
            "Stirrup spacing exceeds 200mm limit"
        );

        when(inspectionRepository.save(any(SiteInspection.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(defectRepository.save(any(SiteDefect.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SiteInspectionDto inspection = siteInspectionService.createInspection("prj_01", dto, engineerUser);

        assertEquals(InspectionStatus.FAILED, inspection.status());
        assertEquals(InspectionType.REBAR, inspection.inspectionType());
        // Verify automatic defect creation
        verify(defectRepository, times(1)).save(any(SiteDefect.class));
    }

    @Test
    void testDefectReinspection_ClosedWhenPasses_ReopenedWhenFails() {
        SiteDefect defect = SiteDefect.builder()
            .id("def_01")
            .project(mockProject)
            .companyId("org_apex")
            .title("Honeycombing on shear wall")
            .description("Air voids requiring micro-concrete grouting")
            .status(DefectStatus.RESOLVED)
            .createdBy("usr_eng")
            .build();

        when(defectRepository.findById("def_01")).thenReturn(Optional.of(defect));
        when(defectRepository.save(any(SiteDefect.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Re-inspection PASS -> CLOSED
        SiteDefectReinspectDto passDto = new SiteDefectReinspectDto("CLOSE", "Grouting completed cleanly");
        SiteDefectDto closed = siteInspectionService.reinspectDefect("def_01", passDto, engineerUser);
        assertEquals(DefectStatus.CLOSED, closed.status());
        assertEquals("usr_eng", closed.reInspectedBy());

        // Re-inspection FAIL -> Reverts back to OPEN
        SiteDefectReinspectDto failDto = new SiteDefectReinspectDto("REJECT", "Inadequate bonding observed");
        SiteDefectDto reopened = siteInspectionService.reinspectDefect("def_01", failDto, engineerUser);
        assertEquals(DefectStatus.OPEN, reopened.status());
    }
}
