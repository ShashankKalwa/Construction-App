package com.example.contruction_management_app.backend.repository;

import com.example.contruction_management_app.backend.domain.MaterialRequest;
import com.example.contruction_management_app.backend.domain.MaterialRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialRequestRepository extends JpaRepository<MaterialRequest, String> {
    List<MaterialRequest> findByProjectIdOrderByCreatedAtDesc(String projectId);
    List<MaterialRequest> findByProjectIdAndStatusOrderByCreatedAtDesc(String projectId, MaterialRequestStatus status);
}
