package com.example.contruction_management_app.backend.repository;

import com.example.contruction_management_app.backend.domain.UnitRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UnitRateRepository extends JpaRepository<UnitRate, String> {
    List<UnitRate> findByOrganizationId(String organizationId);
    Optional<UnitRate> findByOrganizationIdAndItemCode(String organizationId, String itemCode);
}
