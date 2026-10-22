package com.example.contruction_management_app.backend.dto;

import com.example.contruction_management_app.backend.domain.WeatherCondition;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record DailySiteReportCreateDto(
    @NotNull(message = "Report date is required")
    LocalDate reportDate,

    @NotNull(message = "Weather condition is required")
    WeatherCondition weather,

    @NotBlank(message = "Work summary is required")
    String workSummary,

    String plannedTomorrow,

    @Valid
    List<LaborAttendanceDto> laborAttendances
) {}
