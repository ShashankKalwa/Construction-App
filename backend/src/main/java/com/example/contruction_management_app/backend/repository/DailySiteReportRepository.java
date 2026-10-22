package com.example.contruction_management_app.backend.repository;

import com.example.contruction_management_app.backend.domain.DailySiteReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailySiteReportRepository extends JpaRepository<DailySiteReport, String> {
    List<DailySiteReport> findByProjectIdOrderByReportDateDesc(String projectId);
    Optional<DailySiteReport> findByProjectIdAndReportDate(String projectId, LocalDate reportDate);
}
