package com.example.contruction_management_app.backend.repository;

import com.example.contruction_management_app.backend.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {
}
