package com.example.contruction_management_app.backend.dto;

import com.example.contruction_management_app.backend.domain.InspectionStatus;
import com.example.contruction_management_app.backend.domain.InspectionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SiteInspectionCreateDto(
    @NotBlank String title,
    @NotNull InspectionType inspectionType,
    InspectionStatus status,
    String checklistData,
    LocalDate inspectionDate,
    String location,
    String notes
) {}
