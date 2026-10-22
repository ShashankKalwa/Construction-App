package com.example.contruction_management_app.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderItemCreateDto {
    @NotBlank
    private String materialId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal orderedQuantity;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal unitPrice;
}
