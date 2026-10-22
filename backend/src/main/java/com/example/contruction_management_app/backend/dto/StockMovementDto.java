package com.example.contruction_management_app.backend.dto;

import com.example.contruction_management_app.backend.domain.MovementType;
import com.example.contruction_management_app.backend.domain.StockMovement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementDto {
    private String id;
    private String materialId;
    private String materialName;
    private MovementType movementType;
    private BigDecimal quantity;
    private BigDecimal balanceAfter;
    private String referenceId;
    private String notes;
    private String performedBy;
    private Instant createdAt;

    public static StockMovementDto fromEntity(StockMovement sm) {
        return StockMovementDto.builder()
            .id(sm.getId())
            .materialId(sm.getMaterial().getId())
            .materialName(sm.getMaterial().getName())
            .movementType(sm.getMovementType())
            .quantity(sm.getQuantity())
            .balanceAfter(sm.getBalanceAfter())
            .referenceId(sm.getReferenceId())
            .notes(sm.getNotes())
            .performedBy(sm.getPerformedBy())
            .createdAt(sm.getCreatedAt())
            .build();
    }
}
