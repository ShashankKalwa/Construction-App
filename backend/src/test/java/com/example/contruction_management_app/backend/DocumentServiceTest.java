package com.example.contruction_management_app.backend;

import com.example.contruction_management_app.backend.domain.*;
import com.example.contruction_management_app.backend.dto.*;
import com.example.contruction_management_app.backend.exception.ForbiddenException;
import com.example.contruction_management_app.backend.repository.*;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import com.example.contruction_management_app.backend.service.AuditLogService;
import com.example.contruction_management_app.backend.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class DocumentServiceTest {

    private DocumentRepository documentRepository;
    private DocumentVersionRepository documentVersionRepository;
    private ProjectRepository projectRepository;
    private ProjectMemberRepository projectMemberRepository;
    private AuditLogService auditLogService;
    private DocumentService documentService;

    private Project mockProject;
    private Organization mockOrg;
    private UserPrincipal authorizedPrincipal;
    private UserPrincipal unauthorizedPrincipal;

    @BeforeEach
    void setUp() {
        documentRepository = Mockito.mock(DocumentRepository.class);
        documentVersionRepository = Mockito.mock(DocumentVersionRepository.class);
        projectRepository = Mockito.mock(ProjectRepository.class);
        projectMemberRepository = Mockito.mock(ProjectMemberRepository.class);
        auditLogService = Mockito.mock(AuditLogService.class);

        documentService = new DocumentService(
            documentRepository,
            documentVersionRepository,
            projectRepository,
            projectMemberRepository,
            auditLogService
        );

        mockOrg = new Organization("Apex Infrastructure", "apex-infra");
        mockOrg.setId("org_apex_infra");

        mockProject = new Project("Commercial Tower B", "CTB-01", "Mumbai", new BigDecimal("100000000"), mockOrg);
        mockProject.setId("prj_commercial_tower_b");

        authorizedPrincipal = new UserPrincipal(
            "usr_pm_rahul", "rahul@apex.com", "hash",
            "org_apex_infra", "pm", List.of(), true
        );

        unauthorizedPrincipal = new UserPrincipal(
            "usr_outsider", "outsider@other.com", "hash",
            "org_other", "contractor", List.of(), true
        );

        when(projectMemberRepository.existsByProjectIdAndUserId("prj_commercial_tower_b", "usr_pm_rahul")).thenReturn(true);
        when(projectMemberRepository.existsByProjectIdAndUserId("prj_commercial_tower_b", "usr_outsider")).thenReturn(false);
        when(projectRepository.findById("prj_commercial_tower_b")).thenReturn(Optional.of(mockProject));
    }

    @Test
    void testCreateDocumentWithInitialVersion() {
        DocumentCreateDto createDto = new DocumentCreateDto(
            "Architectural General Arrangement",
            DocumentType.DRAWING_ARCHITECTURAL,
            "Drawings",
            "v1.0",
            "tower_b_ga_v1.0.pdf",
            12450000L,
            "application/pdf",
            "https://storage.cms.com/docs/tower_b_ga.pdf",
            "Initial schematic submission"
        );

        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
            Document doc = invocation.getArgument(0);
            return doc;
        });

        DocumentDto result = documentService.createDocument("prj_commercial_tower_b", createDto, authorizedPrincipal);

        assertNotNull(result);
        assertEquals("Architectural General Arrangement", result.title());
        assertEquals("v1.0", result.latestVersion());
        assertEquals(DocumentType.DRAWING_ARCHITECTURAL, result.documentType());
        assertEquals(DocumentStatus.APPROVED, result.status());
    }

    @Test
    void testAddDocumentVersionUpdatesLatestVersion() {
        Document existingDoc = Document.builder()
            .id("doc_arch_001")
            .project(mockProject)
            .organization(mockOrg)
            .title("Architectural General Arrangement")
            .documentType(DocumentType.DRAWING_ARCHITECTURAL)
            .latestVersion("v1.0")
            .status(DocumentStatus.APPROVED)
            .createdBy("usr_pm_rahul")
            .versions(new ArrayList<>())
            .build();

        when(documentRepository.findById("doc_arch_001")).thenReturn(Optional.of(existingDoc));
        when(documentVersionRepository.save(any(DocumentVersion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DocumentVersionCreateDto versionCreateDto = new DocumentVersionCreateDto(
            "v2.0",
            "tower_b_ga_v2.0.pdf",
            14200000L,
            "application/pdf",
            "https://storage.cms.com/docs/tower_b_ga_v2.pdf",
            "Incorporated structural MEP shaft clearances"
        );

        DocumentVersionDto versionResult = documentService.addDocumentVersion("doc_arch_001", versionCreateDto, authorizedPrincipal);

        assertNotNull(versionResult);
        assertEquals("v2.0", versionResult.versionNumber());
        assertEquals("doc_arch_001", existingDoc.getId());
        assertEquals("v2.0", existingDoc.getLatestVersion());
    }

    @Test
    void testUnauthorizedUserAccessDenied() {
        assertThrows(ForbiddenException.class, () -> {
            documentService.getDocumentsByProject("prj_commercial_tower_b", null, unauthorizedPrincipal);
        });
    }
}
