package com.example.contruction_management_app.backend.repository;

import com.example.contruction_management_app.backend.domain.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, String> {
    Optional<Invitation> findByToken(String token);
    Optional<Invitation> findByOrganizationIdAndEmailAndStatus(String organizationId, String email, String status);
    List<Invitation> findByOrganizationId(String organizationId);
    List<Invitation> findByOrganizationIdAndStatus(String organizationId, String status);
    List<Invitation> findByEmail(String email);
}
