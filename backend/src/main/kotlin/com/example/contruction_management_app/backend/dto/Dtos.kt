package com.example.contruction_management_app.backend.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

data class LoginRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Password is required")
    val password: String
)

data class RoleDto(
    val id: String,
    val name: String,
    val description: String?,
    val permissions: List<String>
)

data class UserDto(
    val id: String,
    val email: String,
    val fullName: String,
    val avatarUrl: String?,
    val organizationId: String,
    val organizationName: String?,
    val role: RoleDto
)

data class TokenResponse(
    val accessToken: String,
    val tokenType: String = "bearer",
    val user: UserDto
)

data class ProjectCreateDto(
    @field:NotBlank(message = "Project name is required")
    val name: String,

    @field:NotBlank(message = "Project code is required")
    val code: String,

    @field:NotBlank(message = "Location is required")
    val location: String,

    @field:Positive(message = "Budget must be positive")
    val budgetInr: BigDecimal
)

data class ProjectResponseDto(
    val id: String,
    val name: String,
    val code: String,
    val location: String,
    val budgetInr: BigDecimal,
    val status: String,
    val progressPct: Double,
    val organizationId: String
)

data class HealthResponse(
    val status: String,
    val service: String,
    val version: String,
    val timestamp: Long = System.currentTimeMillis()
)
