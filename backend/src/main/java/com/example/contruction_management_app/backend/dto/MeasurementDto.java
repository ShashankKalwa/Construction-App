package com.example.contruction_management_app.backend.dto;

import com.example.contruction_management_app.backend.domain.Measurement;

import java.math.BigDecimal;
import java.time.Instant;

public record MeasurementDto(
    String id,
    String projectId,
    String boqItemId,
    String itemDescription,
    BigDecimal numberOfUnits,
    BigDecimal lengthM,
    BigDecimal widthM,
    BigDecimal depthM,
    BigDecimal deductionM3,
    BigDecimal netQuantity,
    String unit,
    String locationDetails,
    String measuredBy,
    Instant measuredAt
) {
    public static MeasurementDto fromEntity(Measurement m) {
        return new MeasurementDto(
            m.getId(),
            m.getProject().getId(),
            m.getBoqItem() != null ? m.getBoqItem().getId() : null,
            m.getItemDescription(),
            m.getNumberOfUnits(),
            m.getLengthM(),
            m.getWidthM(),
            m.getDepthM(),
            m.getDeductionM3(),
            m.getNetQuantity(),
            m.getUnit(),
            m.getLocationDetails(),
            m.getMeasuredBy(),
            m.getMeasuredAt()
        );
    }
}
