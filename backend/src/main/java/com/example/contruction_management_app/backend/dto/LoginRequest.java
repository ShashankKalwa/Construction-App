package com.example.contruction_management_app.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "Username or email is required")
    String email,

    @NotBlank(message = "Password is required")
    String password
) {}

