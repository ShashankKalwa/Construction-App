package com.example.contruction_management_app.feature.inventory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.contruction_management_app.core.network.NetworkClient
import com.example.contruction_management_app.domain.model.*
import com.example.contruction_management_app.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

sealed interface MaterialRequestsUiState {
    object Loading : MaterialRequestsUiState
    data class Success(
        val allRequests: List<MaterialRequest>,
        val filteredRequests: List<MaterialRequest>,
        val selectedStatus: MaterialRequestStatus? = null
    ) : MaterialRequestsUiState
    data class Error(val message: String) : MaterialRequestsUiState
}

sealed interface StockLedgerUiState {
    object Loading : StockLedgerUiState
    data class Success(val movements: List<StockMovement>) : StockLedgerUiState
    data class Error(val message: String) : StockLedgerUiState
}

sealed interface LowStockUiState {
    object Loading : LowStockUiState
    data class Success(val lowStockMaterials: List<Material>) : LowStockUiState
    data class Error(val message: String) : LowStockUiState
}

class InventoryViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: InventoryRepository = InventoryRepository(NetworkClient.getApiService(application))
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<InventoryUiState>(InventoryUiState.Loading)
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    private val _requestsState = MutableStateFlow<MaterialRequestsUiState>(MaterialRequestsUiState.Loading)
    val requestsState: StateFlow<MaterialRequestsUiState> = _requestsState.asStateFlow()

    private val _ledgerState = MutableStateFlow<StockLedgerUiState>(StockLedgerUiState.Loading)
    val ledgerState: StateFlow<StockLedgerUiState> = _ledgerState.asStateFlow()

    private val _lowStockState = MutableStateFlow<LowStockUiState>(LowStockUiState.Loading)
    val lowStockState: StateFlow<LowStockUiState> = _lowStockState.asStateFlow()

    private val _actionMessage = MutableSharedFlow<String>()
    val actionMessage: SharedFlow<String> = _actionMessage.asSharedFlow()

    private var currentProjectId: String = "prj_commercial_tower_b"
    private var allMaterialsList = listOf<Material>()
    private var currentCategory: MaterialCategory? = null

    private var allRequestsList = listOf<MaterialRequest>()
    private var currentRequestStatus: MaterialRequestStatus? = null

    fun loadAll(projectId: String) {
        currentProjectId = projectId
        loadMaterials(projectId)
        loadRequests(projectId)
        loadLedger(projectId)
        loadLowStock(projectId)
    }

    // -------------------------------------------------------------------------
    // Materials & Stock
    // -------------------------------------------------------------------------

    fun loadMaterials(projectId: String = currentProjectId) {
        currentProjectId = projectId
        viewModelScope.launch {
            _uiState.value = InventoryUiState.Loading
            val result = repository.getMaterials(projectId)
            result.fold(
                onSuccess = { list ->
                    allMaterialsList = list
                    applyCategoryFilter()
                },
                onFailure = {
                    _uiState.value = InventoryUiState.Error(it.message ?: "Failed to load materials")
                }
            )
        }
    }

    fun filterCategory(category: MaterialCategory?) {
        currentCategory = category
        applyCategoryFilter()
    }

    private fun applyCategoryFilter() {
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

    fun loadLowStock(projectId: String = currentProjectId) {
        viewModelScope.launch {
            _lowStockState.value = LowStockUiState.Loading
            val result = repository.getLowStockMaterials(projectId)
            result.fold(
                onSuccess = { list ->
                    _lowStockState.value = LowStockUiState.Success(list)
                },
                onFailure = {
                    _lowStockState.value = LowStockUiState.Error(it.message ?: "Failed to load low-stock items")
                }
            )
        }
    }

    fun adjustStock(
        materialId: String,
        movementType: MovementType,
        quantity: BigDecimal,
        referenceId: String?,
        notes: String?,
        onComplete: ((Boolean, String) -> Unit)? = null
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
                    val msg = "Stock adjusted successfully (${it.name})"
                    _actionMessage.emit(msg)
                    onComplete?.invoke(true, msg)
                    loadMaterials(currentProjectId)
                    loadLedger(currentProjectId)
                    loadLowStock(currentProjectId)
                },
                onFailure = {
                    val err = it.message ?: "Failed to adjust stock"
                    _actionMessage.emit(err)
                    onComplete?.invoke(false, err)
                }
            )
        }
    }

    fun returnStock(
        materialId: String,
        quantity: BigDecimal,
        referenceId: String?,
        notes: String?,
        onComplete: ((Boolean, String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = repository.returnStock(
                projectId = currentProjectId,
                materialId = materialId,
                quantity = quantity,
                referenceId = referenceId,
                notes = notes
            )
            result.fold(
                onSuccess = {
                    val msg = "Stock returned successfully (${it.name})"
                    _actionMessage.emit(msg)
                    onComplete?.invoke(true, msg)
                    loadMaterials(currentProjectId)
                    loadLedger(currentProjectId)
                    loadLowStock(currentProjectId)
                },
                onFailure = {
                    val err = it.message ?: "Failed to return stock"
                    _actionMessage.emit(err)
                    onComplete?.invoke(false, err)
                }
            )
        }
    }

    // -------------------------------------------------------------------------
    // Material Requests (Requisitions)
    // -------------------------------------------------------------------------

    fun loadRequests(projectId: String = currentProjectId) {
        currentProjectId = projectId
        viewModelScope.launch {
            _requestsState.value = MaterialRequestsUiState.Loading
            val result = repository.getMaterialRequests(projectId)
            result.fold(
                onSuccess = { list ->
                    allRequestsList = list
                    applyRequestFilter()
                },
                onFailure = {
                    _requestsState.value = MaterialRequestsUiState.Error(it.message ?: "Failed to load requests")
                }
            )
        }
    }

    fun filterRequests(status: MaterialRequestStatus?) {
        currentRequestStatus = status
        applyRequestFilter()
    }

    private fun applyRequestFilter() {
        val filtered = if (currentRequestStatus == null) {
            allRequestsList
        } else {
            allRequestsList.filter { it.status == currentRequestStatus }
        }
        _requestsState.value = MaterialRequestsUiState.Success(
            allRequests = allRequestsList,
            filteredRequests = filtered,
            selectedStatus = currentRequestStatus
        )
    }

    fun createMaterialRequest(
        materialId: String,
        quantity: BigDecimal,
        requiredByDate: String?,
        phaseOrTaskDesc: String?,
        onComplete: ((Boolean, String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = repository.createMaterialRequest(
                projectId = currentProjectId,
                materialId = materialId,
                quantity = quantity,
                requiredByDate = requiredByDate,
                phaseOrTaskDesc = phaseOrTaskDesc
            )
            result.fold(
                onSuccess = {
                    val msg = "Material request submitted (${it.materialName})"
                    _actionMessage.emit(msg)
                    onComplete?.invoke(true, msg)
                    loadRequests(currentProjectId)
                },
                onFailure = {
                    val err = it.message ?: "Failed to submit request"
                    _actionMessage.emit(err)
                    onComplete?.invoke(false, err)
                }
            )
        }
    }

    fun approveRequest(
        requestId: String,
        onComplete: ((Boolean, String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = repository.approveMaterialRequest(currentProjectId, requestId)
            result.fold(
                onSuccess = {
                    val msg = "Material request approved (${it.materialName})"
                    _actionMessage.emit(msg)
                    onComplete?.invoke(true, msg)
                    loadRequests(currentProjectId)
                },
                onFailure = {
                    val err = it.message ?: "Approval failed (Check dual-control rules)"
                    _actionMessage.emit(err)
                    onComplete?.invoke(false, err)
                }
            )
        }
    }

    fun rejectRequest(
        requestId: String,
        reason: String,
        onComplete: ((Boolean, String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = repository.rejectMaterialRequest(currentProjectId, requestId, reason)
            result.fold(
                onSuccess = {
                    val msg = "Material request rejected"
                    _actionMessage.emit(msg)
                    onComplete?.invoke(true, msg)
                    loadRequests(currentProjectId)
                },
                onFailure = {
                    val err = it.message ?: "Rejection failed"
                    _actionMessage.emit(err)
                    onComplete?.invoke(false, err)
                }
            )
        }
    }

    fun issueStock(
        requestId: String,
        notes: String?,
        onComplete: ((Boolean, String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = repository.issueMaterialRequest(currentProjectId, requestId, notes)
            result.fold(
                onSuccess = {
                    val msg = "Stock issued successfully for ${it.materialName}"
                    _actionMessage.emit(msg)
                    onComplete?.invoke(true, msg)
                    loadRequests(currentProjectId)
                    loadMaterials(currentProjectId)
                    loadLedger(currentProjectId)
                    loadLowStock(currentProjectId)
                },
                onFailure = {
                    val err = it.message ?: "Issuance failed (check available stock)"
                    _actionMessage.emit(err)
                    onComplete?.invoke(false, err)
                }
            )
        }
    }

    // -------------------------------------------------------------------------
    // Stock Movement Ledger
    // -------------------------------------------------------------------------

    fun loadLedger(projectId: String = currentProjectId) {
        currentProjectId = projectId
        viewModelScope.launch {
            _ledgerState.value = StockLedgerUiState.Loading
            val result = repository.getStockLedger(projectId)
            result.fold(
                onSuccess = { list ->
                    _ledgerState.value = StockLedgerUiState.Success(list)
                },
                onFailure = {
                    _ledgerState.value = StockLedgerUiState.Error(it.message ?: "Failed to load stock ledger")
                }
            )
        }
    }
}
