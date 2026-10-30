package com.example.contruction_management_app.backend.service;

import com.example.contruction_management_app.backend.domain.*;
import com.example.contruction_management_app.backend.dto.*;
import com.example.contruction_management_app.backend.exception.ForbiddenException;
import com.example.contruction_management_app.backend.exception.ResourceNotFoundException;
import com.example.contruction_management_app.backend.repository.*;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SiteOperationsService {

    private final DailySiteReportRepository reportRepository;
    private final SiteIssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final WorkProgressRepository workProgressRepository;
    private final SitePhotoRepository photoRepository;

    public SiteOperationsService(
        DailySiteReportRepository reportRepository,
        SiteIssueRepository issueRepository,
        ProjectRepository projectRepository,
        ProjectMemberRepository projectMemberRepository,
        WorkProgressRepository workProgressRepository,
        SitePhotoRepository photoRepository
    ) {
        this.reportRepository = reportRepository;
        this.issueRepository = issueRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.workProgressRepository = workProgressRepository;
        this.photoRepository = photoRepository;
    }

    @Transactional
    public DailySiteReportDto createDailyReport(String projectId, DailySiteReportCreateDto dto, UserPrincipal principal) {
        Project project = validateProjectAccess(projectId, principal);

        String reportId = "dsr_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        DailySiteReport report = DailySiteReport.builder()
            .id(reportId)
            .project(project)
            .companyId(project.getOrganization().getId())
            .reportDate(dto.reportDate())
            .weather(dto.weather())
            .workSummary(dto.workSummary())
            .plannedTomorrow(dto.plannedTomorrow())
            .status(DsrStatus.SUBMITTED)
            .createdBy(principal.getId())
            .createdAt(Instant.now())
            .laborAttendances(new ArrayList<>())
            .build();

        if (dto.laborAttendances() != null && !dto.laborAttendances().isEmpty()) {
            for (LaborAttendanceDto laDto : dto.laborAttendances()) {
                String laId = "la_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
                BigDecimal hours = laDto.hoursWorked() != null ? laDto.hoursWorked() : BigDecimal.valueOf(8.0);
                LaborAttendance la = LaborAttendance.builder()
                    .id(laId)
                    .report(report)
                    .trade(laDto.trade())
                    .headcount(laDto.headcount())
                    .hoursWorked(hours)
                    .contractorName(laDto.contractorName())
                    .build();
                report.getLaborAttendances().add(la);
            }
        }

        DailySiteReport saved = reportRepository.save(report);
        return DailySiteReportDto.fromEntity(saved);
    }

    @Transactional
    public DailySiteReportDto approveDailyReport(String projectId, String reportId, DsrApprovalDto dto, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);

        DailySiteReport report = reportRepository.findById(reportId)
            .orElseThrow(() -> new ResourceNotFoundException("Daily site report not found: " + reportId));

        if (!report.getProject().getId().equals(projectId)) {
            throw new ResourceNotFoundException("Report does not belong to project: " + projectId);
        }

        // Enforce dual-control: Submitter cannot approve their own DSR
        if (report.getCreatedBy() != null && report.getCreatedBy().equals(principal.getId())) {
            throw new AccessDeniedException("Self-approval not permitted. Submitter cannot approve their own daily site report.");
        }

        if ("APPROVE".equalsIgnoreCase(dto.action())) {
            report.setStatus(DsrStatus.APPROVED);
        } else {
            report.setStatus(DsrStatus.CORRECTIONS_REQUIRED);
        }

        report.setApprovedBy(principal.getId());
        report.setApprovedAt(Instant.now());
        report.setApprovalNotes(dto.notes());

        DailySiteReport saved = reportRepository.save(report);
        return DailySiteReportDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<DailySiteReportDto> getDailyReportsForProject(String projectId, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);
        return reportRepository.findByProjectIdOrderByReportDateDesc(projectId).stream()
            .map(DailySiteReportDto::fromEntity)
            .toList();
    }

    // Work Progress (Reported vs Verified Quantities)
    @Transactional
    public WorkProgressDto recordWorkProgress(String projectId, WorkProgressCreateDto dto, UserPrincipal principal) {
        Project project = validateProjectAccess(projectId, principal);

        String id = "wp_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        WorkProgressRecord record = WorkProgressRecord.builder()
            .id(id)
            .project(project)
            .companyId(project.getOrganization().getId())
            .taskId(dto.taskId())
            .reportId(dto.reportId())
            .itemName(dto.itemName())
            .unit(dto.unit())
            .plannedQuantity(dto.plannedQuantity())
            .reportedQuantity(dto.reportedQuantity())
            .verifiedQuantity(BigDecimal.ZERO)
            .status(WorkProgressStatus.REPORTED)
            .reportedBy(principal.getId())
            .recordedAt(Instant.now())
            .build();

        WorkProgressRecord saved = workProgressRepository.save(record);
        return WorkProgressDto.fromEntity(saved);
    }

    @Transactional
    public WorkProgressDto verifyWorkProgress(String projectId, String progressId, WorkProgressVerifyDto dto, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);

        WorkProgressRecord record = workProgressRepository.findById(progressId)
            .orElseThrow(() -> new ResourceNotFoundException("Work progress record not found: " + progressId));

        if (!record.getProject().getId().equals(projectId)) {
            throw new ResourceNotFoundException("Record does not belong to project: " + projectId);
        }

        // Enforce dual-control: Submitter cannot verify their own reported progress
        if (record.getReportedBy() != null && record.getReportedBy().equals(principal.getId())) {
            throw new AccessDeniedException("Self-verification not permitted. Submitter cannot verify their own reported progress.");
        }

        if ("VERIFY".equalsIgnoreCase(dto.action()) || "APPROVE".equalsIgnoreCase(dto.action())) {
            record.setStatus(WorkProgressStatus.VERIFIED);
            record.setVerifiedQuantity(dto.verifiedQuantity());
        } else {
            record.setStatus(WorkProgressStatus.REJECTED);
            record.setVerifiedQuantity(BigDecimal.ZERO);
        }

        record.setVerifiedBy(principal.getId());
        record.setVerifiedAt(Instant.now());
        record.setVerificationNotes(dto.notes());

        WorkProgressRecord saved = workProgressRepository.save(record);
        return WorkProgressDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<WorkProgressDto> getWorkProgressForProject(String projectId, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);
        return workProgressRepository.findByProjectIdOrderByRecordedAtDesc(projectId).stream()
            .map(WorkProgressDto::fromEntity)
            .toList();
    }

    // Photo Evidence
    @Transactional
    public SitePhotoDto saveSitePhoto(String projectId, SitePhotoCreateDto dto, UserPrincipal principal) {
        Project project = validateProjectAccess(projectId, principal);

        String id = "photo_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        SitePhoto photo = SitePhoto.builder()
            .id(id)
            .project(project)
            .companyId(project.getOrganization().getId())
            .reportId(dto.reportId())
            .taskId(dto.taskId())
            .defectId(dto.defectId())
            .photoUrl(dto.photoUrl())
            .caption(dto.caption())
            .locationTag(dto.locationTag())
            .capturedAt(Instant.now())
            .uploadedBy(principal.getId())
            .build();

        SitePhoto saved = photoRepository.save(photo);
        return SitePhotoDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<SitePhotoDto> getPhotosForProject(String projectId, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);
        return photoRepository.findByProjectIdOrderByCapturedAtDesc(projectId).stream()
            .map(SitePhotoDto::fromEntity)
            .toList();
    }

    // Site Issues / Snags
    @Transactional
    public SiteIssueDto logSiteIssue(String projectId, SiteIssueCreateDto dto, UserPrincipal principal) {
        Project project = validateProjectAccess(projectId, principal);

        DailySiteReport report = null;
        if (dto.reportId() != null && !dto.reportId().isBlank()) {
            report = reportRepository.findById(dto.reportId()).orElse(null);
        }

        String issueId = "issue_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        SiteIssue issue = SiteIssue.builder()
            .id(issueId)
            .project(project)
            .report(report)
            .title(dto.title())
            .description(dto.description())
            .severity(dto.severity())
            .status(IssueStatus.OPEN)
            .assignedTo(dto.assignedTo())
            .createdBy(principal.getId())
            .createdAt(Instant.now())
            .build();

        SiteIssue saved = issueRepository.save(issue);
        return SiteIssueDto.fromEntity(saved);
    }

    @Transactional
    public SiteIssueDto updateIssueStatus(String issueId, SiteIssueStatusUpdateDto dto, UserPrincipal principal) {
        SiteIssue issue = issueRepository.findById(issueId)
            .orElseThrow(() -> new ResourceNotFoundException("Site issue not found: " + issueId));

        validateProjectAccess(issue.getProject().getId(), principal);

        issue.setStatus(dto.status());
        if (dto.status() == IssueStatus.RESOLVED) {
            issue.setResolvedAt(Instant.now());
        } else {
            issue.setResolvedAt(null);
        }

        SiteIssue saved = issueRepository.save(issue);
        return SiteIssueDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<SiteIssueDto> getSiteIssuesForProject(String projectId, IssueStatus status, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);
        List<SiteIssue> issues;
        if (status != null) {
            issues = issueRepository.findByProjectIdAndStatusOrderByCreatedAtDesc(projectId, status);
        } else {
            issues = issueRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
        }
        return issues.stream().map(SiteIssueDto::fromEntity).toList();
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
}
