package com.example.contruction_management_app.backend.dto;

import java.util.List;

public record RoleDto(
    String id,
    String name,
    String description,
    List<String> permissions
) {}
