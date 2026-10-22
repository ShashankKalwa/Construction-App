package com.example.contruction_management_app.backend.repository;

import com.example.contruction_management_app.backend.domain.Boq;
import com.example.contruction_management_app.backend.domain.BoqStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoqRepository extends JpaRepository<Boq, String> {
    List<Boq> findByProjectIdOrderByVersionDesc(String projectId);
    Optional<Boq> findFirstByProjectIdAndStatusOrderByVersionDesc(String projectId, BoqStatus status);
    Optional<Boq> findFirstByProjectIdOrderByVersionDesc(String projectId);
}
