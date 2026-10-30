package com.example.contruction_management_app.backend.repository;

import com.example.contruction_management_app.backend.domain.DefectStatus;
import com.example.contruction_management_app.backend.domain.SiteDefect;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SiteDefectRepository extends JpaRepository<SiteDefect, String> {
    List<SiteDefect> findByProjectIdOrderByCreatedAtDesc(String projectId);
    List<SiteDefect> findByProjectIdAndStatusOrderByCreatedAtDesc(String projectId, DefectStatus status);
}
