package com.example.contruction_management_app.backend.api.v1;

import com.example.contruction_management_app.backend.dto.MilestoneCreateDto;
import com.example.contruction_management_app.backend.dto.MilestoneDto;
import com.example.contruction_management_app.backend.dto.MilestoneStatusUpdateDto;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import com.example.contruction_management_app.backend.service.MilestoneService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class MilestoneController {

    private final MilestoneService milestoneService;

    public MilestoneController(MilestoneService milestoneService) {
        this.milestoneService = milestoneService;
    }

    @GetMapping("/projects/{projectId}/milestones")
    public ResponseEntity<List<MilestoneDto>> getMilestones(
        @PathVariable String projectId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<MilestoneDto> milestones = milestoneService.getMilestones(projectId, principal);
        return ResponseEntity.ok(milestones);
    }

    @PostMapping("/projects/{projectId}/milestones")
    @PreAuthorize("@projectAccess.can(#projectId, 'projects:edit') or hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<MilestoneDto> createMilestone(
        @PathVariable String projectId,
        @Valid @RequestBody MilestoneCreateDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        MilestoneDto milestone = milestoneService.createMilestone(projectId, dto, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(milestone);
    }

    @PatchMapping("/milestones/{milestoneId}/status")
    @PreAuthorize("hasAuthority('projects:edit') or hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<MilestoneDto> updateMilestoneStatus(
        @PathVariable String milestoneId,
        @Valid @RequestBody MilestoneStatusUpdateDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        MilestoneDto milestone = milestoneService.updateMilestoneStatus(milestoneId, dto, principal);
        return ResponseEntity.ok(milestone);
    }
}
