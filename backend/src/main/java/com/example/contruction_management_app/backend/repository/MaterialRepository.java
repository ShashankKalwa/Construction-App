package com.example.contruction_management_app.backend.repository;

import com.example.contruction_management_app.backend.domain.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialRepository extends JpaRepository<Material, String> {
    List<Material> findByProjectIdOrderByNameAsc(String projectId);
    Optional<Material> findByProjectIdAndCode(String projectId, String code);
}
