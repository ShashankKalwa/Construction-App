package com.example.contruction_management_app.backend.api.v1;

import com.example.contruction_management_app.backend.domain.AuditEntityType;
import com.example.contruction_management_app.backend.dto.AuditLogDto;
import com.example.contruction_management_app.backend.dto.AuditSummaryDto;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import com.example.contruction_management_app.backend.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/projects/{projectId}/audit-logs")
    public ResponseEntity<List<AuditLogDto>> getAuditLogs(
        @PathVariable String projectId,
        @RequestParam(required = false) AuditEntityType type,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(auditLogService.getAuditLogsByProject(projectId, type, principal));
    }

    @GetMapping("/projects/{projectId}/audit-report/summary")
    public ResponseEntity<AuditSummaryDto> getAuditSummary(
        @PathVariable String projectId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(auditLogService.getAuditSummary(projectId, principal));
    }
}
