package com.example.contruction_management_app.domain.repository

import com.example.contruction_management_app.core.network.*
import com.example.contruction_management_app.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

class CalculationRepository(private val apiService: CmsApiService) {

    // =========================================================================
    // 1. Concrete Calculation (Client-side offline fallback + server sync)
    // =========================================================================
    suspend fun calculateConcrete(
        lengthM: Double,
        widthM: Double,
        depthM: Double,
        grade: String = "M20",
        wastagePct: Double = 5.0,
        elementType: String = "SLAB"
    ): Result<ConcreteResult> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.calculateConcrete(
                ConcreteNetworkRequest(lengthM, widthM, depthM, grade, wastagePct, elementType)
            )
            if (response.isSuccessful && response.body() != null) {
                val b = response.body()!!
                Result.success(
                    ConcreteResult(
                        b.wetVolumeM3, b.dryVolumeM3, b.grade, b.mixRatio,
                        b.cementBags, b.cementKg, b.sandTonnes, b.sandCft,
                        b.aggregateTonnes, b.aggregateCft, b.waterLiters, b.wastagePct
                    )
                )
            } else {
                Result.success(computeConcreteLocally(lengthM, widthM, depthM, grade, wastagePct))
            }
        } catch (_: Exception) {
            Result.success(computeConcreteLocally(lengthM, widthM, depthM, grade, wastagePct))
        }
    }

    private fun computeConcreteLocally(
        length: Double, width: Double, depth: Double,
        grade: String, wastage: Double
    ): ConcreteResult {
        val cementPart = 1.0
        val sandPart: Double
        val aggPart: Double
        val mixRatio: String

        when (grade.trim().uppercase(Locale.ROOT)) {
            "M10" -> { sandPart = 3.0; aggPart = 6.0; mixRatio = "1:3:6" }
            "M15" -> { sandPart = 2.0; aggPart = 4.0; mixRatio = "1:2:4" }
            "M25" -> { sandPart = 1.0; aggPart = 2.0; mixRatio = "1:1:2" }
            else -> { sandPart = 1.5; aggPart = 3.0; mixRatio = "1:1.5:3" }
        }
        val totalParts = cementPart + sandPart + aggPart
        val wetVolume = length * width * depth
        val dryVolume = wetVolume * 1.54 * (1.0 + (wastage / 100.0))

        val cementVol = dryVolume * (cementPart / totalParts)
        val cementKg = cementVol * 1440.0
        val cementBags = cementKg / 50.0

        val sandVol = dryVolume * (sandPart / totalParts)
        val sandTonnes = sandVol * 1.60
        val sandCft = sandVol * 35.3147

        val aggVol = dryVolume * (aggPart / totalParts)
        val aggTonnes = aggVol * 1.50
        val aggCft = aggVol * 35.3147

        val waterLiters = cementKg * 0.50

        return ConcreteResult(
            round2(wetVolume), round2(dryVolume), grade, mixRatio,
            round2(cementBags), round2(cementKg), round2(sandTonnes), round2(sandCft),
            round2(aggTonnes), round2(aggCft), round2(waterLiters), round2(wastage)
        )
    }

    // =========================================================================
    // 2. Steel Rebar Calculation
    // =========================================================================
    suspend fun calculateSteel(
        diameterMm: Double,
        lengthM: Double,
        numberOfBars: Int,
        wastagePct: Double = 3.0,
        elementType: String = "COLUMN"
    ): Result<SteelResult> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.calculateSteel(
                SteelNetworkRequest(diameterMm, lengthM, numberOfBars, wastagePct, elementType)
            )
            if (response.isSuccessful && response.body() != null) {
                val b = response.body()!!
                Result.success(
                    SteelResult(
                        b.diameterMm, b.lengthPerBarM, b.numberOfBars, b.totalLengthM,
                        b.unitWeightKgPerM, b.rawWeightKg, b.totalWeightKg, b.totalWeightTonnes,
                        b.bundleCount, b.wastagePct
                    )
                )
            } else {
                Result.success(computeSteelLocally(diameterMm, lengthM, numberOfBars, wastagePct))
            }
        } catch (_: Exception) {
            Result.success(computeSteelLocally(diameterMm, lengthM, numberOfBars, wastagePct))
        }
    }

    private fun computeSteelLocally(
        diameterMm: Double, lengthM: Double, numberOfBars: Int, wastagePct: Double
    ): SteelResult {
        val unitWeight = (diameterMm * diameterMm) / 162.0
        val totalLength = lengthM * numberOfBars
        val rawWeight = unitWeight * totalLength
        val totalWeight = rawWeight * (1.0 + (wastagePct / 100.0))
        val totalTonnes = totalWeight / 1000.0
        val barsPerBundle = when {
            diameterMm <= 8.0 -> 12
            diameterMm <= 10.0 -> 8
            diameterMm <= 12.0 -> 6
            diameterMm <= 16.0 -> 4
            diameterMm <= 20.0 -> 2
            else -> 1
        }
        val bundleCount = Math.ceil(numberOfBars.toDouble() / barsPerBundle).toInt()

        return SteelResult(
            round2(diameterMm), round2(lengthM), numberOfBars, round2(totalLength),
            round3(unitWeight), round2(rawWeight), round2(totalWeight), round3(totalTonnes),
            bundleCount, round2(wastagePct)
        )
    }

    // =========================================================================
    // 3. Brickwork Calculation
    // =========================================================================
    suspend fun calculateBrickwork(
        lengthM: Double,
        heightM: Double,
        thicknessM: Double,
        openingsAreaSqM: Double = 0.0,
        mortarRatio: String = "1:6",
        wastagePct: Double = 5.0
    ): Result<BrickworkResult> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.calculateBrickwork(
                BrickworkNetworkRequest(lengthM, heightM, thicknessM, openingsAreaSqM, mortarRatio, wastagePct)
            )
            if (response.isSuccessful && response.body() != null) {
                val b = response.body()!!
                Result.success(
                    BrickworkResult(
                        b.grossWallVolumeM3, b.deductionsVolumeM3, b.netWallVolumeM3,
                        b.brickCount, b.mortarDryVolumeM3, b.mortarRatio,
                        b.cementBags, b.cementKg, b.sandTonnes, b.sandCft, b.wastagePct
                    )
                )
            } else {
                Result.success(computeBrickworkLocally(lengthM, heightM, thicknessM, openingsAreaSqM, mortarRatio, wastagePct))
            }
        } catch (_: Exception) {
            Result.success(computeBrickworkLocally(lengthM, heightM, thicknessM, openingsAreaSqM, mortarRatio, wastagePct))
        }
    }

    private fun computeBrickworkLocally(
        length: Double, height: Double, thickness: Double,
        openings: Double, ratio: String, wastage: Double
    ): BrickworkResult {
        val grossVol = length * height * thickness
        val deductions = openings * thickness
        val netVol = Math.max(0.0, grossVol - deductions)
        val brickCount = Math.ceil(netVol * 500.0 * (1.0 + (wastage / 100.0)))
        val dryMortar = netVol * 0.30 * 1.33 * (1.0 + (wastage / 100.0))
        val sandPart = if (ratio == "1:4") 4.0 else 6.0
        val sumParts = 1.0 + sandPart
        val cementVol = dryMortar * (1.0 / sumParts)
        val cementKg = cementVol * 1440.0
        val cementBags = cementKg / 50.0
        val sandVol = dryMortar * (sandPart / sumParts)

        return BrickworkResult(
            round2(grossVol), round2(deductions), round2(netVol), brickCount,
            round2(dryMortar), ratio, round2(cementBags), round2(cementKg),
            round2(sandVol * 1.60), round2(sandVol * 35.3147), round2(wastage)
        )
    }

    // =========================================================================
    // 4. Excavation Calculation
    // =========================================================================
    suspend fun calculateExcavation(
        lengthM: Double,
        widthM: Double,
        depthM: Double,
        bulkingFactor: Double = 1.25,
        compactionFactor: Double = 0.85,
        truckCapacityM3: Double = 10.0
    ): Result<ExcavationResult> = withContext(Dispatchers.IO) {
        val pitVol = lengthM * widthM * depthM
        val looseVol = pitVol * bulkingFactor
        val compactedBackfill = pitVol * compactionFactor
        val trips = Math.ceil(looseVol / truckCapacityM3).toInt()

        Result.success(
            ExcavationResult(
                round2(pitVol), round2(looseVol), round2(bulkingFactor),
                round2(compactedBackfill), round2(compactionFactor),
                round2(truckCapacityM3), trips
            )
        )
    }

    // =========================================================================
    // 5. Plastering Calculation
    // =========================================================================
    suspend fun calculatePlaster(
        lengthM: Double,
        heightM: Double,
        thicknessMm: Double,
        openingsAreaSqM: Double = 0.0,
        mortarRatio: String = "1:4",
        wastagePct: Double = 5.0
    ): Result<PlasterResult> = withContext(Dispatchers.IO) {
        val area = Math.max(0.0, (lengthM * heightM) - openingsAreaSqM)
        val wetVol = area * (thicknessMm / 1000.0)
        val dryVol = wetVol * 1.30 * (1.0 + (wastagePct / 100.0))
        val sandPart = if (mortarRatio == "1:6") 6.0 else (if (mortarRatio == "1:3") 3.0 else 4.0)
        val sumParts = 1.0 + sandPart
        val cementVol = dryVol * (1.0 / sumParts)
        val cementKg = cementVol * 1440.0
        val cementBags = cementKg / 50.0
        val sandVol = dryVol * (sandPart / sumParts)

        Result.success(
            PlasterResult(
                round2(area), round2(wetVol), round2(dryVol), mortarRatio,
                round2(cementBags), round2(cementKg),
                round2(sandVol * 1.60), round2(sandVol * 35.3147), round2(wastagePct)
            )
        )
    }

    // =========================================================================
    // 6. Paint Estimator
    // =========================================================================
    suspend fun calculatePaint(
        lengthM: Double,
        widthM: Double,
        heightM: Double,
        openingsAreaSqM: Double = 0.0,
        includeCeiling: Boolean = true,
        numberOfCoats: Int = 2,
        includePrimer: Boolean = true,
        coverageRateSqMPerLiter: Double = 10.0,
        wastagePct: Double = 5.0
    ): Result<PaintResult> = withContext(Dispatchers.IO) {
        val wallArea = Math.max(0.0, (2.0 * (lengthM + widthM) * heightM) - openingsAreaSqM)
        val ceilingArea = if (includeCeiling) (lengthM * widthM) else 0.0
        val totalArea = wallArea + ceilingArea

        val primerLiters = if (includePrimer) (totalArea / 12.0) * (1.0 + (wastagePct / 100.0)) else 0.0
        val paintLiters = ((totalArea * numberOfCoats) / coverageRateSqMPerLiter) * (1.0 + (wastagePct / 100.0))

        val cans20L = (paintLiters / 20.0).toInt()
        val remAfter20 = paintLiters - (cans20L * 20.0)
        val cans4L = (remAfter20 / 4.0).toInt()
        val remAfter4 = remAfter20 - (cans4L * 4.0)
        val cans1L = Math.ceil(remAfter4).toInt()

        Result.success(
            PaintResult(
                round2(wallArea), round2(ceilingArea), round2(totalArea),
                numberOfCoats, round2(primerLiters), round2(paintLiters),
                cans20L, cans4L, cans1L, round2(wastagePct)
            )
        )
    }

    // =========================================================================
    // 7. Project History Persistence & Fetching
    // =========================================================================
    suspend fun saveCalculation(
        projectId: String,
        calculatorType: CalculatorType,
        title: String,
        elementType: String?,
        inputParameters: String,
        outputResults: String,
        primaryQuantity: Double,
        primaryUnit: String
    ): Result<CalculationHistoryItem> = withContext(Dispatchers.IO) {
        try {
            val req = SaveCalculationRequest(
                projectId, calculatorType.name, title, elementType,
                inputParameters, outputResults, primaryQuantity, primaryUnit
            )
            val resp = apiService.saveCalculation(projectId, req)
            if (resp.isSuccessful && resp.body() != null) {
                val b = resp.body()!!
                Result.success(
                    CalculationHistoryItem(
                        b.id, b.projectId,
                        CalculatorType.valueOf(b.calculatorType),
                        b.title, b.elementType, b.inputParameters,
                        b.outputResults, b.primaryQuantity, b.primaryUnit,
                        b.createdBy, b.createdAt
                    )
                )
            } else {
                // Local fallback record
                val localId = "local_calc_" + System.currentTimeMillis()
                Result.success(
                    CalculationHistoryItem(
                        localId, projectId, calculatorType, title, elementType,
                        inputParameters, outputResults, primaryQuantity, primaryUnit,
                        "offline_user", java.time.Instant.now().toString()
                    )
                )
            }
        } catch (_: Exception) {
            val localId = "local_calc_" + System.currentTimeMillis()
            Result.success(
                CalculationHistoryItem(
                    localId, projectId, calculatorType, title, elementType,
                    inputParameters, outputResults, primaryQuantity, primaryUnit,
                    "offline_user", java.time.Instant.now().toString()
                )
            )
        }
    }

    suspend fun getProjectCalculations(
        projectId: String,
        calculatorType: CalculatorType? = null
    ): Result<List<CalculationHistoryItem>> = withContext(Dispatchers.IO) {
        try {
            val resp = apiService.getProjectCalculations(projectId, calculatorType?.name)
            if (resp.isSuccessful && resp.body() != null) {
                val list = resp.body()!!.map { b ->
                    CalculationHistoryItem(
                        b.id, b.projectId,
                        try { CalculatorType.valueOf(b.calculatorType) } catch (_: Exception) { CalculatorType.CONCRETE },
                        b.title, b.elementType, b.inputParameters,
                        b.outputResults, b.primaryQuantity, b.primaryUnit,
                        b.createdBy, b.createdAt
                    )
                }
                Result.success(list)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun round2(v: Double): Double = BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).toDouble()
    private fun round3(v: Double): Double = BigDecimal.valueOf(v).setScale(3, RoundingMode.HALF_UP).toDouble()
}
