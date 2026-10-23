package com.example.contruction_management_app.feature.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.contruction_management_app.domain.model.DashboardSummary
import com.example.contruction_management_app.domain.model.Project
import com.example.contruction_management_app.domain.repository.ProjectRepository
import com.example.contruction_management_app.domain.repository.ProjectRepositoryImpl
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
            val activeId = repository.getActiveProjectId().firstOrNull()

            // 1. FAST CACHE-FIRST EMISSION: If cached projects exist, show them instantly without waiting for network!
            if (!forceRefresh && _uiState.value !is HomeDashboardUiState.Success) {
                val cached = repository.getCachedProjects()
                if (cached.isNotEmpty()) {
                    val activeProject = if (activeId != null) {
                        cached.find { it.id == activeId } ?: cached.firstOrNull()
                    } else {
                        cached.firstOrNull()
                    }
                    _uiState.value = HomeDashboardUiState.Success(
                        summary = repository.getCachedDashboardSummary(),
                        projects = cached,
                        activeProject = activeProject,
                        selectedCategory = currentCategory,
                        isRefreshing = true
                    )
                } else {
                    _uiState.value = HomeDashboardUiState.Loading
                }
            } else if (forceRefresh && _uiState.value is HomeDashboardUiState.Success) {
                val current = _uiState.value as HomeDashboardUiState.Success
                _uiState.value = current.copy(isRefreshing = true)
            }

            try {
                // 2. PARALLEL NETWORK FETCHING: Fetch projects and dashboard summary concurrently
                val (projectsResult, summaryResult) = coroutineScope {
                    val projectsDeferred = async { repository.getProjects() }
                    val summaryDeferred = async { repository.getDashboardSummary(activeId) }
                    Pair(projectsDeferred.await(), summaryDeferred.await())
                }

                val projects = projectsResult.getOrDefault(repository.getCachedProjects())
                val activeProject = if (activeId != null) {
                    projects.find { it.id == activeId } ?: projects.firstOrNull()
                } else {
                    projects.firstOrNull()
                }

                if (activeId == null && activeProject != null) {
                    repository.setActiveProjectId(activeProject.id)
                }

                val summary = summaryResult.getOrDefault(repository.getCachedDashboardSummary())

                _uiState.value = HomeDashboardUiState.Success(
                    summary = summary,
                    projects = projects,
                    activeProject = activeProject,
                    selectedCategory = currentCategory,
                    isRefreshing = false
                )
            } catch (e: Exception) {
                // If network fails, fallback smoothly to cached data
                val cached = repository.getCachedProjects()
                if (cached.isNotEmpty()) {
                    val activeProject = if (activeId != null) {
                        cached.find { it.id == activeId } ?: cached.firstOrNull()
                    } else {
                        cached.firstOrNull()
                    }
                    _uiState.value = HomeDashboardUiState.Success(
                        summary = repository.getCachedDashboardSummary(),
                        projects = cached,
                        activeProject = activeProject,
                        selectedCategory = currentCategory,
                        isRefreshing = false
                    )
                } else {
                    _uiState.value = HomeDashboardUiState.Error(e.message ?: "An unexpected error occurred")
                }
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
