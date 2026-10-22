package com.example.contruction_management_app.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record ProjectMemberAssignDto(
    @NotBlank(message = "User ID is required")
    String userId,

    @NotBlank(message = "Assigned role is required")
    String assignedRole
) {}
