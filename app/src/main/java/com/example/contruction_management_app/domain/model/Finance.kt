package com.example.contruction_management_app.domain.model

import java.math.BigDecimal

enum class ExpenseCategory {
    LABOR,
    EQUIPMENT,
    UTILITIES,
    SUBCONTRACTOR,
    PETTY_CASH,
    MATERIALS
}

enum class ExpenseStatus {
    SUBMITTED,
    VERIFIED,
    APPROVED,
    PAID,
    REJECTED
}

enum class BillPaymentStatus {
    PENDING,
    APPROVED,
    PAID,
    REJECTED
}

data class Expense(
    val id: String,
    val projectId: String,
    val category: ExpenseCategory,
    val title: String,
    val amount: BigDecimal,
    val expenseDate: String,
    val receiptUrl: String?,
    val status: ExpenseStatus,
    val submittedBy: String?,
    val verifiedBy: String?,
    val approvedBy: String?,
    val paidBy: String?,
    val createdAt: String? = null
)

data class ContractorBill(
    val id: String,
    val projectId: String,
    val contractorName: String,
    val billNumber: String,
    val billDate: String,
    val grossAmount: BigDecimal,
    val retentionPct: BigDecimal,
    val retentionAmount: BigDecimal,
    val tdsPct: BigDecimal,
    val tdsAmount: BigDecimal,
    val netPayable: BigDecimal,
    val status: BillPaymentStatus,
    val workDescription: String?,
    val certifiedBy: String?,
    val createdAt: String? = null
)
