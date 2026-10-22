package com.example.contruction_management_app.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record BoqCreateDto(
    @NotBlank(message = "Title is required")
    String title
) {}
