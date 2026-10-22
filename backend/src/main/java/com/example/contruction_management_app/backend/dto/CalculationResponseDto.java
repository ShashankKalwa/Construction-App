package com.example.contruction_management_app.backend.dto;

import com.example.contruction_management_app.backend.domain.Calculation;
import com.example.contruction_management_app.backend.domain.CalculatorType;

import java.time.Instant;

public record CalculationResponseDto(
    String id,
    String projectId,
    CalculatorType calculatorType,
    String title,
    String elementType,
    String inputParameters,
    String outputResults,
    Double primaryQuantity,
    String primaryUnit,
    String createdBy,
    Instant createdAt
) {
    public static CalculationResponseDto fromEntity(Calculation entity) {
        return new CalculationResponseDto(
            entity.getId(),
            entity.getProjectId(),
            entity.getCalculatorType(),
            entity.getTitle(),
            entity.getElementType(),
            entity.getInputParameters(),
            entity.getOutputResults(),
            entity.getPrimaryQuantity(),
            entity.getPrimaryUnit(),
            entity.getCreatedBy(),
            entity.getCreatedAt()
        );
    }
}
