package com.example.contruction_management_app.backend.dto;

public record InvitationDto(
    String id,
    String organizationId,
    String organizationName,
    String email,
    String roleId,
    String roleName,
    String invitedByUserName,
    String status,
    String token,
    String expiresAt,
    String createdAt
) {}
