package com.example.contruction_management_app.backend.repository;

import com.example.contruction_management_app.backend.domain.AuditEntityType;
import com.example.contruction_management_app.backend.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {
    List<AuditLog> findByProjectIdOrderByCreatedAtDesc(String projectId);
    List<AuditLog> findByProjectIdAndEntityTypeOrderByCreatedAtDesc(String projectId, AuditEntityType entityType);
}
