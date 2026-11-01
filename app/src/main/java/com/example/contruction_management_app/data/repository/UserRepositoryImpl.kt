package com.example.contruction_management_app.data.repository

import android.content.Context
import com.example.contruction_management_app.core.auth.SessionDataStore
import com.example.contruction_management_app.core.network.CmsApiService
import com.example.contruction_management_app.core.network.LoginNetworkRequest
import com.example.contruction_management_app.core.network.NetworkClient
import com.example.contruction_management_app.domain.model.Role
import com.example.contruction_management_app.domain.model.User
import com.example.contruction_management_app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import java.io.IOException

class UserRepositoryImpl(
    private val context: Context,
    private val sessionDataStore: SessionDataStore = SessionDataStore(context),
    private val apiService: CmsApiService = NetworkClient.getApiService(context)
) : UserRepository {

    override suspend fun authenticateWithCredentials(
        username: String,
        password: String
    ): Result<User> {
        val trimmedUsername = username.trim()
        val trimmedPassword = password.trim()

        if (trimmedUsername.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter your email or username."))
        }
        if (trimmedPassword.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter your password."))
        }

        val loginEmail = if (trimmedUsername.contains("@")) {
            trimmedUsername
        } else {
            "$trimmedUsername@chaitanyainfratech.com"
        }

        return try {
            val response = apiService.login(LoginNetworkRequest(loginEmail, trimmedPassword))
            if (response.isSuccessful && response.body() != null) {
                val loginBody = response.body()!!
                val netUser = loginBody.user
                val authenticatedUser = User(
                    id = netUser.id,
                    email = netUser.email,
                    fullName = netUser.fullName,
                    avatarUrl = netUser.avatarUrl,
                    organizationId = netUser.organizationId,
                    organizationName = netUser.organizationName.ifBlank { "Chaitanya InfraTech" },
                    role = Role.fromId(netUser.role.id)
                )

                sessionDataStore.saveSession(
                    token = loginBody.accessToken,
                    user = authenticatedUser
                )
                try {
                    val meResponse = apiService.getMe()
                    if (meResponse.isSuccessful && meResponse.body() != null) {
                        sessionDataStore.saveUserMe(meResponse.body()!!)
                    }
                } catch (_: Exception) {
                    // Ignore if /me call cannot be reached immediately
                }
                Result.success(authenticatedUser)
            } else if (response.code() == 401 || response.code() == 403) {
                Result.failure(IllegalArgumentException("Invalid email or password"))
            } else {
                Result.failure(IllegalStateException(response.errorBody()?.string() ?: "Authentication failed (HTTP ${response.code()})"))
            }
        } catch (e: IOException) {
            Result.failure(IOException("Unable to connect to server. Please check your internet connection.", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): Flow<User?> {
        return sessionDataStore.currentUserFlow
    }

    override suspend fun logout() {
        sessionDataStore.clearSession()
    }
}
