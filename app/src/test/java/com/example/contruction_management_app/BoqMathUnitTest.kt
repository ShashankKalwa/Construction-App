package com.example.contruction_management_app

import com.example.contruction_management_app.core.network.*
import com.example.contruction_management_app.domain.model.Boq
import com.example.contruction_management_app.domain.model.BoqItem
import com.example.contruction_management_app.domain.model.BoqStatus
import com.example.contruction_management_app.domain.repository.BoqRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.math.BigDecimal

class BoqMathUnitTest {

    private lateinit var fakeApiService: CmsApiService
    private lateinit var boqRepository: BoqRepository

    @Before
    fun setUp() {
        fakeApiService = object : FakeCmsApiService() {}
        boqRepository = BoqRepository(fakeApiService)
    }

    @Test
    fun testBoqItemTotalCalculation() {
        val qty = BigDecimal("850.00")
        val rate = BigDecimal("6500.00")
        val expectedTotal = BigDecimal("5525000.00")

        val item = BoqItem(
            id = "test_item",
            boqId = "boq_1",
            itemCode = "02.01",
            description = "Reinforced Cement Concrete M25",
            category = "CONCRETE",
            unit = "M3",
            estimatedQuantity = qty,
            unitRate = rate,
            totalAmount = qty.multiply(rate)
        )

        assertEquals(0, expectedTotal.compareTo(item.totalAmount))
        val digitsOnly = item.formatAmount().filter { it.isDigit() }
        assertTrue(digitsOnly.startsWith("5525000"))
    }

    @Test
    fun testBoqSumAggregation() {
        val item1 = BoqItem("i1", "b1", "01.01", "Earthwork", "EARTHWORK", "M3", BigDecimal("1500"), BigDecimal("350"), BigDecimal("525000"))
        val item2 = BoqItem("i2", "b1", "02.01", "RCC M25", "CONCRETE", "M3", BigDecimal("850"), BigDecimal("6500"), BigDecimal("5525000"))
        val item3 = BoqItem("i3", "b1", "03.01", "Rebar Steel", "STEEL", "KG", BigDecimal("68000"), BigDecimal("72"), BigDecimal("4896000"))
        val item4 = BoqItem("i4", "b1", "04.01", "Brickwork 1:6", "MASONRY", "M3", BigDecimal("420"), BigDecimal("4200"), BigDecimal("1764000"))

        val items = listOf(item1, item2, item3, item4)
        val grandTotal = items.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.totalAmount) }

        assertEquals(0, BigDecimal("12710000").compareTo(grandTotal))

        val boq = Boq(
            id = "b1",
            projectId = "prj_1",
            title = "Civil Structure Phase 1",
            version = 1,
            status = BoqStatus.DRAFT,
            totalAmount = grandTotal,
            createdBy = "Project Manager",
            approvedBy = null,
            items = items,
            createdAt = "2026-03-01T10:00:00Z"
        )

        assertEquals(4, boq.items.size)
        val digitsTotal = boq.formatTotalAmount().filter { it.isDigit() }
        assertTrue(digitsTotal.startsWith("12710000"))
    }

    @Test
    fun testBoqRepositoryOfflineFallbackWorkflow() = runBlocking {
        // Repository should load seeded demo BOQ offline
        val result = boqRepository.getBoqs("prj_commercial_tower_b")
        assertTrue(result.isSuccess)
        val boqs = result.getOrNull()
        assertNotNull(boqs)
        assertFalse(boqs!!.isEmpty())

        val firstBoq = boqs.first()
        assertEquals(BoqStatus.DRAFT, firstBoq.status)
        assertTrue(firstBoq.totalAmount > BigDecimal.ZERO)

        // Submit BOQ
        val submitResult = boqRepository.submitBoq(firstBoq.id)
        assertTrue(submitResult.isSuccess)
        assertEquals(BoqStatus.SUBMITTED, submitResult.getOrNull()?.status)

        // Approve BOQ
        val approveResult = boqRepository.decideBoq(firstBoq.id, BoqStatus.APPROVED)
        assertTrue(approveResult.isSuccess)
        assertEquals(BoqStatus.APPROVED, approveResult.getOrNull()?.status)
    }
}
