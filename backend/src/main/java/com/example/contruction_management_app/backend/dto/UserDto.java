package com.example.contruction_management_app.backend.dto;

public record UserDto(
    String id,
    String email,
    String fullName,
    String avatarUrl,
    String organizationId,
    String organizationName,
    RoleDto role
) {}
