package com.example.contruction_management_app.backend.dto;

import com.example.contruction_management_app.backend.domain.WorkProgressRecord;
import com.example.contruction_management_app.backend.domain.WorkProgressStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record WorkProgressDto(
    String id,
    String projectId,
    String taskId,
    String reportId,
    String itemName,
    String unit,
    BigDecimal plannedQuantity,
    BigDecimal reportedQuantity,
    BigDecimal verifiedQuantity,
    WorkProgressStatus status,
    String reportedBy,
    String verifiedBy,
    String verificationNotes,
    Instant recordedAt,
    Instant verifiedAt
) {
    public static WorkProgressDto fromEntity(WorkProgressRecord r) {
        return new WorkProgressDto(
            r.getId(),
            r.getProject().getId(),
            r.getTaskId(),
            r.getReportId(),
            r.getItemName(),
            r.getUnit(),
            r.getPlannedQuantity(),
            r.getReportedQuantity(),
            r.getVerifiedQuantity(),
            r.getStatus(),
            r.getReportedBy(),
            r.getVerifiedBy(),
            r.getVerificationNotes(),
            r.getRecordedAt(),
            r.getVerifiedAt()
        );
    }
}
