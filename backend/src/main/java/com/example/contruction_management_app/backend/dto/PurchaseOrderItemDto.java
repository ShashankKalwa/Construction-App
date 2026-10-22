package com.example.contruction_management_app.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderItemDto {
    private String id;
    private String purchaseOrderId;
    private String materialId;
    private String materialName;
    private String unit;
    private BigDecimal orderedQuantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private BigDecimal receivedQuantity;
}
