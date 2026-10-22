package com.example.contruction_management_app.backend.dto;

import com.example.contruction_management_app.backend.domain.ExpenseCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCostDto {
    private ExpenseCategory category;
    private String categoryName;
    private BigDecimal budgetAmount;
    private BigDecimal actualAmount;
    private BigDecimal percentage;
}
