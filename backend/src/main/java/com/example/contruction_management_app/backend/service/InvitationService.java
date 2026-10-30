package com.example.contruction_management_app.backend.service;

import com.example.contruction_management_app.backend.domain.CompanyMembership;
import com.example.contruction_management_app.backend.domain.Invitation;
import com.example.contruction_management_app.backend.domain.Organization;
import com.example.contruction_management_app.backend.domain.Role;
import com.example.contruction_management_app.backend.domain.User;
import com.example.contruction_management_app.backend.dto.AcceptInvitationDto;
import com.example.contruction_management_app.backend.dto.CompanyMembershipDto;
import com.example.contruction_management_app.backend.dto.CreateInvitationDto;
import com.example.contruction_management_app.backend.dto.InvitationDto;
import com.example.contruction_management_app.backend.exception.BadRequestException;
import com.example.contruction_management_app.backend.exception.ForbiddenException;
import com.example.contruction_management_app.backend.exception.ResourceNotFoundException;
import com.example.contruction_management_app.backend.repository.CompanyMembershipRepository;
import com.example.contruction_management_app.backend.repository.InvitationRepository;
import com.example.contruction_management_app.backend.repository.OrganizationRepository;
import com.example.contruction_management_app.backend.repository.RoleRepository;
import com.example.contruction_management_app.backend.repository.UserRepository;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final CompanyMembershipRepository companyMembershipRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public InvitationService(
        InvitationRepository invitationRepository,
        CompanyMembershipRepository companyMembershipRepository,
        OrganizationRepository organizationRepository,
        UserRepository userRepository,
        RoleRepository roleRepository
    ) {
        this.invitationRepository = invitationRepository;
        this.companyMembershipRepository = companyMembershipRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public InvitationDto createInvitation(CreateInvitationDto dto, UserPrincipal principal) {
        validateCompanyAdmin(principal);

        Organization org = organizationRepository.findById(principal.getOrganizationId())
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        Role role = roleRepository.findById(dto.roleId().toLowerCase())
            .orElseThrow(() -> new BadRequestException("Role not found: " + dto.roleId()));

        User inviter = userRepository.findById(principal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Inviter user not found"));

        // Check if user is already an active member of this organization
        Optional<User> existingUser = userRepository.findByEmail(dto.email().trim().toLowerCase());
        if (existingUser.isPresent()) {
            boolean alreadyMember = companyMembershipRepository.existsByOrganizationIdAndUserIdAndStatus(
                org.getId(), existingUser.get().getId(), "ACTIVE"
            );
            if (alreadyMember) {
                throw new BadRequestException("User with email " + dto.email() + " is already an active member of " + org.getName());
            }
        }

        Instant expiresAt = Instant.now().plus(7, ChronoUnit.DAYS);
        Invitation invitation = new Invitation(org, dto.email().trim().toLowerCase(), role, inviter, expiresAt);
        Invitation saved = invitationRepository.save(invitation);

        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<InvitationDto> listInvitations(UserPrincipal principal) {
        validateCompanyAdmin(principal);
        return invitationRepository.findByOrganizationId(principal.getOrganizationId())
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public void revokeInvitation(String invitationId, UserPrincipal principal) {
        validateCompanyAdmin(principal);

        Invitation invitation = invitationRepository.findById(invitationId)
            .orElseThrow(() -> new ResourceNotFoundException("Invitation not found: " + invitationId));

        if (!invitation.getOrganization().getId().equals(principal.getOrganizationId())) {
            throw new ForbiddenException("Cannot revoke invitation from another organization");
        }

        invitation.setStatus("REVOKED");
        invitationRepository.save(invitation);
    }

    @Transactional
    public CompanyMembershipDto acceptInvitation(AcceptInvitationDto dto, UserPrincipal principal) {
        Invitation invitation = invitationRepository.findByToken(dto.token().trim())
            .orElseThrow(() -> new ResourceNotFoundException("Invalid or unrecognized invitation token"));

        if (!"PENDING".equalsIgnoreCase(invitation.getStatus())) {
            throw new BadRequestException("Invitation is no longer pending (Status: " + invitation.getStatus() + ")");
        }

        if (Instant.now().isAfter(invitation.getExpiresAt())) {
            invitation.setStatus("EXPIRED");
            invitationRepository.save(invitation);
            throw new BadRequestException("Invitation token has expired");
        }

        User user = userRepository.findById(principal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<CompanyMembership> existingMembership = companyMembershipRepository.findByOrganizationIdAndUserId(
            invitation.getOrganization().getId(), user.getId()
        );

        CompanyMembership membership;
        if (existingMembership.isPresent()) {
            membership = existingMembership.get();
            membership.setRole(invitation.getRole());
            membership.setStatus("ACTIVE");
        } else {
            membership = new CompanyMembership(
                invitation.getOrganization(),
                user,
                invitation.getRole(),
                "ACTIVE"
            );
        }

        CompanyMembership savedMembership = companyMembershipRepository.save(membership);

        invitation.setStatus("ACCEPTED");
        invitationRepository.save(invitation);

        return new CompanyMembershipDto(
            savedMembership.getId(),
            savedMembership.getOrganization().getId(),
            savedMembership.getOrganization().getName(),
            savedMembership.getRole().getId(),
            savedMembership.getRole().getName(),
            savedMembership.getStatus(),
            savedMembership.getJoinedAt().toString()
        );
    }

    private void validateCompanyAdmin(UserPrincipal principal) {
        if (!"super_admin".equalsIgnoreCase(principal.getRoleId()) && !"company_admin".equalsIgnoreCase(principal.getRoleId())) {
            throw new ForbiddenException("Only Company Admins can manage invitations");
        }
    }

    private InvitationDto toDto(Invitation inv) {
        return new InvitationDto(
            inv.getId(),
            inv.getOrganization().getId(),
            inv.getOrganization().getName(),
            inv.getEmail(),
            inv.getRole().getId(),
            inv.getRole().getName(),
            inv.getInvitedBy().getFullName(),
            inv.getStatus(),
            inv.getToken(),
            inv.getExpiresAt().toString(),
            inv.getCreatedAt().toString()
        );
    }
}
