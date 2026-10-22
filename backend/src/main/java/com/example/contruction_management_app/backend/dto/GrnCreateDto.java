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
public class GrnCreateDto {
    @NotBlank
    private String purchaseOrderId;

    @NotBlank
    private String materialId;

    @NotBlank
    private String challanNumber;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal receivedQuantity;

    @NotNull
    private LocalDate receivedDate;

    private String qualityRemarks;
}
