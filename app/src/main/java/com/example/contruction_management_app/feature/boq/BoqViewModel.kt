package com.example.contruction_management_app.feature.boq

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.contruction_management_app.core.network.NetworkClient
import com.example.contruction_management_app.domain.model.Boq
import com.example.contruction_management_app.domain.model.BoqStatus
import com.example.contruction_management_app.domain.repository.BoqRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

sealed interface BoqListUiState {
    object Loading : BoqListUiState
    data class Success(val boqs: List<Boq>) : BoqListUiState
    data class Error(val message: String) : BoqListUiState
}

sealed interface BoqDetailUiState {
    object Loading : BoqDetailUiState
    data class Success(val boq: Boq) : BoqDetailUiState
    data class Error(val message: String) : BoqDetailUiState
}

class BoqViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BoqRepository(NetworkClient.getApiService(application))

    private val _listState = MutableStateFlow<BoqListUiState>(BoqListUiState.Loading)
    val listState: StateFlow<BoqListUiState> = _listState.asStateFlow()

    private val _detailState = MutableStateFlow<BoqDetailUiState>(BoqDetailUiState.Loading)
    val detailState: StateFlow<BoqDetailUiState> = _detailState.asStateFlow()

    private var currentProjectId: String? = null
    private var currentBoqId: String? = null

    fun loadBoqs(projectId: String) {
        currentProjectId = projectId
        viewModelScope.launch {
            _listState.value = BoqListUiState.Loading
            val result = repository.getBoqs(projectId)
            result.fold(
                onSuccess = { _listState.value = BoqListUiState.Success(it) },
                onFailure = { _listState.value = BoqListUiState.Error(it.message ?: "Failed to load BOQs") }
            )
        }
    }

    fun loadBoqDetail(boqId: String) {
        currentBoqId = boqId
        viewModelScope.launch {
            _detailState.value = BoqDetailUiState.Loading
            val result = repository.getBoqById(boqId)
            result.fold(
                onSuccess = { _detailState.value = BoqDetailUiState.Success(it) },
                onFailure = { _detailState.value = BoqDetailUiState.Error(it.message ?: "Failed to load BOQ details") }
            )
        }
    }

    fun createBoq(projectId: String, title: String, version: Int = 1) {
        viewModelScope.launch {
            val result = repository.createBoq(projectId, title, version)
            result.fold(
                onSuccess = {
                    loadBoqs(projectId)
                },
                onFailure = {
                    // silently refresh
                    loadBoqs(projectId)
                }
            )
        }
    }

    fun addBoqItem(
        boqId: String,
        itemCode: String,
        description: String,
        category: String,
        unit: String,
        estimatedQuantity: BigDecimal,
        unitRate: BigDecimal,
        calculationId: String? = null
    ) {
        viewModelScope.launch {
            val result = repository.addBoqItem(
                boqId = boqId,
                itemCode = itemCode,
                description = description,
                category = category,
                unit = unit,
                estimatedQuantity = estimatedQuantity,
                unitRate = unitRate,
                calculationId = calculationId
            )
            result.fold(
                onSuccess = {
                    loadBoqDetail(boqId)
                    currentProjectId?.let { loadBoqs(it) }
                },
                onFailure = {
                    loadBoqDetail(boqId)
                }
            )
        }
    }

    fun submitBoq(boqId: String) {
        viewModelScope.launch {
            val result = repository.submitBoq(boqId)
            result.fold(
                onSuccess = {
                    _detailState.value = BoqDetailUiState.Success(it)
                    currentProjectId?.let { pid -> loadBoqs(pid) }
                },
                onFailure = {
                    currentProjectId?.let { pid -> loadBoqs(pid) }
                }
            )
        }
    }

    fun decideBoq(boqId: String, status: BoqStatus, reason: String? = null) {
        viewModelScope.launch {
            val result = repository.decideBoq(boqId, status, reason)
            result.fold(
                onSuccess = {
                    _detailState.value = BoqDetailUiState.Success(it)
                    currentProjectId?.let { pid -> loadBoqs(pid) }
                },
                onFailure = {
                    currentProjectId?.let { pid -> loadBoqs(pid) }
                }
            )
        }
    }
}
