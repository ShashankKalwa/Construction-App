package com.example.contruction_management_app.feature.finance

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.contruction_management_app.core.network.NetworkClient
import com.example.contruction_management_app.domain.model.CostDashboardSummary
import com.example.contruction_management_app.domain.model.DashboardPeriod
import com.example.contruction_management_app.domain.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface BudgetDashboardUiState {
    object Loading : BudgetDashboardUiState
    data class Success(val summary: CostDashboardSummary) : BudgetDashboardUiState
    data class Error(val message: String) : BudgetDashboardUiState
}

class BudgetDashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FinanceRepository(NetworkClient.getApiService(application))

    private val _uiState = MutableStateFlow<BudgetDashboardUiState>(BudgetDashboardUiState.Loading)
    val uiState: StateFlow<BudgetDashboardUiState> = _uiState.asStateFlow()

    private var currentProjectId: String = "prj_commercial_tower_b"
    private var currentPeriod: DashboardPeriod = DashboardPeriod.TOTAL

    fun loadDashboard(projectId: String, period: DashboardPeriod = currentPeriod) {
        currentProjectId = projectId
        currentPeriod = period
        viewModelScope.launch {
            _uiState.value = BudgetDashboardUiState.Loading
            val result = repository.getCostDashboard(projectId, period)
            result.fold(
                onSuccess = { _uiState.value = BudgetDashboardUiState.Success(it) },
                onFailure = { _uiState.value = BudgetDashboardUiState.Error(it.message ?: "Failed to load budget dashboard") }
            )
        }
    }

    fun setPeriod(period: DashboardPeriod) {
        loadDashboard(currentProjectId, period)
    }
}
