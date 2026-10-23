package com.example.contruction_management_app.feature.project

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.contruction_management_app.domain.model.Project
import com.example.contruction_management_app.domain.model.ProjectDetail
import com.example.contruction_management_app.domain.repository.ProjectRepository
import com.example.contruction_management_app.domain.repository.ProjectRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

sealed interface ProjectListUiState {
    data object Loading : ProjectListUiState
    data class Success(
        val projects: List<Project>,
        val filteredProjects: List<Project>,
        val searchQuery: String = "",
        val statusFilter: String = "ALL",
        val isCreating: Boolean = false
    ) : ProjectListUiState
    data class Error(val message: String) : ProjectListUiState
}

sealed interface ProjectDetailUiState {
    data object Loading : ProjectDetailUiState
    data class Success(
        val project: ProjectDetail,
        val selectedTabIndex: Int = 0
    ) : ProjectDetailUiState
    data class Error(val message: String) : ProjectDetailUiState
}

class ProjectViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: ProjectRepository = ProjectRepositoryImpl(application)
) : AndroidViewModel(application) {

    private val _listState = MutableStateFlow<ProjectListUiState>(ProjectListUiState.Loading)
    val listState: StateFlow<ProjectListUiState> = _listState.asStateFlow()

    private val _detailState = MutableStateFlow<ProjectDetailUiState>(ProjectDetailUiState.Loading)
    val detailState: StateFlow<ProjectDetailUiState> = _detailState.asStateFlow()

    private var allProjects: List<Project> = emptyList()
    private var currentQuery: String = ""
    private var currentFilter: String = "ALL"

    init {
        loadProjects()
    }

    fun loadProjects() {
        viewModelScope.launch {
            _listState.value = ProjectListUiState.Loading
            val result = repository.getProjects()
            result.onSuccess { projects ->
                allProjects = projects
                applyFilters()
            }.onFailure { error ->
                _listState.value = ProjectListUiState.Error(error.message ?: "Failed to load projects")
            }
        }
    }

    fun setSearchQuery(query: String) {
        currentQuery = query
        applyFilters()
    }

    fun setStatusFilter(filter: String) {
        currentFilter = filter
        applyFilters()
    }

    private fun applyFilters() {
        var filtered = allProjects
        if (currentQuery.isNotBlank()) {
            val q = currentQuery.trim().lowercase()
            filtered = filtered.filter {
                it.name.lowercase().contains(q) ||
                it.code.lowercase().contains(q) ||
                it.location.lowercase().contains(q)
            }
        }
        if (currentFilter != "ALL") {
            filtered = filtered.filter { it.status.equals(currentFilter, ignoreCase = true) }
        }

        _listState.value = ProjectListUiState.Success(
            projects = allProjects,
            filteredProjects = filtered,
            searchQuery = currentQuery,
            statusFilter = currentFilter
        )
    }

    fun createProject(
        name: String,
        code: String,
        location: String,
        budgetInr: BigDecimal,
        onSuccess: (Project) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val current = _listState.value
            if (current is ProjectListUiState.Success) {
                _listState.value = current.copy(isCreating = true)
            }

            val result = repository.createProject(name, code, location, budgetInr)
            result.onSuccess { newProject ->
                allProjects = listOf(newProject) + allProjects
                applyFilters()
                onSuccess(newProject)
            }.onFailure { error ->
                if (current is ProjectListUiState.Success) {
                    _listState.value = current.copy(isCreating = false)
                }
                onError(error.message ?: "Failed to create project")
            }
        }
    }

    fun loadProjectDetail(projectId: String) {
        viewModelScope.launch {
            _detailState.value = ProjectDetailUiState.Loading
            val result = repository.getProjectDetail(projectId)
            result.onSuccess { detail ->
                _detailState.value = ProjectDetailUiState.Success(project = detail, selectedTabIndex = 0)
            }.onFailure { error ->
                _detailState.value = ProjectDetailUiState.Error(error.message ?: "Failed to load project details")
            }
        }
    }

    fun setDetailTab(tabIndex: Int) {
        val current = _detailState.value
        if (current is ProjectDetailUiState.Success) {
            _detailState.value = current.copy(selectedTabIndex = tabIndex)
        }
    }
}
