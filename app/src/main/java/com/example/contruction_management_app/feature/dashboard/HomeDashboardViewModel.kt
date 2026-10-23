package com.example.contruction_management_app.feature.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.contruction_management_app.domain.model.DashboardSummary
import com.example.contruction_management_app.domain.model.Project
import com.example.contruction_management_app.domain.repository.ProjectRepository
import com.example.contruction_management_app.domain.repository.ProjectRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

sealed interface HomeDashboardUiState {
    data object Loading : HomeDashboardUiState
    data class Success(
        val summary: DashboardSummary,
        val projects: List<Project>,
        val activeProject: Project?,
        val selectedCategory: String = "all",
        val isRefreshing: Boolean = false
    ) : HomeDashboardUiState
    data class Error(val message: String) : HomeDashboardUiState
}

class HomeDashboardViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: ProjectRepository = ProjectRepositoryImpl(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<HomeDashboardUiState>(HomeDashboardUiState.Loading)
    val uiState: StateFlow<HomeDashboardUiState> = _uiState.asStateFlow()

    private var currentCategory: String = "all"

    init {
        loadDashboard()
    }

    fun loadDashboard(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!forceRefresh && _uiState.value !is HomeDashboardUiState.Success) {
                _uiState.value = HomeDashboardUiState.Loading
            } else if (forceRefresh && _uiState.value is HomeDashboardUiState.Success) {
                val current = _uiState.value as HomeDashboardUiState.Success
                _uiState.value = current.copy(isRefreshing = true)
            }

            try {
                val activeId = repository.getActiveProjectId().firstOrNull()
                val projectsResult = repository.getProjects()
                val projects = projectsResult.getOrDefault(emptyList())

                val activeProject = if (activeId != null) {
                    projects.find { it.id == activeId } ?: projects.firstOrNull()
                } else {
                    projects.firstOrNull()
                }

                // If no active project set yet, set the first one
                if (activeId == null && activeProject != null) {
                    repository.setActiveProjectId(activeProject.id)
                }

                val summaryResult = repository.getDashboardSummary(activeProject?.id)
                val summary = summaryResult.getOrNull()

                if (summary != null) {
                    _uiState.value = HomeDashboardUiState.Success(
                        summary = summary,
                        projects = projects,
                        activeProject = activeProject,
                        selectedCategory = currentCategory,
                        isRefreshing = false
                    )
                } else {
                    val errorMsg = summaryResult.exceptionOrNull()?.message ?: "Failed to load dashboard summary"
                    _uiState.value = HomeDashboardUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = HomeDashboardUiState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun selectProject(projectId: String) {
        viewModelScope.launch {
            repository.setActiveProjectId(projectId)
            loadDashboard(forceRefresh = true)
        }
    }

    fun selectCategory(category: String) {
        currentCategory = category
        val current = _uiState.value
        if (current is HomeDashboardUiState.Success) {
            _uiState.value = current.copy(selectedCategory = category)
        }
    }
}
