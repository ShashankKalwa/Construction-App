package com.example.contruction_management_app.backend.dto;

import com.example.contruction_management_app.backend.domain.BillPaymentStatus;
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
public class ContractorBillDto {
    private String id;
    private String projectId;
    private String contractorName;
    private String billNumber;
    private LocalDate billDate;
    private BigDecimal grossAmount;
    private BigDecimal retentionPct;
    private BigDecimal retentionAmount;
    private BigDecimal tdsPct;
    private BigDecimal tdsAmount;
    private BigDecimal netPayable;
    private BillPaymentStatus status;
    private String workDescription;
    private String certifiedBy;
    private Instant createdAt;
    private Instant updatedAt;
}
