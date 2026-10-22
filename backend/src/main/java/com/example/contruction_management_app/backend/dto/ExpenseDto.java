package com.example.contruction_management_app.backend.dto;

import com.example.contruction_management_app.backend.domain.ExpenseCategory;
import com.example.contruction_management_app.backend.domain.ExpenseStatus;
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
public class ExpenseDto {
    private String id;
    private String projectId;
    private ExpenseCategory category;
    private String title;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private String receiptUrl;
    private ExpenseStatus status;
    private String submittedBy;
    private String verifiedBy;
    private String approvedBy;
    private String paidBy;
    private Instant createdAt;
    private Instant updatedAt;
}
