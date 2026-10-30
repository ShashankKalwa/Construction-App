package com.example.contruction_management_app.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record SitePhotoCreateDto(
    String reportId,
    String taskId,
    String defectId,
    @NotBlank String photoUrl,
    String caption,
    String locationTag
) {}
