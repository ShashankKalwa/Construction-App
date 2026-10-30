package com.example.contruction_management_app.backend.repository;

import com.example.contruction_management_app.backend.domain.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MilestoneRepository extends JpaRepository<Milestone, String> {
    List<Milestone> findByProjectIdOrderByDueDateAsc(String projectId);
    List<Milestone> findByCompanyId(String companyId);
    long countByProjectIdAndStatus(String projectId, String status);
}
