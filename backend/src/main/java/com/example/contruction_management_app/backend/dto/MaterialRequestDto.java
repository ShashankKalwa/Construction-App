package com.example.contruction_management_app.backend.dto;

import com.example.contruction_management_app.backend.domain.MaterialRequest;
import com.example.contruction_management_app.backend.domain.MaterialRequestStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record MaterialRequestDto(
    String id,
    String projectId,
    String materialId,
    String materialName,
    String materialUnit,
    BigDecimal quantity,
    LocalDate requiredByDate,
    String requestedBy,
    String approvedBy,
    MaterialRequestStatus status,
    String phaseOrTaskDesc,
    Instant createdAt
) {
    public static MaterialRequestDto fromEntity(MaterialRequest r) {
        return new MaterialRequestDto(
            r.getId(),
            r.getProject().getId(),
            r.getMaterial().getId(),
            r.getMaterial().getName(),
            r.getMaterial().getUnit(),
            r.getQuantity(),
            r.getRequiredByDate(),
            r.getRequestedBy(),
            r.getApprovedBy(),
            r.getStatus(),
            r.getPhaseOrTaskDesc(),
            r.getCreatedAt()
        );
    }
}
