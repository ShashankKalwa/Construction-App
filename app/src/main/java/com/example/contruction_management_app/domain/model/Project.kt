package com.example.contruction_management_app.domain.model

import java.math.BigDecimal

data class Project(
    val id: String,
    val name: String,
    val code: String,
    val location: String,
    val budgetInr: BigDecimal,
    val status: String = "Active",
    val progressPct: Double = 0.0,
    val organizationId: String
) {
    val budgetDisplay: String
        get() {
            val amount = budgetInr.toDouble()
            return when {
                amount >= 10000000.0 -> "₹%.2f Cr".format(amount / 10000000.0)
                amount >= 100000.0 -> "₹%.2f L".format(amount / 100000.0)
                else -> "₹%,.0f".format(amount)
            }
        }
}

data class UserSummary(
    val id: String,
    val fullName: String,
    val email: String,
    val avatarUrl: String? = null
)

data class ProjectMember(
    val id: String,
    val user: UserSummary,
    val assignedRole: String?,
    val joinedAt: String
)

data class ProjectDetail(
    val id: String,
    val name: String,
    val code: String,
    val location: String,
    val budgetInr: BigDecimal,
    val status: String,
    val progressPct: Double,
    val organizationId: String,
    val members: List<ProjectMember> = emptyList(),
    val phasesCount: Int = 0,
    val tasksCount: Int = 0
) {
    val budgetDisplay: String
        get() {
            val amount = budgetInr.toDouble()
            return when {
                amount >= 10000000.0 -> "₹%.2f Cr".format(amount / 10000000.0)
                amount >= 100000.0 -> "₹%.2f L".format(amount / 100000.0)
                else -> "₹%,.0f".format(amount)
            }
        }
}
