package com.example.contruction_management_app.backend.dto;

import com.example.contruction_management_app.backend.domain.IssueSeverity;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record SiteDefectCreateDto(
    String inspectionId,
    @NotBlank String title,
    @NotBlank String description,
    String location,
    IssueSeverity severity,
    String assignedTo,
    LocalDate deadline
) {}
