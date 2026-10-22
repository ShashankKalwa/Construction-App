package com.example.contruction_management_app.backend.repository;

import com.example.contruction_management_app.backend.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {
    List<Task> findByProjectId(String projectId);
    List<Task> findByPhaseId(String phaseId);
    List<Task> findByAssigneeId(String assigneeId);
    long countByProjectId(String projectId);
    long countByProjectIdAndStatus(String projectId, String status);
}
