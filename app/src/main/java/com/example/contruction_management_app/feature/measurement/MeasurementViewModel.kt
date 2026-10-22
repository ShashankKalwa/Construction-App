package com.example.contruction_management_app.feature.measurement

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.contruction_management_app.core.network.NetworkClient
import com.example.contruction_management_app.domain.model.Measurement
import com.example.contruction_management_app.domain.repository.MeasurementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

sealed interface MeasurementUiState {
    object Loading : MeasurementUiState
    data class Success(val measurements: List<Measurement>) : MeasurementUiState
    data class Error(val message: String) : MeasurementUiState
}

class MeasurementViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MeasurementRepository(NetworkClient.getApiService(application))

    private val _uiState = MutableStateFlow<MeasurementUiState>(MeasurementUiState.Loading)
    val uiState: StateFlow<MeasurementUiState> = _uiState.asStateFlow()

    private var currentProjectId: String = "prj_commercial_tower_b"

    fun loadMeasurements(projectId: String, boqItemId: String? = null) {
        currentProjectId = projectId
        viewModelScope.launch {
            _uiState.value = MeasurementUiState.Loading
            val result = repository.getMeasurements(projectId, boqItemId)
            result.fold(
                onSuccess = { _uiState.value = MeasurementUiState.Success(it) },
                onFailure = { _uiState.value = MeasurementUiState.Error(it.message ?: "Failed to load measurements") }
            )
        }
    }

    fun addMeasurement(
        itemDescription: String,
        numberOfUnits: BigDecimal,
        lengthM: BigDecimal,
        widthM: BigDecimal,
        depthM: BigDecimal,
        deductionM3: BigDecimal = BigDecimal.ZERO,
        unit: String = "M3",
        locationDetails: String? = null
    ) {
        viewModelScope.launch {
            val result = repository.createMeasurement(
                projectId = currentProjectId,
                itemDescription = itemDescription,
                numberOfUnits = numberOfUnits,
                lengthM = lengthM,
                widthM = widthM,
                depthM = depthM,
                deductionM3 = deductionM3,
                unit = unit,
                locationDetails = locationDetails
            )
            result.fold(
                onSuccess = {
                    loadMeasurements(currentProjectId)
                },
                onFailure = {
                    loadMeasurements(currentProjectId)
                }
            )
        }
    }
}
