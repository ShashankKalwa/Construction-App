package com.example.contruction_management_app.backend.repository;

import com.example.contruction_management_app.backend.domain.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialRepository extends JpaRepository<Material, String> {
    List<Material> findByProjectId(String projectId);
    List<Material> findByProjectIdOrderByNameAsc(String projectId);
    Optional<Material> findByProjectIdAndCode(String projectId, String code);

    @Query("SELECT m FROM Material m WHERE m.project.id = :projectId AND m.currentStock <= m.minStockThreshold ORDER BY m.name ASC")
    List<Material> findLowStockMaterials(@Param("projectId") String projectId);
}
