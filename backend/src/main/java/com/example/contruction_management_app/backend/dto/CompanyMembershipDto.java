package com.example.contruction_management_app.backend.dto;

public record CompanyMembershipDto(
    String id,
    String organizationId,
    String organizationName,
    String roleId,
    String roleName,
    String status,
    String joinedAt
) {}
