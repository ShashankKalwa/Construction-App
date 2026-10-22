package com.example.contruction_management_app.backend.dto;

import com.example.contruction_management_app.backend.domain.LaborAttendance;
import com.example.contruction_management_app.backend.domain.LaborTrade;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record LaborAttendanceDto(
    String id,
    @NotNull(message = "Trade is required")
    LaborTrade trade,
    @NotNull(message = "Headcount is required")
    @Positive(message = "Headcount must be positive")
    Integer headcount,
    BigDecimal hoursWorked,
    String contractorName
) {
    public static LaborAttendanceDto fromEntity(LaborAttendance la) {
        return new LaborAttendanceDto(
            la.getId(),
            la.getTrade(),
            la.getHeadcount(),
            la.getHoursWorked(),
            la.getContractorName()
        );
    }
}
