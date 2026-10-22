package com.example.contruction_management_app.backend.service;

import com.example.contruction_management_app.backend.domain.*;
import com.example.contruction_management_app.backend.dto.*;
import com.example.contruction_management_app.backend.exception.ForbiddenException;
import com.example.contruction_management_app.backend.repository.*;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ProjectRepository projectRepository;
    private final OrganizationRepository organizationRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public AuditLogService(
        AuditLogRepository auditLogRepository,
        ProjectRepository projectRepository,
        OrganizationRepository organizationRepository,
        ProjectMemberRepository projectMemberRepository
    ) {
        this.auditLogRepository = auditLogRepository;
        this.projectRepository = projectRepository;
        this.organizationRepository = organizationRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Transactional(readOnly = true)
    public List<AuditLogDto> getAuditLogsByProject(String projectId, AuditEntityType typeFilter, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);
        List<AuditLog> logs;
        if (typeFilter != null) {
            logs = auditLogRepository.findByProjectIdAndEntityTypeOrderByCreatedAtDesc(projectId, typeFilter);
        } else {
            logs = auditLogRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
        }
        return logs.stream().map(AuditLogDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public AuditSummaryDto getAuditSummary(String projectId, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);
        List<AuditLog> logs = auditLogRepository.findByProjectIdOrderByCreatedAtDesc(projectId);

        long totalEvents = logs.size();
        long approvalsCount = logs.stream()
            .filter(l -> l.getAction() == AuditAction.APPROVE)
            .count();
        long stockIssuesCount = logs.stream()
            .filter(l -> l.getAction() == AuditAction.ISSUE_STOCK || l.getEntityType() == AuditEntityType.STOCK)
            .count();
        long documentsCount = logs.stream()
            .filter(l -> l.getEntityType() == AuditEntityType.DOCUMENT)
            .count();
        long paymentsCount = logs.stream()
            .filter(l -> l.getAction() == AuditAction.PAY_BILL || l.getEntityType() == AuditEntityType.CONTRACTOR_BILL)
            .count();

        return new AuditSummaryDto(totalEvents, approvalsCount, stockIssuesCount, documentsCount, paymentsCount);
    }

    @Transactional
    public void recordLog(
        String organizationId,
        String projectId,
        String userId,
        String userName,
        AuditAction action,
        AuditEntityType entityType,
        String entityId,
        String details,
        String ipAddress
    ) {
        Organization organization = organizationRepository.findById(organizationId).orElse(null);
        Project project = (projectId != null) ? projectRepository.findById(projectId).orElse(null) : null;

        AuditLog log = AuditLog.builder()
            .id(UUID.randomUUID().toString())
            .organization(organization)
            .project(project)
            .userId(userId)
            .userName(userName != null ? userName : "System")
            .action(action)
            .entityType(entityType)
            .entityId(entityId)
            .details(details)
            .ipAddress(ipAddress != null ? ipAddress : "127.0.0.1")
            .createdAt(Instant.now())
            .build();

        auditLogRepository.save(log);
    }

    private void validateProjectAccess(String projectId, UserPrincipal principal) {
        if (principal == null) return;
        boolean isSuperAdmin = principal.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN") || a.getAuthority().equals("ROLE_COMPANY_ADMIN"));
        if (!isSuperAdmin && !projectMemberRepository.existsByProjectIdAndUserId(projectId, principal.getId())) {
            throw new ForbiddenException("You do not have access to project: " + projectId);
        }
    }
}
