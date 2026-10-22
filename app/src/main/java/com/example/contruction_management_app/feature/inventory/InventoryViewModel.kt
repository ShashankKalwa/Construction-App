package com.example.contruction_management_app.feature.inventory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.contruction_management_app.core.network.NetworkClient
import com.example.contruction_management_app.domain.model.Material
import com.example.contruction_management_app.domain.model.MaterialCategory
import com.example.contruction_management_app.domain.model.MovementType
import com.example.contruction_management_app.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

sealed interface InventoryUiState {
    object Loading : InventoryUiState
    data class Success(
        val allMaterials: List<Material>,
        val filteredMaterials: List<Material>,
        val selectedCategory: MaterialCategory? = null
    ) : InventoryUiState
    data class Error(val message: String) : InventoryUiState
}

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InventoryRepository(NetworkClient.getApiService(application))

    private val _uiState = MutableStateFlow<InventoryUiState>(InventoryUiState.Loading)
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    private var currentProjectId: String = "prj_commercial_tower_b"
    private var allMaterialsList = listOf<Material>()
    private var currentCategory: MaterialCategory? = null

    fun loadMaterials(projectId: String) {
        currentProjectId = projectId
        viewModelScope.launch {
            _uiState.value = InventoryUiState.Loading
            val result = repository.getMaterials(projectId)
            result.fold(
                onSuccess = { list ->
                    allMaterialsList = list
                    applyFilter()
                },
                onFailure = {
                    _uiState.value = InventoryUiState.Error(it.message ?: "Failed to load materials")
                }
            )
        }
    }

    fun filterCategory(category: MaterialCategory?) {
        currentCategory = category
        applyFilter()
    }

    private fun applyFilter() {
        val filtered = if (currentCategory == null) {
            allMaterialsList
        } else {
            allMaterialsList.filter { it.category == currentCategory }
        }
        _uiState.value = InventoryUiState.Success(
            allMaterials = allMaterialsList,
            filteredMaterials = filtered,
            selectedCategory = currentCategory
        )
    }

    fun adjustStock(
        materialId: String,
        movementType: MovementType,
        quantity: BigDecimal,
        referenceId: String?,
        notes: String?
    ) {
        viewModelScope.launch {
            val result = repository.adjustStock(
                projectId = currentProjectId,
                materialId = materialId,
                movementType = movementType,
                quantity = quantity,
                referenceId = referenceId,
                notes = notes
            )
            result.fold(
                onSuccess = {
                    loadMaterials(currentProjectId)
                },
                onFailure = {
                    loadMaterials(currentProjectId)
                }
            )
        }
    }

    fun createMaterialRequest(
        materialId: String,
        quantity: BigDecimal,
        requiredByDate: String?,
        phaseOrTaskDesc: String?
    ) {
        viewModelScope.launch {
            repository.createMaterialRequest(
                projectId = currentProjectId,
                materialId = materialId,
                quantity = quantity,
                requiredByDate = requiredByDate,
                phaseOrTaskDesc = phaseOrTaskDesc
            )
        }
    }
}
