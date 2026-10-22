package com.example.contruction_management_app.backend.repository;

import com.example.contruction_management_app.backend.domain.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, String> {
    boolean existsByProjectIdAndUserId(String projectId, String userId);
    Optional<ProjectMember> findByProjectIdAndUserId(String projectId, String userId);
    List<ProjectMember> findByProjectId(String projectId);
}
