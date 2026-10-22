package com.example.contruction_management_app.backend.service;

import com.example.contruction_management_app.backend.domain.*;
import com.example.contruction_management_app.backend.dto.*;
import com.example.contruction_management_app.backend.exception.ForbiddenException;
import com.example.contruction_management_app.backend.exception.ResourceNotFoundException;
import com.example.contruction_management_app.backend.repository.*;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final AuditLogService auditLogService;

    public DocumentService(
        DocumentRepository documentRepository,
        DocumentVersionRepository documentVersionRepository,
        ProjectRepository projectRepository,
        ProjectMemberRepository projectMemberRepository,
        AuditLogService auditLogService
    ) {
        this.documentRepository = documentRepository;
        this.documentVersionRepository = documentVersionRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<DocumentDto> getDocumentsByProject(String projectId, DocumentType typeFilter, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);
        List<Document> documents;
        if (typeFilter != null) {
            documents = documentRepository.findByProjectIdAndDocumentTypeOrderByCreatedAtDesc(projectId, typeFilter);
        } else {
            documents = documentRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
        }
        return documents.stream().map(DocumentDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public DocumentDto getDocumentById(String documentId, UserPrincipal principal) {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found with ID: " + documentId));
        validateProjectAccess(document.getProject().getId(), principal);
        return DocumentDto.fromEntity(document);
    }

    @Transactional
    public DocumentDto createDocument(String projectId, DocumentCreateDto dto, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        String versionNumber = (dto.versionNumber() != null && !dto.versionNumber().isBlank())
            ? dto.versionNumber()
            : "v1.0";

        Document document = Document.builder()
            .id(UUID.randomUUID().toString())
            .project(project)
            .organization(project.getOrganization())
            .title(dto.title())
            .documentType(dto.documentType())
            .category(dto.category() != null ? dto.category() : "General")
            .latestVersion(versionNumber)
            .status(DocumentStatus.APPROVED)
            .createdBy(principal.getUsername())
            .versions(new ArrayList<>())
            .build();

        DocumentVersion version = DocumentVersion.builder()
            .id(UUID.randomUUID().toString())
            .document(document)
            .versionNumber(versionNumber)
            .fileName(dto.fileName())
            .fileSizeBytes(dto.fileSizeBytes() != null ? dto.fileSizeBytes() : 0L)
            .mimeType(dto.mimeType() != null ? dto.mimeType() : "application/pdf")
            .fileUrl(dto.fileUrl())
            .changeNotes(dto.changeNotes() != null ? dto.changeNotes() : "Initial revision upload")
            .uploadedBy(principal.getUsername())
            .build();

        document.getVersions().add(version);
        Document saved = documentRepository.save(document);

        auditLogService.recordLog(
            project.getOrganization().getId(),
            projectId,
            principal.getId() != null ? principal.getId() : principal.getUsername(),
            principal.getUsername(),
            AuditAction.CREATE,
            AuditEntityType.DOCUMENT,
            saved.getId(),
            "Created document '" + saved.getTitle() + "' version " + versionNumber,
            null
        );

        return DocumentDto.fromEntity(saved);
    }

    @Transactional
    public DocumentVersionDto addDocumentVersion(String documentId, DocumentVersionCreateDto dto, UserPrincipal principal) {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found with ID: " + documentId));
        validateProjectAccess(document.getProject().getId(), principal);

        DocumentVersion version = DocumentVersion.builder()
            .id(UUID.randomUUID().toString())
            .document(document)
            .versionNumber(dto.versionNumber())
            .fileName(dto.fileName())
            .fileSizeBytes(dto.fileSizeBytes() != null ? dto.fileSizeBytes() : 0L)
            .mimeType(dto.mimeType() != null ? dto.mimeType() : "application/pdf")
            .fileUrl(dto.fileUrl())
            .changeNotes(dto.changeNotes())
            .uploadedBy(principal.getUsername())
            .build();

        DocumentVersion savedVersion = documentVersionRepository.save(version);

        document.setLatestVersion(dto.versionNumber());
        documentRepository.save(document);

        auditLogService.recordLog(
            document.getOrganization().getId(),
            document.getProject().getId(),
            principal.getId() != null ? principal.getId() : principal.getUsername(),
            principal.getUsername(),
            AuditAction.UPDATE,
            AuditEntityType.DOCUMENT,
            document.getId(),
            "Uploaded revision " + dto.versionNumber() + " for '" + document.getTitle() + "'",
            null
        );

        return DocumentVersionDto.fromEntity(savedVersion);
    }

    @Transactional(readOnly = true)
    public List<DocumentVersionDto> getDocumentVersions(String documentId, UserPrincipal principal) {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found with ID: " + documentId));
        validateProjectAccess(document.getProject().getId(), principal);

        return documentVersionRepository.findByDocumentIdOrderByUploadedAtDesc(documentId).stream()
            .map(DocumentVersionDto::fromEntity)
            .toList();
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
