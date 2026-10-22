package com.example.contruction_management_app.backend.repository;

import com.example.contruction_management_app.backend.domain.LaborAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LaborAttendanceRepository extends JpaRepository<LaborAttendance, String> {
    List<LaborAttendance> findByReportId(String reportId);
}
