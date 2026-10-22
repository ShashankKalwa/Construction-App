package com.example.contruction_management_app.backend.repository;

import com.example.contruction_management_app.backend.domain.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, String> {
    List<Vendor> findByOrganizationIdOrderByNameAsc(String organizationId);
}
