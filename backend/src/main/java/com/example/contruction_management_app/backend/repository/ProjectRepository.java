package com.example.contruction_management_app.backend.repository;

import com.example.contruction_management_app.backend.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {
    List<Project> findAllByOrganizationId(String organizationId);

    @Query("""
        SELECT p FROM Project p 
        JOIN ProjectMember pm ON p.id = pm.project.id 
        WHERE p.organization.id = :orgId AND pm.user.id = :userId
    """)
    List<Project> findAllByOrganizationAndUser(
        @Param("orgId") String orgId,
        @Param("userId") String userId
    );
}
