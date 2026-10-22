package com.example.contruction_management_app.backend.dto;

import com.example.contruction_management_app.backend.domain.PurchaseOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderDto {
    private String id;
    private String poNumber;
    private String projectId;
    private String vendorId;
    private String vendorName;
    private LocalDate orderDate;
    private LocalDate deliveryDeadline;
    private BigDecimal totalAmount;
    private PurchaseOrderStatus status;
    private String createdBy;
    @Builder.Default
    private List<PurchaseOrderItemDto> items = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;
}
