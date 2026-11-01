package com.example.contruction_management_app.domain.repository

import com.example.contruction_management_app.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun authenticateWithCredentials(username: String, password: String): Result<User>
    fun getCurrentUser(): Flow<User?>
    suspend fun logout()
}
