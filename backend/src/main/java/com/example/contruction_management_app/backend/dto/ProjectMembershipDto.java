package com.example.contruction_management_app.backend.dto;

public record ProjectMembershipDto(
    String id,
    String projectId,
    String projectName,
    String projectCode,
    String assignedRole,
    String status,
    String joinedAt
) {}
