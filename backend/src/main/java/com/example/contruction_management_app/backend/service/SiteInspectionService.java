package com.example.contruction_management_app.backend.service;

import com.example.contruction_management_app.backend.domain.*;
import com.example.contruction_management_app.backend.dto.*;
import com.example.contruction_management_app.backend.exception.ForbiddenException;
import com.example.contruction_management_app.backend.exception.ResourceNotFoundException;
import com.example.contruction_management_app.backend.repository.*;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class SiteInspectionService {

    private final SiteInspectionRepository inspectionRepository;
    private final SiteDefectRepository defectRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public SiteInspectionService(
        SiteInspectionRepository inspectionRepository,
        SiteDefectRepository defectRepository,
        ProjectRepository projectRepository,
        ProjectMemberRepository projectMemberRepository
    ) {
        this.inspectionRepository = inspectionRepository;
        this.defectRepository = defectRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Transactional
    public SiteInspectionDto createInspection(String projectId, SiteInspectionCreateDto dto, UserPrincipal principal) {
        Project project = validateProjectAccess(projectId, principal);

        String inspectionId = "insp_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        LocalDate date = dto.inspectionDate() != null ? dto.inspectionDate() : LocalDate.now();
        InspectionStatus status = dto.status() != null ? dto.status() : InspectionStatus.PASSED;
        String checklist = dto.checklistData() != null ? dto.checklistData() : "[]";

        SiteInspection inspection = SiteInspection.builder()
            .id(inspectionId)
            .project(project)
            .companyId(project.getOrganization().getId())
            .title(dto.title())
            .inspectionType(dto.inspectionType())
            .status(status)
            .checklistData(checklist)
            .inspectorId(principal.getId())
            .inspectionDate(date)
            .location(dto.location())
            .notes(dto.notes())
            .createdAt(Instant.now())
            .build();

        SiteInspection saved = inspectionRepository.save(inspection);

        // If inspection failed and notes/title indicate a specific issue, create an initial defect
        if (status == InspectionStatus.FAILED) {
            String defId = "def_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            SiteDefect defect = SiteDefect.builder()
                .id(defId)
                .project(project)
                .companyId(project.getOrganization().getId())
                .inspection(saved)
                .title("Defect: " + dto.title())
                .description(dto.notes() != null ? dto.notes() : "Failed checklist inspection")
                .location(dto.location())
                .severity(IssueSeverity.HIGH)
                .status(DefectStatus.OPEN)
                .createdBy(principal.getId())
                .createdAt(Instant.now())
                .deadline(date.plusDays(3))
                .build();
            defectRepository.save(defect);
        }

        return SiteInspectionDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<SiteInspectionDto> getInspectionsForProject(String projectId, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);
        return inspectionRepository.findByProjectIdOrderByInspectionDateDesc(projectId).stream()
            .map(SiteInspectionDto::fromEntity)
            .toList();
    }

    // Snag & Defect Management with Re-inspection
    @Transactional
    public SiteDefectDto logDefect(String projectId, SiteDefectCreateDto dto, UserPrincipal principal) {
        Project project = validateProjectAccess(projectId, principal);

        SiteInspection inspection = null;
        if (dto.inspectionId() != null && !dto.inspectionId().isBlank()) {
            inspection = inspectionRepository.findById(dto.inspectionId()).orElse(null);
        }

        String defId = "def_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        IssueSeverity severity = dto.severity() != null ? dto.severity() : IssueSeverity.MEDIUM;

        SiteDefect defect = SiteDefect.builder()
            .id(defId)
            .project(project)
            .companyId(project.getOrganization().getId())
            .inspection(inspection)
            .title(dto.title())
            .description(dto.description())
            .location(dto.location())
            .severity(severity)
            .status(DefectStatus.OPEN)
            .assignedTo(dto.assignedTo())
            .deadline(dto.deadline())
            .createdBy(principal.getId())
            .createdAt(Instant.now())
            .build();

        SiteDefect saved = defectRepository.save(defect);
        return SiteDefectDto.fromEntity(saved);
    }

    @Transactional
    public SiteDefectDto updateDefectStatus(String defectId, SiteDefectStatusUpdateDto dto, UserPrincipal principal) {
        SiteDefect defect = defectRepository.findById(defectId)
            .orElseThrow(() -> new ResourceNotFoundException("Defect not found: " + defectId));

        validateProjectAccess(defect.getProject().getId(), principal);

        defect.setStatus(dto.status());
        if (dto.status() == DefectStatus.RESOLVED) {
            defect.setResolvedAt(Instant.now());
        }

        SiteDefect saved = defectRepository.save(defect);
        return SiteDefectDto.fromEntity(saved);
    }

    @Transactional
    public SiteDefectDto reinspectDefect(String defectId, SiteDefectReinspectDto dto, UserPrincipal principal) {
        SiteDefect defect = defectRepository.findById(defectId)
            .orElseThrow(() -> new ResourceNotFoundException("Defect not found: " + defectId));

        validateProjectAccess(defect.getProject().getId(), principal);

        defect.setReInspectedBy(principal.getId());
        defect.setReInspectedAt(Instant.now());
        defect.setReInspectionNotes(dto.notes());

        if ("CLOSE".equalsIgnoreCase(dto.action()) || "APPROVE".equalsIgnoreCase(dto.action()) || "PASS".equalsIgnoreCase(dto.action())) {
            defect.setStatus(DefectStatus.CLOSED);
        } else {
            // Defect persists upon re-inspection failure: revert to OPEN for re-work
            defect.setStatus(DefectStatus.OPEN);
        }

        SiteDefect saved = defectRepository.save(defect);
        return SiteDefectDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<SiteDefectDto> getDefectsForProject(String projectId, DefectStatus status, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);
        List<SiteDefect> list;
        if (status != null) {
            list = defectRepository.findByProjectIdAndStatusOrderByCreatedAtDesc(projectId, status);
        } else {
            list = defectRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
        }
        return list.stream().map(SiteDefectDto::fromEntity).toList();
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
