package com.example.contruction_management_app.feature.audit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.contruction_management_app.core.network.NetworkClient
import com.example.contruction_management_app.domain.model.AuditEntityType
import com.example.contruction_management_app.domain.model.AuditLogItem
import com.example.contruction_management_app.domain.model.AuditSummary
import com.example.contruction_management_app.domain.repository.AuditRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuditUiState {
    object Loading : AuditUiState
    data class Success(
        val allLogs: List<AuditLogItem>,
        val filteredLogs: List<AuditLogItem>,
        val summary: AuditSummary? = null,
        val selectedType: AuditEntityType? = null
    ) : AuditUiState
    data class Error(val message: String) : AuditUiState
}

class AuditViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuditRepository(NetworkClient.getApiService(application))

    private val _uiState = MutableStateFlow<AuditUiState>(AuditUiState.Loading)
    val uiState: StateFlow<AuditUiState> = _uiState.asStateFlow()

    private var currentProjectId: String = "prj_commercial_tower_b"
    private var allLogsList = listOf<AuditLogItem>()
    private var currentSummary: AuditSummary? = null
    private var currentTypeFilter: AuditEntityType? = null

    fun loadAuditLogs(projectId: String) {
        currentProjectId = projectId
        viewModelScope.launch {
            _uiState.value = AuditUiState.Loading
            val logsResult = repository.getAuditLogs(projectId)
            val summaryResult = repository.getAuditSummary(projectId)

            summaryResult.fold(
                onSuccess = { currentSummary = it },
                onFailure = {}
            )

            logsResult.fold(
                onSuccess = { list ->
                    allLogsList = list
                    applyFilter()
                },
                onFailure = {
                    _uiState.value = AuditUiState.Error(it.message ?: "Failed to load audit logs")
                }
            )
        }
    }

    fun setTypeFilter(type: AuditEntityType?) {
        currentTypeFilter = type
        applyFilter()
    }

    private fun applyFilter() {
        val filtered = if (currentTypeFilter != null) {
            allLogsList.filter { it.entityType == currentTypeFilter }
        } else {
            allLogsList
        }
        _uiState.value = AuditUiState.Success(
            allLogs = allLogsList,
            filteredLogs = filtered,
            summary = currentSummary,
            selectedType = currentTypeFilter
        )
    }
}
