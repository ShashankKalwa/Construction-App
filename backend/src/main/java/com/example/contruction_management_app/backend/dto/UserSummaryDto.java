package com.example.contruction_management_app.backend.dto;

public record UserSummaryDto(
    String id,
    String fullName,
    String email,
    String avatarUrl
) {}
