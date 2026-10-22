package com.example.contruction_management_app.domain.model

import java.math.BigDecimal

enum class PurchaseOrderStatus {
    DRAFT,
    ISSUED,
    PARTIALLY_DELIVERED,
    COMPLETED,
    CANCELLED
}

data class Vendor(
    val id: String,
    val organizationId: String,
    val name: String,
    val gstin: String?,
    val phone: String?,
    val email: String?,
    val category: String?,
    val paymentTerms: String?
)

data class PurchaseOrderItem(
    val id: String,
    val purchaseOrderId: String,
    val materialId: String,
    val materialName: String,
    val unit: String,
    val orderedQuantity: BigDecimal,
    val unitPrice: BigDecimal,
    val totalPrice: BigDecimal,
    val receivedQuantity: BigDecimal = BigDecimal.ZERO
)

data class PurchaseOrder(
    val id: String,
    val poNumber: String,
    val projectId: String,
    val vendorId: String,
    val vendorName: String?,
    val orderDate: String,
    val deliveryDeadline: String?,
    val totalAmount: BigDecimal,
    val status: PurchaseOrderStatus,
    val createdBy: String?,
    val items: List<PurchaseOrderItem> = emptyList(),
    val createdAt: String? = null
)

data class GoodsReceiptNote(
    val id: String,
    val purchaseOrderId: String,
    val poNumber: String?,
    val materialId: String,
    val materialName: String?,
    val unit: String?,
    val challanNumber: String,
    val receivedQuantity: BigDecimal,
    val receivedDate: String,
    val inspectedBy: String?,
    val qualityRemarks: String?,
    val createdAt: String? = null
)
