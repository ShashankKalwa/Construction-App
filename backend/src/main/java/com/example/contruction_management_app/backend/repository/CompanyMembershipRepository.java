package com.example.contruction_management_app.backend.repository;

import com.example.contruction_management_app.backend.domain.CompanyMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyMembershipRepository extends JpaRepository<CompanyMembership, String> {
    Optional<CompanyMembership> findByOrganizationIdAndUserId(String organizationId, String userId);
    List<CompanyMembership> findByUserId(String userId);
    List<CompanyMembership> findByUserIdAndStatus(String userId, String status);
    List<CompanyMembership> findByOrganizationId(String organizationId);
    boolean existsByOrganizationIdAndUserIdAndStatus(String organizationId, String userId, String status);
}
