package com.example.contruction_management_app.backend.dto;

import com.example.contruction_management_app.backend.domain.DailySiteReport;
import com.example.contruction_management_app.backend.domain.WeatherCondition;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record DailySiteReportDto(
    String id,
    String projectId,
    LocalDate reportDate,
    WeatherCondition weather,
    String workSummary,
    String plannedTomorrow,
    String createdBy,
    List<LaborAttendanceDto> laborAttendances,
    Integer totalHeadcount,
    Instant createdAt
) {
    public static DailySiteReportDto fromEntity(DailySiteReport r) {
        List<LaborAttendanceDto> laList = (r.getLaborAttendances() != null)
            ? r.getLaborAttendances().stream().map(LaborAttendanceDto::fromEntity).toList()
            : List.of();

        int totalHeadcount = laList.stream().mapToInt(LaborAttendanceDto::headcount).sum();

        return new DailySiteReportDto(
            r.getId(),
            r.getProject().getId(),
            r.getReportDate(),
            r.getWeather(),
            r.getWorkSummary(),
            r.getPlannedTomorrow(),
            r.getCreatedBy(),
            laList,
            totalHeadcount,
            r.getCreatedAt()
        );
    }
}
