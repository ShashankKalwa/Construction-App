package com.example.contruction_management_app.feature.tools

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.contruction_management_app.core.network.NetworkClient
import com.example.contruction_management_app.domain.model.*
import com.example.contruction_management_app.domain.repository.CalculationRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CalculationRepository(NetworkClient.getApiService(application))
    private val gson = Gson()

    private val _calculatorType = MutableStateFlow(CalculatorType.CONCRETE)
    val calculatorType: StateFlow<CalculatorType> = _calculatorType.asStateFlow()

    private val _projectId = MutableStateFlow<String?>("prj_green_valley_residency")
    val projectId: StateFlow<String?> = _projectId.asStateFlow()

    private val _activeResult = MutableStateFlow<Any?>(null)
    val activeResult: StateFlow<Any?> = _activeResult.asStateFlow()

    private val _historyList = MutableStateFlow<List<CalculationHistoryItem>>(emptyList())
    val historyList: StateFlow<List<CalculationHistoryItem>> = _historyList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun setCalculatorType(type: CalculatorType) {
        _calculatorType.value = type
        _activeResult.value = null
        loadHistory()
    }

    fun setProjectId(id: String?) {
        if (!id.isNullOrBlank()) {
            _projectId.value = id
            loadHistory()
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun calculateConcrete(
        length: Double, width: Double, depth: Double,
        grade: String, wastage: Double, elementType: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.calculateConcrete(length, width, depth, grade, wastage, elementType)
            res.onSuccess {
                _activeResult.value = it
            }.onFailure {
                _userMessage.value = "Concrete calculation failed: ${it.message}"
            }
            _isLoading.value = false
        }
    }

    fun calculateSteel(
        diameter: Double, length: Double, count: Int,
        wastage: Double, elementType: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.calculateSteel(diameter, length, count, wastage, elementType)
            res.onSuccess {
                _activeResult.value = it
            }.onFailure {
                _userMessage.value = "Steel calculation failed: ${it.message}"
            }
            _isLoading.value = false
        }
    }

    fun calculateBrickwork(
        length: Double, height: Double, thickness: Double,
        openings: Double, ratio: String, wastage: Double
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.calculateBrickwork(length, height, thickness, openings, ratio, wastage)
            res.onSuccess {
                _activeResult.value = it
            }.onFailure {
                _userMessage.value = "Brickwork calculation failed: ${it.message}"
            }
            _isLoading.value = false
        }
    }

    fun calculateExcavation(
        length: Double, width: Double, depth: Double,
        bulking: Double, compaction: Double, truckCap: Double
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.calculateExcavation(length, width, depth, bulking, compaction, truckCap)
            res.onSuccess {
                _activeResult.value = it
            }.onFailure {
                _userMessage.value = "Excavation calculation failed: ${it.message}"
            }
            _isLoading.value = false
        }
    }

    fun calculatePlaster(
        length: Double, height: Double, thickness: Double,
        openings: Double, ratio: String, wastage: Double
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.calculatePlaster(length, height, thickness, openings, ratio, wastage)
            res.onSuccess {
                _activeResult.value = it
            }.onFailure {
                _userMessage.value = "Plaster calculation failed: ${it.message}"
            }
            _isLoading.value = false
        }
    }

    fun calculatePaint(
        length: Double, width: Double, height: Double,
        openings: Double, incCeiling: Boolean, coats: Int,
        incPrimer: Boolean, coverage: Double, wastage: Double
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.calculatePaint(
                length, width, height, openings, incCeiling, coats, incPrimer, coverage, wastage
            )
            res.onSuccess {
                _activeResult.value = it
            }.onFailure {
                _userMessage.value = "Paint calculation failed: ${it.message}"
            }
            _isLoading.value = false
        }
    }

    fun saveCurrentCalculation(title: String, elementType: String?) {
        val currentRes = _activeResult.value ?: return
        val projId = _projectId.value ?: "prj_green_valley_residency"
        val type = _calculatorType.value

        var primaryQty = 0.0
        var primaryUnit = type.defaultUnit

        when (currentRes) {
            is ConcreteResult -> {
                primaryQty = currentRes.wetVolumeM3
                primaryUnit = "m³"
            }
            is SteelResult -> {
                primaryQty = currentRes.totalWeightKg
                primaryUnit = "kg"
            }
            is BrickworkResult -> {
                primaryQty = currentRes.brickCount
                primaryUnit = "bricks"
            }
            is ExcavationResult -> {
                primaryQty = currentRes.pitVolumeM3
                primaryUnit = "m³"
            }
            is PlasterResult -> {
                primaryQty = currentRes.surfaceAreaSqM
                primaryUnit = "m²"
            }
            is PaintResult -> {
                primaryQty = currentRes.paintLiters
                primaryUnit = "liters"
            }
        }

        val outputJson = gson.toJson(currentRes)
        val inputJson = "{\"type\":\"${type.name}\",\"timestamp\":\"${System.currentTimeMillis()}\"}"

        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.saveCalculation(
                projId, type, title, elementType, inputJson, outputJson, primaryQty, primaryUnit
            )
            res.onSuccess {
                _userMessage.value = "Calculation saved to project record!"
                loadHistory()
            }.onFailure {
                _userMessage.value = "Saved locally (offline mode): ${it.message}"
            }
            _isLoading.value = false
        }
    }

    fun loadHistory() {
        val projId = _projectId.value ?: return
        viewModelScope.launch {
            val res = repository.getProjectCalculations(projId, _calculatorType.value)
            res.onSuccess {
                _historyList.value = it
            }
        }
    }
}
