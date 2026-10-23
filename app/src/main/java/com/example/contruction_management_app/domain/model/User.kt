package com.example.contruction_management_app.domain.model

data class User(
    val id: String,
    val email: String,
    val fullName: String,
    val avatarUrl: String? = null,
    val organizationId: String,
    val organizationName: String = "Apex Infra Ltd",
    val role: Role,
    val assignedProjectIds: List<String> = emptyList()
)

data class Organization(
    val id: String,
    val name: String,
    val code: String
)

data class ProjectSummary(
    val id: String,
    val name: String,
    val location: String,
    val progress: Float,
    val status: String,
    val budgetInr: Double
)

data class RoleCredential(
    val role: Role,
    val username: String,
    val email: String,
    val password: String,
    val description: String
)

