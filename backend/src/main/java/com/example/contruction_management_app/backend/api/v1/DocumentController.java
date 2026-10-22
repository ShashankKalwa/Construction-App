package com.example.contruction_management_app.backend.api.v1;

import com.example.contruction_management_app.backend.domain.DocumentType;
import com.example.contruction_management_app.backend.dto.*;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import com.example.contruction_management_app.backend.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/projects/{projectId}/documents")
    public ResponseEntity<List<DocumentDto>> getDocuments(
        @PathVariable String projectId,
        @RequestParam(required = false) DocumentType type,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(documentService.getDocumentsByProject(projectId, type, principal));
    }

    @GetMapping("/documents/{documentId}")
    public ResponseEntity<DocumentDto> getDocument(
        @PathVariable String documentId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(documentService.getDocumentById(documentId, principal));
    }

    @PostMapping("/projects/{projectId}/documents")
    public ResponseEntity<DocumentDto> createDocument(
        @PathVariable String projectId,
        @Valid @RequestBody DocumentCreateDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(documentService.createDocument(projectId, dto, principal));
    }

    @PostMapping("/documents/{documentId}/versions")
    public ResponseEntity<DocumentVersionDto> addVersion(
        @PathVariable String documentId,
        @Valid @RequestBody DocumentVersionCreateDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(documentService.addDocumentVersion(documentId, dto, principal));
    }

    @GetMapping("/documents/{documentId}/versions")
    public ResponseEntity<List<DocumentVersionDto>> getVersions(
        @PathVariable String documentId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(documentService.getDocumentVersions(documentId, principal));
    }
}
