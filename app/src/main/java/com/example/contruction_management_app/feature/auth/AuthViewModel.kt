package com.example.contruction_management_app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.contruction_management_app.core.auth.SessionDataStore
import com.example.contruction_management_app.domain.model.Role
import com.example.contruction_management_app.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Authenticated(val user: User) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(
    private val sessionDataStore: SessionDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkExistingSession()
    }

    private fun checkExistingSession() {
        viewModelScope.launch {
            sessionDataStore.currentUserFlow.collect { user ->
                if (user != null) {
                    _uiState.value = AuthUiState.Authenticated(user)
                }
            }
        }
    }

    fun login(email: String, role: Role) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                // In Phase 1, demo login creates an active authenticated session with the selected role
                val dummyUser = User(
                    id = "usr_${System.currentTimeMillis()}",
                    email = email.ifBlank { "${role.id}@apexinfra.com" },
                    fullName = when (role) {
                        Role.SUPER_ADMIN -> "Vikram Malhotra"
                        Role.COMPANY_ADMIN -> "Rajesh Sharma"
                        Role.PROJECT_MANAGER -> "Priya Nair"
                        Role.CIVIL_ENGINEER -> "Anand Verma"
                        Role.SITE_SUPERVISOR -> "Shashank Kumar"
                        Role.STOREKEEPER -> "Ramesh Yadav"
                        Role.ACCOUNTANT -> "Sunita Roy"
                        Role.CONTRACTOR -> "Harpreet Singh"
                        Role.CLIENT_OWNER -> "Deepak Singhal"
                    },
                    organizationId = "org_apex_infra",
                    organizationName = "Apex Infra Ltd",
                    role = role
                )

                sessionDataStore.saveSession(
                    token = "mock_jwt_token_${dummyUser.id}",
                    user = dummyUser
                )
                _uiState.value = AuthUiState.Authenticated(dummyUser)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionDataStore.clearSession()
            _uiState.value = AuthUiState.Idle
        }
    }
}
