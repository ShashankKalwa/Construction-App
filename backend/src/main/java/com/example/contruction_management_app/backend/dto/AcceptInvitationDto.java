package com.example.contruction_management_app.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record AcceptInvitationDto(
    @NotBlank(message = "Token is required") String token
) {}
