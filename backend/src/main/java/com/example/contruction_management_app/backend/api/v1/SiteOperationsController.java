package com.example.contruction_management_app.backend.api.v1;

import com.example.contruction_management_app.backend.domain.IssueStatus;
import com.example.contruction_management_app.backend.dto.*;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import com.example.contruction_management_app.backend.service.SiteOperationsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class SiteOperationsController {

    private final SiteOperationsService siteOperationsService;

    public SiteOperationsController(SiteOperationsService siteOperationsService) {
        this.siteOperationsService = siteOperationsService;
    }

    // Daily Site Reports
    @PostMapping("/projects/{projectId}/daily-reports")
    @PreAuthorize("hasAuthority('reports:daily_site') or hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<DailySiteReportDto> createDailyReport(
        @PathVariable String projectId,
        @Valid @RequestBody DailySiteReportCreateDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        DailySiteReportDto report = siteOperationsService.createDailyReport(projectId, dto, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }

    @PostMapping("/projects/{projectId}/daily-reports/{reportId}/approve")
    @PreAuthorize("hasAuthority('reports:daily_site') or hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<DailySiteReportDto> approveDailyReport(
        @PathVariable String projectId,
        @PathVariable String reportId,
        @Valid @RequestBody DsrApprovalDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        DailySiteReportDto report = siteOperationsService.approveDailyReport(projectId, reportId, dto, principal);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/projects/{projectId}/daily-reports")
    public ResponseEntity<List<DailySiteReportDto>> getDailyReports(
        @PathVariable String projectId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<DailySiteReportDto> list = siteOperationsService.getDailyReportsForProject(projectId, principal);
        return ResponseEntity.ok(list);
    }

    // Work Progress (Reported vs Verified Quantities)
    @PostMapping("/projects/{projectId}/work-progress")
    @PreAuthorize("hasAuthority('reports:daily_site') or hasAuthority('tasks:update') or hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<WorkProgressDto> recordWorkProgress(
        @PathVariable String projectId,
        @Valid @RequestBody WorkProgressCreateDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        WorkProgressDto wp = siteOperationsService.recordWorkProgress(projectId, dto, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(wp);
    }

    @PostMapping("/projects/{projectId}/work-progress/{progressId}/verify")
    @PreAuthorize("hasAuthority('tasks:manage') or hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('CIVIL_ENGINEER')")
    public ResponseEntity<WorkProgressDto> verifyWorkProgress(
        @PathVariable String projectId,
        @PathVariable String progressId,
        @Valid @RequestBody WorkProgressVerifyDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        WorkProgressDto wp = siteOperationsService.verifyWorkProgress(projectId, progressId, dto, principal);
        return ResponseEntity.ok(wp);
    }

    @GetMapping("/projects/{projectId}/work-progress")
    public ResponseEntity<List<WorkProgressDto>> getWorkProgress(
        @PathVariable String projectId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<WorkProgressDto> list = siteOperationsService.getWorkProgressForProject(projectId, principal);
        return ResponseEntity.ok(list);
    }

    // Photo Evidence
    @PostMapping("/projects/{projectId}/photos")
    public ResponseEntity<SitePhotoDto> saveSitePhoto(
        @PathVariable String projectId,
        @Valid @RequestBody SitePhotoCreateDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        SitePhotoDto photo = siteOperationsService.saveSitePhoto(projectId, dto, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(photo);
    }

    @GetMapping("/projects/{projectId}/photos")
    public ResponseEntity<List<SitePhotoDto>> getSitePhotos(
        @PathVariable String projectId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<SitePhotoDto> list = siteOperationsService.getPhotosForProject(projectId, principal);
        return ResponseEntity.ok(list);
    }

    // Issues
    @PostMapping("/projects/{projectId}/issues")
    public ResponseEntity<SiteIssueDto> logSiteIssue(
        @PathVariable String projectId,
        @Valid @RequestBody SiteIssueCreateDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        SiteIssueDto issue = siteOperationsService.logSiteIssue(projectId, dto, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(issue);
    }

    @GetMapping("/projects/{projectId}/issues")
    public ResponseEntity<List<SiteIssueDto>> getSiteIssues(
        @PathVariable String projectId,
        @RequestParam(required = false) IssueStatus status,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<SiteIssueDto> list = siteOperationsService.getSiteIssuesForProject(projectId, status, principal);
        return ResponseEntity.ok(list);
    }

    @PatchMapping("/issues/{issueId}/status")
    public ResponseEntity<SiteIssueDto> updateIssueStatus(
        @PathVariable String issueId,
        @Valid @RequestBody SiteIssueStatusUpdateDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        SiteIssueDto issue = siteOperationsService.updateIssueStatus(issueId, dto, principal);
        return ResponseEntity.ok(issue);
    }
}
