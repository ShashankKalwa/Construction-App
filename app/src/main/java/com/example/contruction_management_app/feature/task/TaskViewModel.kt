package com.example.contruction_management_app.feature.task

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.contruction_management_app.domain.model.Phase
import com.example.contruction_management_app.domain.model.Task
import com.example.contruction_management_app.domain.model.TaskStatus
import com.example.contruction_management_app.domain.repository.ProjectRepository
import com.example.contruction_management_app.domain.repository.ProjectRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PhasesUiState {
    data object Loading : PhasesUiState
    data class Success(
        val phases: List<Phase>,
        val filteredPhases: List<Phase>,
        val selectedStatus: TaskStatus? = null,
        val isUpdating: Boolean = false,
        val updateError: String? = null
    ) : PhasesUiState
    data class Error(val message: String) : PhasesUiState
}

class TaskViewModel(
    application: Application,
    private val repository: ProjectRepository = ProjectRepositoryImpl(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<PhasesUiState>(PhasesUiState.Loading)
    val uiState: StateFlow<PhasesUiState> = _uiState.asStateFlow()

    private var currentProjectId: String? = null
    private var allPhases: List<Phase> = emptyList()
    private var activeFilter: TaskStatus? = null

    fun loadPhases(projectId: String) {
        currentProjectId = projectId
        viewModelScope.launch {
            _uiState.value = PhasesUiState.Loading
            val result = repository.getPhasesWithTasks(projectId)
            result.onSuccess { phases ->
                allPhases = phases
                applyFilter()
            }.onFailure { error ->
                _uiState.value = PhasesUiState.Error(error.message ?: "Failed to load phases and tasks")
            }
        }
    }

    fun filterByStatus(status: TaskStatus?) {
        activeFilter = status
        applyFilter()
    }

    private fun applyFilter() {
        val filtered = if (activeFilter == null) {
            allPhases
        } else {
            allPhases.map { phase ->
                phase.copy(tasks = phase.tasks.filter { it.status == activeFilter })
            }
        }

        _uiState.value = PhasesUiState.Success(
            phases = allPhases,
            filteredPhases = filtered,
            selectedStatus = activeFilter
        )
    }

    fun updateTaskProgress(
        taskId: String,
        progressPct: Double,
        status: TaskStatus?,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is PhasesUiState.Success) {
                _uiState.value = currentState.copy(isUpdating = true, updateError = null)
            }

            val result = repository.updateTaskProgress(taskId, progressPct, status)
            result.onSuccess { updatedTask ->
                // Update local list optimistically
                allPhases = allPhases.map { phase ->
                    if (phase.id == updatedTask.phaseId) {
                        val updatedTasks = phase.tasks.map { if (it.id == taskId) updatedTask else it }
                        val newPhaseProgress = if (updatedTasks.isNotEmpty()) {
                            updatedTasks.map { it.progressPct }.average()
                        } else phase.progressPct
                        phase.copy(
                            tasks = updatedTasks,
                            progressPct = newPhaseProgress
                        )
                    } else {
                        phase
                    }
                }
                applyFilter()
                onSuccess()
            }.onFailure { error ->
                if (currentState is PhasesUiState.Success) {
                    _uiState.value = currentState.copy(
                        isUpdating = false,
                        updateError = error.message ?: "Failed to update progress"
                    )
                }
            }
        }
    }
}
