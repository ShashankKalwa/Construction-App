package com.example.contruction_management_app.backend.repository;

import com.example.contruction_management_app.backend.domain.SiteInspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SiteInspectionRepository extends JpaRepository<SiteInspection, String> {
    List<SiteInspection> findByProjectIdOrderByInspectionDateDesc(String projectId);
}
