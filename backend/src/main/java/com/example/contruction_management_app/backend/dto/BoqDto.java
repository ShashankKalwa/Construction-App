package com.example.contruction_management_app.backend.dto;

import com.example.contruction_management_app.backend.domain.Boq;
import com.example.contruction_management_app.backend.domain.BoqStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record BoqDto(
    String id,
    String projectId,
    String title,
    Integer version,
    BoqStatus status,
    BigDecimal totalAmount,
    String createdBy,
    String approvedBy,
    List<BoqItemDto> items,
    Instant createdAt,
    Instant updatedAt
) {
    public static BoqDto fromEntity(Boq boq) {
        List<BoqItemDto> itemDtos = (boq.getItems() != null)
            ? boq.getItems().stream().map(BoqItemDto::fromEntity).toList()
            : List.of();

        return new BoqDto(
            boq.getId(),
            boq.getProject().getId(),
            boq.getTitle(),
            boq.getVersion(),
            boq.getStatus(),
            boq.getTotalAmount(),
            boq.getCreatedBy(),
            boq.getApprovedBy(),
            itemDtos,
            boq.getCreatedAt(),
            boq.getUpdatedAt()
        );
    }
}
