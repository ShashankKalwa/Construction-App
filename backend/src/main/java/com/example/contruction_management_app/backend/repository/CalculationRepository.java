package com.example.contruction_management_app.backend.repository;

import com.example.contruction_management_app.backend.domain.Calculation;
import com.example.contruction_management_app.backend.domain.CalculatorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalculationRepository extends JpaRepository<Calculation, String> {
    List<Calculation> findByProjectIdOrderByCreatedAtDesc(String projectId);
    List<Calculation> findByProjectIdAndCalculatorTypeOrderByCreatedAtDesc(String projectId, CalculatorType calculatorType);
    long countByProjectId(String projectId);
}
