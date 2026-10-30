package com.example.contruction_management_app.backend.dto;

import com.example.contruction_management_app.backend.domain.DefectStatus;
import jakarta.validation.constraints.NotNull;

public record SiteDefectStatusUpdateDto(
    @NotNull DefectStatus status
) {}
