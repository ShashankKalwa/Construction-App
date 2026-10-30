package com.example.contruction_management_app.backend.repository;

import com.example.contruction_management_app.backend.domain.WorkProgressRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkProgressRepository extends JpaRepository<WorkProgressRecord, String> {
    List<WorkProgressRecord> findByProjectIdOrderByRecordedAtDesc(String projectId);
    List<WorkProgressRecord> findByReportId(String reportId);
}
