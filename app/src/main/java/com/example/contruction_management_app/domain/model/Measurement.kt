package com.example.contruction_management_app.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

data class Measurement(
    val id: String,
    val projectId: String,
    val boqItemId: String? = null,
    val itemDescription: String,
    val numberOfUnits: BigDecimal,
    val lengthM: BigDecimal,
    val widthM: BigDecimal,
    val depthM: BigDecimal,
    val deductionM3: BigDecimal,
    val netQuantity: BigDecimal,
    val unit: String,
    val locationDetails: String? = null,
    val measuredBy: String? = null,
    val measuredAt: String
) {
    fun formatDimensions(): String {
        return "${numberOfUnits.stripTrailingZeros().toPlainString()} nos × " +
                "${lengthM.setScale(2, RoundingMode.HALF_UP)}m × " +
                "${widthM.setScale(2, RoundingMode.HALF_UP)}m × " +
                "${depthM.setScale(2, RoundingMode.HALF_UP)}m"
    }

    fun formatNetQuantity(): String {
        return "${netQuantity.setScale(2, RoundingMode.HALF_UP).toPlainString()} $unit"
    }
}
