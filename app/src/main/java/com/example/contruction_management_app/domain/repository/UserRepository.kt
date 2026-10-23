package com.example.contruction_management_app.domain.repository

import com.example.contruction_management_app.domain.model.Role
import com.example.contruction_management_app.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getAllDummyUsers(): List<User>
    suspend fun getUserByRole(role: Role): User
    suspend fun authenticateUser(email: String, role: Role): Result<User>
    fun getCurrentUser(): Flow<User?>
    suspend fun logout()
}
