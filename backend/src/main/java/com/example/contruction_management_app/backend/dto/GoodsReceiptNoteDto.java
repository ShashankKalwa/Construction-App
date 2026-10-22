package com.example.contruction_management_app.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceiptNoteDto {
    private String id;
    private String purchaseOrderId;
    private String poNumber;
    private String materialId;
    private String materialName;
    private String unit;
    private String challanNumber;
    private BigDecimal receivedQuantity;
    private LocalDate receivedDate;
    private String inspectedBy;
    private String qualityRemarks;
    private Instant createdAt;
}
