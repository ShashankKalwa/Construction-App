package com.example.contruction_management_app.backend.repository;

import com.example.contruction_management_app.backend.domain.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, String> {
    List<DocumentVersion> findByDocumentIdOrderByUploadedAtDesc(String documentId);
}
