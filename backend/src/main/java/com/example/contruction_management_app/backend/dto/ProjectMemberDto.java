package com.example.contruction_management_app.backend.dto;

public record ProjectMemberDto(
    String id,
    UserSummaryDto user,
    String assignedRole,
    String joinedAt
) {}
