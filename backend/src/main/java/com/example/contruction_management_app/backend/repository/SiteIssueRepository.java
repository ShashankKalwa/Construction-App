package com.example.contruction_management_app.backend.repository;

import com.example.contruction_management_app.backend.domain.IssueStatus;
import com.example.contruction_management_app.backend.domain.SiteIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SiteIssueRepository extends JpaRepository<SiteIssue, String> {
    List<SiteIssue> findByProjectIdOrderByCreatedAtDesc(String projectId);
    List<SiteIssue> findByProjectIdAndStatusOrderByCreatedAtDesc(String projectId, IssueStatus status);
}
