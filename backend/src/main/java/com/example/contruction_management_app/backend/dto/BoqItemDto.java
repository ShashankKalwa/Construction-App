package com.example.contruction_management_app.backend.dto;

import com.example.contruction_management_app.backend.domain.BoqItem;

import java.math.BigDecimal;

public record BoqItemDto(
    String id,
    String boqId,
    String itemCode,
    String description,
    String category,
    String unit,
    BigDecimal estimatedQuantity,
    BigDecimal unitRate,
    BigDecimal totalAmount,
    String calculationId,
    Integer orderIndex
) {
    public static BoqItemDto fromEntity(BoqItem item) {
        return new BoqItemDto(
            item.getId(),
            item.getBoq().getId(),
            item.getItemCode(),
            item.getDescription(),
            item.getCategory(),
            item.getUnit(),
            item.getEstimatedQuantity(),
            item.getUnitRate(),
            item.getTotalAmount(),
            item.getCalculationId(),
            item.getOrderIndex()
        );
    }
}
