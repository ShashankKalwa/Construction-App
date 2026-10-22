package com.example.contruction_management_app.backend.repository;

import com.example.contruction_management_app.backend.domain.Document;
import com.example.contruction_management_app.backend.domain.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, String> {
    List<Document> findByProjectIdOrderByCreatedAtDesc(String projectId);
    List<Document> findByProjectIdAndDocumentTypeOrderByCreatedAtDesc(String projectId, DocumentType documentType);
}
