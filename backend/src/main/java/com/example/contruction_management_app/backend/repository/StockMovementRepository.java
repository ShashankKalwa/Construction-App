package com.example.contruction_management_app.backend.repository;

import com.example.contruction_management_app.backend.domain.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, String> {
    List<StockMovement> findByMaterialIdOrderByCreatedAtDesc(String materialId);
    List<StockMovement> findByMaterialProjectIdOrderByCreatedAtDesc(String projectId);
}
