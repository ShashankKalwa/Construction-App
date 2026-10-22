package com.example.contruction_management_app.backend.service;

import com.example.contruction_management_app.backend.domain.*;
import com.example.contruction_management_app.backend.dto.*;
import com.example.contruction_management_app.backend.exception.ForbiddenException;
import com.example.contruction_management_app.backend.exception.ResourceNotFoundException;
import com.example.contruction_management_app.backend.repository.*;
import com.example.contruction_management_app.backend.security.UserPrincipal;
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

    public SiteOperationsService(
        DailySiteReportRepository reportRepository,
        SiteIssueRepository issueRepository,
        ProjectRepository projectRepository,
        ProjectMemberRepository projectMemberRepository
    ) {
        this.reportRepository = reportRepository;
        this.issueRepository = issueRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Transactional
    public DailySiteReportDto createDailyReport(String projectId, DailySiteReportCreateDto dto, UserPrincipal principal) {
        Project project = validateProjectAccess(projectId, principal);

        String reportId = "dsr_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        DailySiteReport report = DailySiteReport.builder()
            .id(reportId)
            .project(project)
            .reportDate(dto.reportDate())
            .weather(dto.weather())
            .workSummary(dto.workSummary())
            .plannedTomorrow(dto.plannedTomorrow())
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

    @Transactional(readOnly = true)
    public List<DailySiteReportDto> getDailyReportsForProject(String projectId, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);
        return reportRepository.findByProjectIdOrderByReportDateDesc(projectId).stream()
            .map(DailySiteReportDto::fromEntity)
            .toList();
    }

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
