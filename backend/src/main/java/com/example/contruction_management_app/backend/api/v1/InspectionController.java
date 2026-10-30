package com.example.contruction_management_app.backend.api.v1;

import com.example.contruction_management_app.backend.domain.DefectStatus;
import com.example.contruction_management_app.backend.dto.*;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import com.example.contruction_management_app.backend.service.SiteInspectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class InspectionController {

    private final SiteInspectionService inspectionService;

    public InspectionController(SiteInspectionService inspectionService) {
        this.inspectionService = inspectionService;
    }

    // Inspections
    @PostMapping("/projects/{projectId}/inspections")
    @PreAuthorize("hasAuthority('reports:daily_site') or hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN') or hasRole('CIVIL_ENGINEER') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<SiteInspectionDto> createInspection(
        @PathVariable String projectId,
        @Valid @RequestBody SiteInspectionCreateDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        SiteInspectionDto created = inspectionService.createInspection(projectId, dto, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/projects/{projectId}/inspections")
    public ResponseEntity<List<SiteInspectionDto>> getInspections(
        @PathVariable String projectId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<SiteInspectionDto> list = inspectionService.getInspectionsForProject(projectId, principal);
        return ResponseEntity.ok(list);
    }

    // Defects & Snags
    @PostMapping("/projects/{projectId}/defects")
    public ResponseEntity<SiteDefectDto> logDefect(
        @PathVariable String projectId,
        @Valid @RequestBody SiteDefectCreateDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        SiteDefectDto defect = inspectionService.logDefect(projectId, dto, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(defect);
    }

    @GetMapping("/projects/{projectId}/defects")
    public ResponseEntity<List<SiteDefectDto>> getDefects(
        @PathVariable String projectId,
        @RequestParam(required = false) DefectStatus status,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<SiteDefectDto> list = inspectionService.getDefectsForProject(projectId, status, principal);
        return ResponseEntity.ok(list);
    }

    @PatchMapping("/defects/{defectId}/status")
    public ResponseEntity<SiteDefectDto> updateDefectStatus(
        @PathVariable String defectId,
        @Valid @RequestBody SiteDefectStatusUpdateDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        SiteDefectDto defect = inspectionService.updateDefectStatus(defectId, dto, principal);
        return ResponseEntity.ok(defect);
    }

    @PostMapping("/defects/{defectId}/reinspect")
    @PreAuthorize("hasAuthority('reports:daily_site') or hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN') or hasRole('CIVIL_ENGINEER') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<SiteDefectDto> reinspectDefect(
        @PathVariable String defectId,
        @Valid @RequestBody SiteDefectReinspectDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        SiteDefectDto defect = inspectionService.reinspectDefect(defectId, dto, principal);
        return ResponseEntity.ok(defect);
    }
}
