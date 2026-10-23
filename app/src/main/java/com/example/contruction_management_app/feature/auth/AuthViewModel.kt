package com.example.contruction_management_app.feature.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.contruction_management_app.data.repository.UserRepositoryImpl
import com.example.contruction_management_app.domain.model.Role
import com.example.contruction_management_app.domain.model.User
import com.example.contruction_management_app.domain.repository.UserRepository
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

class AuthViewModel @JvmOverloads constructor(
    application: Application,
    private val userRepository: UserRepository = UserRepositoryImpl(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkExistingSession()
    }

    private fun checkExistingSession() {
        viewModelScope.launch {
            userRepository.getCurrentUser().collect { user ->
                if (user != null) {
                    _uiState.value = AuthUiState.Authenticated(user)
                }
            }
        }
    }

    fun login(email: String, role: Role) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = userRepository.authenticateUser(email, role)
            result.onSuccess { user ->
                _uiState.value = AuthUiState.Authenticated(user)
            }.onFailure { e ->
                _uiState.value = AuthUiState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
            _uiState.value = AuthUiState.Idle
        }
    }
}
