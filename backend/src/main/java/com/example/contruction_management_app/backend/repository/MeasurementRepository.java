package com.example.contruction_management_app.backend.repository;

import com.example.contruction_management_app.backend.domain.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeasurementRepository extends JpaRepository<Measurement, String> {
    List<Measurement> findByProjectIdOrderByMeasuredAtDesc(String projectId);
    List<Measurement> findByBoqItemIdOrderByMeasuredAtDesc(String boqItemId);
}
