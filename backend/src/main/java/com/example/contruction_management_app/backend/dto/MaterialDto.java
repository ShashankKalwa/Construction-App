package com.example.contruction_management_app.backend.dto;

import com.example.contruction_management_app.backend.domain.Material;
import com.example.contruction_management_app.backend.domain.MaterialCategory;

import java.math.BigDecimal;
import java.time.Instant;

public record MaterialDto(
    String id,
    String projectId,
    String code,
    String name,
    MaterialCategory category,
    String unit,
    BigDecimal currentStock,
    BigDecimal minStockThreshold,
    BigDecimal unitCost,
    boolean isLowStock,
    Instant createdAt
) {
    public static MaterialDto fromEntity(Material m) {
        return new MaterialDto(
            m.getId(),
            m.getProject().getId(),
            m.getCode(),
            m.getName(),
            m.getCategory(),
            m.getUnit(),
            m.getCurrentStock(),
            m.getMinStockThreshold(),
            m.getUnitCost(),
            m.isLowStock(),
            m.getCreatedAt()
        );
    }
}
