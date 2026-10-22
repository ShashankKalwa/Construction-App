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
public class MaterialConsumptionDto {
    private String materialId;
    private String materialName;
    private String unit;
    private BigDecimal quantityConsumed;
    private BigDecimal totalCost;
    private BigDecimal consumptionPercentage;
}
