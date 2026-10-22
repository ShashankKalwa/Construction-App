package com.example.contruction_management_app

import com.example.contruction_management_app.core.network.*
import com.example.contruction_management_app.domain.repository.CalculationRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class CalculationEngineUnitTest {

    private lateinit var fakeApiService: CmsApiService
    private lateinit var calculationRepository: CalculationRepository

    @Before
    fun setUp() {
        fakeApiService = object : FakeCmsApiService() {}
        calculationRepository = CalculationRepository(fakeApiService)
    }

    @Test
    fun testOfflineConcreteCalculation() = runBlocking {
        // 10m x 5m x 0.15m slab with M20 concrete and 5% wastage
        val result = calculationRepository.calculateConcrete(
            lengthM = 10.0,
            widthM = 5.0,
            depthM = 0.15,
            grade = "M20",
            wastagePct = 5.0
        )

        assertTrue(result.isSuccess)
        val concrete = result.getOrNull()
        assertNotNull(concrete)
        assertEquals(7.50, concrete!!.wetVolumeM3, 0.01)
        assertEquals("M20", concrete.grade)
        assertEquals("1:1.5:3", concrete.mixRatio)
        assertTrue(concrete.dryVolumeM3 > 7.50)
        assertTrue(concrete.cementBags in 60.0..70.0)
        assertTrue(concrete.sandTonnes > 5.0)
        assertTrue(concrete.aggregateTonnes > 9.0)
    }

    @Test
    fun testOfflineSteelCalculation() = runBlocking {
        // 16mm rebar, 12m length, 48 bars, 3% wastage
        val result = calculationRepository.calculateSteel(
            diameterMm = 16.0,
            lengthM = 12.0,
            numberOfBars = 48,
            wastagePct = 3.0
        )

        assertTrue(result.isSuccess)
        val steel = result.getOrNull()
        assertNotNull(steel)
        assertEquals(16.0, steel!!.diameterMm, 0.01)
        assertEquals(576.0, steel.totalLengthM, 0.01)
        assertEquals(1.580, steel.unitWeightKgPerM, 0.005)
        assertTrue(steel.totalWeightKg > 900.0)
        assertEquals(12, steel.bundleCount) // 48 / 4 = 12 bundles
    }

    @Test
    fun testOfflineBrickworkCalculation() = runBlocking {
        // 30m length, 3m height, 0.23m thickness (9-inch), 4.5 sq.m openings
        val result = calculationRepository.calculateBrickwork(
            lengthM = 30.0,
            heightM = 3.0,
            thicknessM = 0.23,
            openingsAreaSqM = 4.5,
            mortarRatio = "1:6",
            wastagePct = 5.0
        )

        assertTrue(result.isSuccess)
        val brickwork = result.getOrNull()
        assertNotNull(brickwork)
        assertEquals(20.70, brickwork!!.grossWallVolumeM3, 0.01)
        assertTrue(brickwork.deductionsVolumeM3 > 1.0)
        assertTrue(brickwork.netWallVolumeM3 > 19.0)
        assertTrue(brickwork.brickCount > 10000)
        assertTrue(brickwork.cementBags > 15.0)
    }

    @Test
    fun testOfflineExcavationCalculation() = runBlocking {
        val result = calculationRepository.calculateExcavation(
            lengthM = 20.0,
            widthM = 12.0,
            depthM = 2.5,
            bulkingFactor = 1.25,
            compactionFactor = 0.85,
            truckCapacityM3 = 10.0
        )

        assertTrue(result.isSuccess)
        val excavation = result.getOrNull()
        assertNotNull(excavation)
        assertEquals(600.0, excavation!!.pitVolumeM3, 0.01)
        assertEquals(750.0, excavation.looseVolumeM3, 0.01)
        assertEquals(510.0, excavation.compactedBackfillM3, 0.01)
        assertEquals(75, excavation.truckTripsRequired) // 750 / 10 = 75
    }

    @Test
    fun testOfflinePlasterCalculation() = runBlocking {
        val result = calculationRepository.calculatePlaster(
            lengthM = 20.0,
            heightM = 3.0,
            thicknessMm = 12.0,
            openingsAreaSqM = 0.0,
            mortarRatio = "1:4",
            wastagePct = 5.0
        )

        assertTrue(result.isSuccess)
        val plaster = result.getOrNull()
        assertNotNull(plaster)
        assertEquals(60.0, plaster!!.surfaceAreaSqM, 0.01)
        assertEquals(0.72, plaster.wetVolumeM3, 0.01)
        assertTrue(plaster.dryVolumeM3 > 0.90)
        assertTrue(plaster.cementBags > 5.0)
    }

    @Test
    fun testOfflinePaintCalculation() = runBlocking {
        val result = calculationRepository.calculatePaint(
            lengthM = 5.0,
            widthM = 4.0,
            heightM = 3.0,
            openingsAreaSqM = 4.0,
            includeCeiling = true,
            numberOfCoats = 2,
            includePrimer = true,
            coverageRateSqMPerLiter = 10.0,
            wastagePct = 5.0
        )

        assertTrue(result.isSuccess)
        val paint = result.getOrNull()
        assertNotNull(paint)
        assertEquals(50.0, paint!!.wallAreaSqM, 0.01)
        assertEquals(20.0, paint.ceilingAreaSqM, 0.01)
        assertEquals(70.0, paint.totalAreaSqM, 0.01)
        assertTrue(paint.primerLiters > 0.0)
        assertTrue(paint.paintLiters > 0.0)
        assertTrue(paint.cans20L >= 0)
    }
}
