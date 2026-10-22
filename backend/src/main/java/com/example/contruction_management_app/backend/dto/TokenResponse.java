package com.example.contruction_management_app.backend.dto;

public record TokenResponse(
    String accessToken,
    String tokenType,
    UserDto user
) {
    public TokenResponse(String accessToken, UserDto user) {
        this(accessToken, "bearer", user);
    }
}
