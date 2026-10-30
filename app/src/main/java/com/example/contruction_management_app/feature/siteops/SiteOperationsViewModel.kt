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
import java.math.BigDecimal

sealed interface DsrUiState {
    object Loading : DsrUiState
    data class Success(val reports: List<DailySiteReport>) : DsrUiState
    data class Error(val message: String) : DsrUiState
}

sealed interface WorkProgressUiState {
    object Loading : WorkProgressUiState
    data class Success(val list: List<WorkProgress>) : WorkProgressUiState
    data class Error(val message: String) : WorkProgressUiState
}

sealed interface InspectionsUiState {
    object Loading : InspectionsUiState
    data class Success(val inspections: List<SiteInspection>) : InspectionsUiState
    data class Error(val message: String) : InspectionsUiState
}

sealed interface DefectsUiState {
    object Loading : DefectsUiState
    data class Success(val defects: List<SiteDefect>) : DefectsUiState
    data class Error(val message: String) : DefectsUiState
}

sealed interface SitePhotosUiState {
    object Loading : SitePhotosUiState
    data class Success(val photos: List<SitePhoto>) : SitePhotosUiState
    data class Error(val message: String) : SitePhotosUiState
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

    private val _workProgressState = MutableStateFlow<WorkProgressUiState>(WorkProgressUiState.Loading)
    val workProgressState: StateFlow<WorkProgressUiState> = _workProgressState.asStateFlow()

    private val _inspectionsState = MutableStateFlow<InspectionsUiState>(InspectionsUiState.Loading)
    val inspectionsState: StateFlow<InspectionsUiState> = _inspectionsState.asStateFlow()

    private val _defectsState = MutableStateFlow<DefectsUiState>(DefectsUiState.Loading)
    val defectsState: StateFlow<DefectsUiState> = _defectsState.asStateFlow()

    private val _photosState = MutableStateFlow<SitePhotosUiState>(SitePhotosUiState.Loading)
    val photosState: StateFlow<SitePhotosUiState> = _photosState.asStateFlow()

    private val _issuesState = MutableStateFlow<IssuesUiState>(IssuesUiState.Loading)
    val issuesState: StateFlow<IssuesUiState> = _issuesState.asStateFlow()

    private var currentProjectId: String = "prj_commercial_tower_b"
    private var currentDefectFilter: DefectStatus? = null
    private var currentStatusFilter: IssueStatus? = null

    // -------------------------------------------------------------------------
    // Daily Site Reports & Review/Approval
    // -------------------------------------------------------------------------

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
                onSuccess = { loadDailyReports(currentProjectId) },
                onFailure = { loadDailyReports(currentProjectId) }
            )
        }
    }

    fun approveDailyReport(reportId: String, action: String, notes: String? = null) {
        viewModelScope.launch {
            val result = repository.approveDailyReport(currentProjectId, reportId, action, notes)
            result.fold(
                onSuccess = { loadDailyReports(currentProjectId) },
                onFailure = { loadDailyReports(currentProjectId) }
            )
        }
    }

    // -------------------------------------------------------------------------
    // Work Progress Tracking
    // -------------------------------------------------------------------------

    fun loadWorkProgress(projectId: String) {
        currentProjectId = projectId
        viewModelScope.launch {
            _workProgressState.value = WorkProgressUiState.Loading
            val result = repository.getWorkProgress(projectId)
            result.fold(
                onSuccess = { _workProgressState.value = WorkProgressUiState.Success(it) },
                onFailure = { _workProgressState.value = WorkProgressUiState.Error(it.message ?: "Failed to load progress") }
            )
        }
    }

    fun recordWorkProgress(
        taskId: String?,
        reportId: String?,
        itemName: String,
        unit: String,
        plannedQuantity: BigDecimal,
        reportedQuantity: BigDecimal
    ) {
        viewModelScope.launch {
            val result = repository.recordWorkProgress(
                projectId = currentProjectId,
                taskId = taskId,
                reportId = reportId,
                itemName = itemName,
                unit = unit,
                plannedQuantity = plannedQuantity,
                reportedQuantity = reportedQuantity
            )
            result.fold(
                onSuccess = { loadWorkProgress(currentProjectId) },
                onFailure = { loadWorkProgress(currentProjectId) }
            )
        }
    }

    fun verifyWorkProgress(
        progressId: String,
        action: String,
        verifiedQuantity: BigDecimal,
        notes: String?
    ) {
        viewModelScope.launch {
            val result = repository.verifyWorkProgress(
                projectId = currentProjectId,
                progressId = progressId,
                action = action,
                verifiedQuantity = verifiedQuantity,
                notes = notes
            )
            result.fold(
                onSuccess = { loadWorkProgress(currentProjectId) },
                onFailure = { loadWorkProgress(currentProjectId) }
            )
        }
    }

    // -------------------------------------------------------------------------
    // Inspections
    // -------------------------------------------------------------------------

    fun loadInspections(projectId: String) {
        currentProjectId = projectId
        viewModelScope.launch {
            _inspectionsState.value = InspectionsUiState.Loading
            val result = repository.getInspections(projectId)
            result.fold(
                onSuccess = { _inspectionsState.value = InspectionsUiState.Success(it) },
                onFailure = { _inspectionsState.value = InspectionsUiState.Error(it.message ?: "Failed to load inspections") }
            )
        }
    }

    fun createInspection(
        title: String,
        inspectionType: InspectionType,
        status: InspectionStatus,
        checklistData: String,
        inspectionDate: String?,
        location: String?,
        notes: String?
    ) {
        viewModelScope.launch {
            val result = repository.createInspection(
                projectId = currentProjectId,
                title = title,
                inspectionType = inspectionType,
                status = status,
                checklistData = checklistData,
                inspectionDate = inspectionDate,
                location = location,
                notes = notes
            )
            result.fold(
                onSuccess = {
                    loadInspections(currentProjectId)
                    // If inspection failed, defects may have been auto-logged, refresh defects too
                    loadDefects(currentProjectId, currentDefectFilter)
                },
                onFailure = { loadInspections(currentProjectId) }
            )
        }
    }

    // -------------------------------------------------------------------------
    // Snag List & Defects
    // -------------------------------------------------------------------------

    fun loadDefects(projectId: String, statusFilter: DefectStatus? = null) {
        currentProjectId = projectId
        currentDefectFilter = statusFilter
        viewModelScope.launch {
            _defectsState.value = DefectsUiState.Loading
            val result = repository.getDefects(projectId, statusFilter)
            result.fold(
                onSuccess = { _defectsState.value = DefectsUiState.Success(it) },
                onFailure = { _defectsState.value = DefectsUiState.Error(it.message ?: "Failed to load defects") }
            )
        }
    }

    fun createDefect(
        inspectionId: String?,
        title: String,
        description: String,
        location: String?,
        severity: IssueSeverity,
        assignedTo: String?,
        deadline: String?
    ) {
        viewModelScope.launch {
            val result = repository.createDefect(
                projectId = currentProjectId,
                inspectionId = inspectionId,
                title = title,
                description = description,
                location = location,
                severity = severity,
                assignedTo = assignedTo,
                deadline = deadline
            )
            result.fold(
                onSuccess = { loadDefects(currentProjectId, currentDefectFilter) },
                onFailure = { loadDefects(currentProjectId, currentDefectFilter) }
            )
        }
    }

    fun updateDefectStatus(defectId: String, status: DefectStatus) {
        viewModelScope.launch {
            val result = repository.updateDefectStatus(currentProjectId, defectId, status)
            result.fold(
                onSuccess = { loadDefects(currentProjectId, currentDefectFilter) },
                onFailure = { loadDefects(currentProjectId, currentDefectFilter) }
            )
        }
    }

    fun reinspectDefect(defectId: String, action: String, notes: String?) {
        viewModelScope.launch {
            val result = repository.reinspectDefect(currentProjectId, defectId, action, notes)
            result.fold(
                onSuccess = { loadDefects(currentProjectId, currentDefectFilter) },
                onFailure = { loadDefects(currentProjectId, currentDefectFilter) }
            )
        }
    }

    // -------------------------------------------------------------------------
    // Site Photos
    // -------------------------------------------------------------------------

    fun loadPhotos(projectId: String) {
        currentProjectId = projectId
        viewModelScope.launch {
            _photosState.value = SitePhotosUiState.Loading
            val result = repository.getSitePhotos(projectId)
            result.fold(
                onSuccess = { _photosState.value = SitePhotosUiState.Success(it) },
                onFailure = { _photosState.value = SitePhotosUiState.Error(it.message ?: "Failed to load photos") }
            )
        }
    }

    fun createSitePhoto(
        reportId: String?,
        taskId: String?,
        defectId: String?,
        photoUrl: String,
        caption: String?,
        locationTag: String?
    ) {
        viewModelScope.launch {
            val result = repository.createSitePhoto(
                projectId = currentProjectId,
                reportId = reportId,
                taskId = taskId,
                defectId = defectId,
                photoUrl = photoUrl,
                caption = caption,
                locationTag = locationTag
            )
            result.fold(
                onSuccess = { loadPhotos(currentProjectId) },
                onFailure = { loadPhotos(currentProjectId) }
            )
        }
    }

    // -------------------------------------------------------------------------
    // Legacy Site Issues (Backward Compatibility)
    // -------------------------------------------------------------------------

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
                onSuccess = { loadIssues(currentProjectId, currentStatusFilter) },
                onFailure = { loadIssues(currentProjectId, currentStatusFilter) }
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
                onSuccess = { loadIssues(currentProjectId, currentStatusFilter) },
                onFailure = { loadIssues(currentProjectId, currentStatusFilter) }
            )
        }
    }
}
