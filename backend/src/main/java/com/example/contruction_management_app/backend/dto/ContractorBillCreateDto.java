package com.example.contruction_management_app.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractorBillCreateDto {
    @NotBlank
    private String contractorName;

    private String billNumber;

    @NotNull
    private LocalDate billDate;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal grossAmount;

    @Builder.Default
    private BigDecimal retentionPct = BigDecimal.valueOf(5.00);

    @Builder.Default
    private BigDecimal tdsPct = BigDecimal.valueOf(2.00);

    private String workDescription;
}
