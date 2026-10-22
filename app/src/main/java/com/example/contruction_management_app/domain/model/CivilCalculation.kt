package com.example.contruction_management_app.domain.model

enum class CalculatorType(val displayName: String, val standardCode: String, val defaultUnit: String) {
    CONCRETE("Concrete Batching & Volume", "IS 456:2000", "m³"),
    STEEL("Steel Rebar Weight & Bundles", "IS 1786 / SP 34", "kg"),
    BRICKWORK("Brickwork Masonry & Mortar", "IS 2212 / CPWD", "bricks"),
    EXCAVATION("Excavation & Earthwork", "IS 1200 Part 1", "m³"),
    PLASTER("Plastering (Internal & External)", "IS 1661 / CPWD", "m²"),
    PAINT("Painting & Primer Estimator", "CPWD DSR 2023", "liters"),
    UNIT_CONVERT("Universal Construction Converter", "IS / Imperial / Metric", "")
}

data class ConcreteResult(
    val wetVolumeM3: Double,
    val dryVolumeM3: Double,
    val grade: String,
    val mixRatio: String,
    val cementBags: Double,
    val cementKg: Double,
    val sandTonnes: Double,
    val sandCft: Double,
    val aggregateTonnes: Double,
    val aggregateCft: Double,
    val waterLiters: Double,
    val wastagePct: Double
)

data class SteelResult(
    val diameterMm: Double,
    val lengthPerBarM: Double,
    val numberOfBars: Int,
    val totalLengthM: Double,
    val unitWeightKgPerM: Double,
    val rawWeightKg: Double,
    val totalWeightKg: Double,
    val totalWeightTonnes: Double,
    val bundleCount: Int,
    val wastagePct: Double
)

data class BrickworkResult(
    val grossWallVolumeM3: Double,
    val deductionsVolumeM3: Double,
    val netWallVolumeM3: Double,
    val brickCount: Double,
    val mortarDryVolumeM3: Double,
    val mortarRatio: String,
    val cementBags: Double,
    val cementKg: Double,
    val sandTonnes: Double,
    val sandCft: Double,
    val wastagePct: Double
)

data class ExcavationResult(
    val pitVolumeM3: Double,
    val looseVolumeM3: Double,
    val bulkingFactor: Double,
    val compactedBackfillM3: Double,
    val compactionFactor: Double,
    val truckCapacityM3: Double,
    val truckTripsRequired: Int
)

data class PlasterResult(
    val surfaceAreaSqM: Double,
    val wetVolumeM3: Double,
    val dryVolumeM3: Double,
    val mortarRatio: String,
    val cementBags: Double,
    val cementKg: Double,
    val sandTonnes: Double,
    val sandCft: Double,
    val wastagePct: Double
)

data class PaintResult(
    val wallAreaSqM: Double,
    val ceilingAreaSqM: Double,
    val totalAreaSqM: Double,
    val numberOfCoats: Int,
    val primerLiters: Double,
    val paintLiters: Double,
    val cans20L: Int,
    val cans4L: Int,
    val cans1L: Int,
    val wastagePct: Double
)

data class UnitConvertResult(
    val category: String,
    val fromValue: Double,
    val fromUnit: String,
    val toValue: Double,
    val toUnit: String,
    val formulaUsed: String,
    val allConversions: Map<String, Double>
)

data class CalculationHistoryItem(
    val id: String,
    val projectId: String,
    val calculatorType: CalculatorType,
    val title: String,
    val elementType: String?,
    val inputParameters: String,
    val outputResults: String,
    val primaryQuantity: Double,
    val primaryUnit: String,
    val createdBy: String?,
    val createdAt: String
)
