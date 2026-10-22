package com.example.contruction_management_app.backend.repository;

import com.example.contruction_management_app.backend.domain.Phase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhaseRepository extends JpaRepository<Phase, String> {
    List<Phase> findByProjectIdOrderByOrderIndexAsc(String projectId);
}
