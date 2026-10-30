package com.example.contruction_management_app.backend.dto;

public record ProjectMemberDto(
    String id,
    UserSummaryDto user,
    String assignedRole,
    String joinedAt,
    String status
) {
    public ProjectMemberDto(String id, UserSummaryDto user, String assignedRole, String joinedAt) {
        this(id, user, assignedRole, joinedAt, "ACTIVE");
    }
}
