package com.example.contruction_management_app.domain.repository

import com.example.contruction_management_app.core.network.CmsApiService
import com.example.contruction_management_app.core.network.CreateMeasurementRequest
import com.example.contruction_management_app.core.network.MeasurementNetworkDto
import com.example.contruction_management_app.domain.model.Measurement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class MeasurementRepository(private val apiService: CmsApiService) {

    private val localMeasurementCache = ConcurrentHashMap<String, MutableList<Measurement>>()

    init {
        seedInitialDemoMeasurements("prj_commercial_tower_b")
    }

    private fun seedInitialDemoMeasurements(projectId: String) {
        val list = mutableListOf(
            Measurement(
                id = "m_1",
                projectId = projectId,
                boqItemId = "item_1",
                itemDescription = "Excavation Grid A1-A4 Foundation Pits",
                numberOfUnits = BigDecimal("4.00"),
                lengthM = BigDecimal("3.50"),
                widthM = BigDecimal("3.50"),
                depthM = BigDecimal("2.20"),
                deductionM3 = BigDecimal("0.00"),
                netQuantity = BigDecimal("107.80"),
                unit = "M3",
                locationDetails = "Grid line A1 to A4 north side",
                measuredBy = "Site Engineer",
                measuredAt = "2026-03-02T11:30:00Z"
            ),
            Measurement(
                id = "m_2",
                projectId = projectId,
                boqItemId = "item_2",
                itemDescription = "RCC Column C1 to C8 casting Ground Floor",
                numberOfUnits = BigDecimal("8.00"),
                lengthM = BigDecimal("0.45"),
                widthM = BigDecimal("0.60"),
                depthM = BigDecimal("3.60"),
                deductionM3 = BigDecimal("0.00"),
                netQuantity = BigDecimal("7.78"),
                unit = "M3",
                locationDetails = "Ground floor podium level",
                measuredBy = "Site Engineer",
                measuredAt = "2026-03-03T15:00:00Z"
            ),
            Measurement(
                id = "m_3",
                projectId = projectId,
                boqItemId = "item_4",
                itemDescription = "Brickwork Perimeter Wall GF with Door Opening Deductions",
                numberOfUnits = BigDecimal("2.00"),
                lengthM = BigDecimal("12.00"),
                widthM = BigDecimal("0.23"),
                depthM = BigDecimal("3.20"),
                deductionM3 = BigDecimal("2.10"),
                netQuantity = BigDecimal("15.57"),
                unit = "M3",
                locationDetails = "GF West Perimeter Corridor",
                measuredBy = "Site Supervisor",
                measuredAt = "2026-03-04T10:15:00Z"
            )
        )
        localMeasurementCache[projectId] = list
    }

    suspend fun getMeasurements(
        projectId: String,
        boqItemId: String? = null
    ): Result<List<Measurement>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProjectMeasurements(projectId, boqItemId)
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                val mapped = response.body()!!.map { mapMeasurementDto(it) }
                localMeasurementCache[projectId] = mapped.toMutableList()
                Result.success(mapped)
            } else {
                val cached = localMeasurementCache[projectId] ?: emptyList()
                val filtered = if (boqItemId != null) cached.filter { it.boqItemId == boqItemId } else cached
                Result.success(filtered)
            }
        } catch (_: Exception) {
            val cached = localMeasurementCache[projectId] ?: emptyList()
            val filtered = if (boqItemId != null) cached.filter { it.boqItemId == boqItemId } else cached
            Result.success(filtered)
        }
    }

    suspend fun createMeasurement(
        projectId: String,
        boqItemId: String? = null,
        itemDescription: String,
        numberOfUnits: BigDecimal,
        lengthM: BigDecimal,
        widthM: BigDecimal,
        depthM: BigDecimal,
        deductionM3: BigDecimal = BigDecimal.ZERO,
        unit: String = "M3",
        locationDetails: String? = null
    ): Result<Measurement> = withContext(Dispatchers.IO) {
        // Gross = units * length * width * depth
        val gross = numberOfUnits.multiply(lengthM).multiply(widthM).multiply(depthM)
        val net = gross.subtract(deductionM3).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)

        try {
            val response = apiService.createMeasurement(
                projectId,
                CreateMeasurementRequest(
                    boqItemId = boqItemId,
                    itemDescription = itemDescription,
                    numberOfUnits = numberOfUnits,
                    lengthM = lengthM,
                    widthM = widthM,
                    depthM = depthM,
                    deductionM3 = deductionM3,
                    unit = unit,
                    locationDetails = locationDetails
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val created = mapMeasurementDto(response.body()!!)
                localMeasurementCache.getOrPut(projectId) { mutableListOf() }.add(0, created)
                Result.success(created)
            } else {
                val local = Measurement(
                    id = "m_" + UUID.randomUUID().toString().take(8),
                    projectId = projectId,
                    boqItemId = boqItemId,
                    itemDescription = itemDescription,
                    numberOfUnits = numberOfUnits,
                    lengthM = lengthM,
                    widthM = widthM,
                    depthM = depthM,
                    deductionM3 = deductionM3,
                    netQuantity = net,
                    unit = unit,
                    locationDetails = locationDetails,
                    measuredBy = "Current User",
                    measuredAt = "Just now"
                )
                localMeasurementCache.getOrPut(projectId) { mutableListOf() }.add(0, local)
                Result.success(local)
            }
        } catch (_: Exception) {
            val local = Measurement(
                id = "m_" + UUID.randomUUID().toString().take(8),
                projectId = projectId,
                boqItemId = boqItemId,
                itemDescription = itemDescription,
                numberOfUnits = numberOfUnits,
                lengthM = lengthM,
                widthM = widthM,
                depthM = depthM,
                deductionM3 = deductionM3,
                netQuantity = net,
                unit = unit,
                locationDetails = locationDetails,
                measuredBy = "Current User",
                measuredAt = "Just now"
            )
            localMeasurementCache.getOrPut(projectId) { mutableListOf() }.add(0, local)
            Result.success(local)
        }
    }

    private fun mapMeasurementDto(dto: MeasurementNetworkDto): Measurement {
        return Measurement(
            id = dto.id,
            projectId = dto.projectId,
            boqItemId = dto.boqItemId,
            itemDescription = dto.itemDescription,
            numberOfUnits = dto.numberOfUnits,
            lengthM = dto.lengthM,
            widthM = dto.widthM,
            depthM = dto.depthM,
            deductionM3 = dto.deductionM3,
            netQuantity = dto.netQuantity,
            unit = dto.unit,
            locationDetails = dto.locationDetails,
            measuredBy = dto.measuredBy,
            measuredAt = dto.measuredAt
        )
    }
}
