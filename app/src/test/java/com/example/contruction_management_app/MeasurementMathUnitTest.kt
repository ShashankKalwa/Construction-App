package com.example.contruction_management_app

import com.example.contruction_management_app.core.network.*
import com.example.contruction_management_app.domain.model.Measurement
import com.example.contruction_management_app.domain.repository.MeasurementRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.math.BigDecimal
import java.math.RoundingMode

class MeasurementMathUnitTest {

    private lateinit var fakeApiService: CmsApiService
    private lateinit var measurementRepository: MeasurementRepository

    @Before
    fun setUp() {
        fakeApiService = object : FakeCmsApiService() {}
        measurementRepository = MeasurementRepository(fakeApiService)
    }

    @Test
    fun testMeasurementIS1200Formula() {
        // 4 units, L=3.50m, W=3.50m, D=2.20m, Deduction=0
        val nos = BigDecimal("4.00")
        val length = BigDecimal("3.50")
        val width = BigDecimal("3.50")
        val depth = BigDecimal("2.20")
        val deduction = BigDecimal.ZERO

        val gross = nos.multiply(length).multiply(width).multiply(depth)
        val net = gross.subtract(deduction).setScale(2, RoundingMode.HALF_UP)

        // 4 * 3.5 * 3.5 * 2.2 = 107.80 m3
        assertEquals(BigDecimal("107.80"), net)

        val m = Measurement(
            id = "test_m1",
            projectId = "prj_1",
            itemDescription = "Excavation Grid A1-A4",
            numberOfUnits = nos,
            lengthM = length,
            widthM = width,
            depthM = depth,
            deductionM3 = deduction,
            netQuantity = net,
            unit = "M3",
            locationDetails = "North Grid",
            measuredBy = "Site Engineer",
            measuredAt = "2026-03-02T11:30:00Z"
        )

        assertEquals("4 nos × 3.50m × 3.50m × 2.20m", m.formatDimensions())
        assertEquals("107.80 M3", m.formatNetQuantity())
    }

    @Test
    fun testMeasurementWithDeductions() {
        // Brickwork Wall: 2 units, L=12.00m, W=0.23m, D=3.20m, Deduction = 2.10 m3 (door/window openings)
        val nos = BigDecimal("2.00")
        val length = BigDecimal("12.00")
        val width = BigDecimal("0.23")
        val depth = BigDecimal("3.20")
        val deduction = BigDecimal("2.10")

        val gross = nos.multiply(length).multiply(width).multiply(depth) // 17.664
        val net = gross.subtract(deduction).setScale(2, RoundingMode.HALF_UP) // 15.564 -> 15.56

        assertEquals(BigDecimal("15.56"), net)

        val m = Measurement(
            id = "test_m2",
            projectId = "prj_1",
            itemDescription = "Brickwork Perimeter Wall",
            numberOfUnits = nos,
            lengthM = length,
            widthM = width,
            depthM = depth,
            deductionM3 = deduction,
            netQuantity = net,
            unit = "M3",
            locationDetails = "Corridor",
            measuredBy = "Site Supervisor",
            measuredAt = "2026-03-04T10:15:00Z"
        )

        assertEquals("15.56 M3", m.formatNetQuantity())
    }

    @Test
    fun testMeasurementRepositoryOfflineFallback() = runBlocking {
        val result = measurementRepository.getMeasurements("prj_commercial_tower_b")
        assertTrue(result.isSuccess)
        val list = result.getOrNull()
        assertNotNull(list)
        assertTrue(list!!.size >= 3)

        val first = list.first()
        assertTrue(first.netQuantity > BigDecimal.ZERO)
        assertTrue(first.lengthM > BigDecimal.ZERO)

        // Create new measurement offline
        val createResult = measurementRepository.createMeasurement(
            projectId = "prj_commercial_tower_b",
            itemDescription = "PCC 1:4:8 under Footings",
            numberOfUnits = BigDecimal("4.00"),
            lengthM = BigDecimal("3.70"),
            widthM = BigDecimal("3.70"),
            depthM = BigDecimal("0.10"),
            deductionM3 = BigDecimal.ZERO,
            unit = "M3",
            locationDetails = "Grid line A1"
        )

        assertTrue(createResult.isSuccess)
        val created = createResult.getOrNull()
        assertNotNull(created)
        assertEquals(BigDecimal("5.48"), created!!.netQuantity)
    }
}
