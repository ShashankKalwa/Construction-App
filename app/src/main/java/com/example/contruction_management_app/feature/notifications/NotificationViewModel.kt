package com.example.contruction_management_app.feature.notifications

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.contruction_management_app.core.network.NetworkClient
import com.example.contruction_management_app.domain.model.NotificationItem
import com.example.contruction_management_app.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface NotificationUiState {
    object Loading : NotificationUiState
    data class Success(
        val notifications: List<NotificationItem>,
        val unreadCount: Long
    ) : NotificationUiState
    data class Error(val message: String) : NotificationUiState
}

class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NotificationRepository(NetworkClient.getApiService(application))

    private val _uiState = MutableStateFlow<NotificationUiState>(NotificationUiState.Loading)
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    private val _unreadBadgeCount = MutableStateFlow(0L)
    val unreadBadgeCount: StateFlow<Long> = _unreadBadgeCount.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.value = NotificationUiState.Loading
            val result = repository.getNotifications()
            val unread = repository.getUnreadCount()
            _unreadBadgeCount.value = unread

            result.fold(
                onSuccess = { list ->
                    _uiState.value = NotificationUiState.Success(list, unread)
                },
                onFailure = {
                    _uiState.value = NotificationUiState.Error(it.message ?: "Failed to load notifications")
                }
            )
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            repository.markAsRead(notificationId)
            loadNotifications()
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            repository.markAllAsRead()
            loadNotifications()
        }
    }
}
