package com.example.contruction_management_app.backend.api.v1;

import com.example.contruction_management_app.backend.dto.AcceptInvitationDto;
import com.example.contruction_management_app.backend.dto.CompanyMembershipDto;
import com.example.contruction_management_app.backend.dto.CreateInvitationDto;
import com.example.contruction_management_app.backend.dto.InvitationDto;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import com.example.contruction_management_app.backend.service.InvitationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invitations")
public class InvitationController {

    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping
    @PreAuthorize("@projectAccess.isCompanyAdmin()")
    public ResponseEntity<InvitationDto> createInvitation(
        @Valid @RequestBody CreateInvitationDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        InvitationDto created = invitationService.createInvitation(dto, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @PreAuthorize("@projectAccess.isCompanyAdmin()")
    public ResponseEntity<List<InvitationDto>> listInvitations(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<InvitationDto> invitations = invitationService.listInvitations(principal);
        return ResponseEntity.ok(invitations);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@projectAccess.isCompanyAdmin()")
    public ResponseEntity<Void> revokeInvitation(
        @PathVariable String id,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        invitationService.revokeInvitation(id, principal);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/accept")
    public ResponseEntity<CompanyMembershipDto> acceptInvitation(
        @Valid @RequestBody AcceptInvitationDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        CompanyMembershipDto membership = invitationService.acceptInvitation(dto, principal);
        return ResponseEntity.ok(membership);
    }
}
