package com.example.contruction_management_app.backend.dto;

import java.util.List;

public record UserMeDto(
    String id,
    String email,
    String fullName,
    String avatarUrl,
    String activeCompanyId,
    String activeCompanyName,
    RoleDto primaryRole,
    List<CompanyMembershipDto> companyMemberships,
    List<ProjectMembershipDto> projectMemberships,
    List<String> effectivePermissions
) {}
