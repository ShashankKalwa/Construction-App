package com.example.contruction_management_app.feature.siteops

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.contruction_management_app.core.network.NetworkClient
import com.example.contruction_management_app.domain.model.*
import com.example.contruction_management_app.domain.repository.SiteOperationsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DsrUiState {
    object Loading : DsrUiState
    data class Success(val reports: List<DailySiteReport>) : DsrUiState
    data class Error(val message: String) : DsrUiState
}

sealed interface IssuesUiState {
    object Loading : IssuesUiState
    data class Success(val issues: List<SiteIssue>) : IssuesUiState
    data class Error(val message: String) : IssuesUiState
}

class SiteOperationsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SiteOperationsRepository(NetworkClient.getApiService(application))

    private val _dsrState = MutableStateFlow<DsrUiState>(DsrUiState.Loading)
    val dsrState: StateFlow<DsrUiState> = _dsrState.asStateFlow()

    private val _issuesState = MutableStateFlow<IssuesUiState>(IssuesUiState.Loading)
    val issuesState: StateFlow<IssuesUiState> = _issuesState.asStateFlow()

    private var currentProjectId: String = "prj_commercial_tower_b"
    private var currentStatusFilter: IssueStatus? = null

    fun loadDailyReports(projectId: String) {
        currentProjectId = projectId
        viewModelScope.launch {
            _dsrState.value = DsrUiState.Loading
            val result = repository.getDailyReports(projectId)
            result.fold(
                onSuccess = { _dsrState.value = DsrUiState.Success(it) },
                onFailure = { _dsrState.value = DsrUiState.Error(it.message ?: "Failed to load reports") }
            )
        }
    }

    fun createDailyReport(
        reportDate: String,
        weather: WeatherCondition,
        workSummary: String,
        plannedTomorrow: String?,
        laborAttendances: List<LaborAttendance>
    ) {
        viewModelScope.launch {
            val result = repository.createDailyReport(
                projectId = currentProjectId,
                reportDate = reportDate,
                weather = weather,
                workSummary = workSummary,
                plannedTomorrow = plannedTomorrow,
                laborAttendances = laborAttendances
            )
            result.fold(
                onSuccess = {
                    loadDailyReports(currentProjectId)
                },
                onFailure = {
                    loadDailyReports(currentProjectId)
                }
            )
        }
    }

    fun loadIssues(projectId: String, statusFilter: IssueStatus? = null) {
        currentProjectId = projectId
        currentStatusFilter = statusFilter
        viewModelScope.launch {
            _issuesState.value = IssuesUiState.Loading
            val result = repository.getIssues(projectId, statusFilter)
            result.fold(
                onSuccess = { _issuesState.value = IssuesUiState.Success(it) },
                onFailure = { _issuesState.value = IssuesUiState.Error(it.message ?: "Failed to load issues") }
            )
        }
    }

    fun createIssue(
        title: String,
        description: String?,
        severity: IssueSeverity,
        assignedTo: String?
    ) {
        viewModelScope.launch {
            val result = repository.createIssue(
                projectId = currentProjectId,
                reportId = null,
                title = title,
                description = description,
                severity = severity,
                assignedTo = assignedTo
            )
            result.fold(
                onSuccess = {
                    loadIssues(currentProjectId, currentStatusFilter)
                },
                onFailure = {
                    loadIssues(currentProjectId, currentStatusFilter)
                }
            )
        }
    }

    fun updateIssueStatus(issueId: String, status: IssueStatus, resolutionNotes: String? = null) {
        viewModelScope.launch {
            val result = repository.updateIssueStatus(
                projectId = currentProjectId,
                issueId = issueId,
                status = status,
                resolutionNotes = resolutionNotes
            )
            result.fold(
                onSuccess = {
                    loadIssues(currentProjectId, currentStatusFilter)
                },
                onFailure = {
                    loadIssues(currentProjectId, currentStatusFilter)
                }
            )
        }
    }
}
