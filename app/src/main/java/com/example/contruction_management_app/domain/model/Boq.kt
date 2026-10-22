package com.example.contruction_management_app.domain.model

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

enum class BoqStatus {
    DRAFT,
    SUBMITTED,
    APPROVED,
    REJECTED
}

data class BoqItem(
    val id: String,
    val boqId: String,
    val itemCode: String,
    val description: String,
    val category: String,
    val unit: String,
    val estimatedQuantity: BigDecimal,
    val unitRate: BigDecimal,
    val totalAmount: BigDecimal,
    val calculationId: String? = null,
    val orderIndex: Int = 0
) {
    fun formatAmount(): String {
        val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))
        return format.format(totalAmount)
    }

    fun formatRate(): String {
        val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))
        return "${format.format(unitRate)} / $unit"
    }
}

data class Boq(
    val id: String,
    val projectId: String,
    val title: String,
    val version: Int,
    val status: BoqStatus,
    val totalAmount: BigDecimal,
    val createdBy: String?,
    val approvedBy: String?,
    val items: List<BoqItem> = emptyList(),
    val createdAt: String,
    val updatedAt: String? = null
) {
    fun formatTotalAmount(): String {
        val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))
        return format.format(totalAmount)
    }
}
