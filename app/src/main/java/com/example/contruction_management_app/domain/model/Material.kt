package com.example.contruction_management_app.domain.model

import java.math.BigDecimal

enum class MaterialCategory {
    CEMENT,
    STEEL,
    AGGREGATE,
    MASONRY,
    PAINT,
    ELECTRICAL,
    PLUMBING,
    TIMBER,
    OTHER
}

enum class MovementType {
    INWARD_GRN,
    OUTWARD_ISSUE,
    ADJUSTMENT
}

enum class MaterialRequestStatus {
    PENDING,
    APPROVED,
    REJECTED,
    FULFILLED
}

data class Material(
    val id: String,
    val projectId: String,
    val code: String,
    val name: String,
    val category: MaterialCategory,
    val unit: String,
    val currentStock: BigDecimal,
    val minStockThreshold: BigDecimal,
    val unitCost: BigDecimal,
    val isLowStock: Boolean = false,
    val createdAt: String? = null
)

data class MaterialRequest(
    val id: String,
    val projectId: String,
    val materialId: String,
    val materialName: String,
    val materialUnit: String,
    val quantity: BigDecimal,
    val requiredByDate: String?,
    val requestedBy: String?,
    val approvedBy: String?,
    val status: MaterialRequestStatus,
    val phaseOrTaskDesc: String?,
    val createdAt: String? = null
)
