package com.example.contruction_management_app

import com.example.contruction_management_app.domain.model.*
import com.example.contruction_management_app.domain.repository.FinanceRepository
import com.example.contruction_management_app.domain.repository.InventoryRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode

class CostCalculationUnitTest {

    private lateinit var fakeApiService: FakeCmsApiService
    private lateinit var inventoryRepository: InventoryRepository
    private lateinit var financeRepository: FinanceRepository

    @Before
    fun setUp() {
        fakeApiService = object : FakeCmsApiService() {}
        inventoryRepository = InventoryRepository(fakeApiService)
        financeRepository = FinanceRepository(fakeApiService)
    }

    @Test
    fun testContractorBillNetPayableMath() {
        // Gross: ₹5,00,000 | Retention 5% = ₹25,000 | TDS 2% = ₹10,000 | Net: ₹4,65,000
        val gross = BigDecimal("500000.00")
        val retentionPct = BigDecimal("5.00")
        val tdsPct = BigDecimal("2.00")

        val retentionAmount = gross.multiply(retentionPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
        val tdsAmount = gross.multiply(tdsPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
        val netPayable = gross.subtract(retentionAmount).subtract(tdsAmount)

        assertEquals(0, BigDecimal("25000.00").compareTo(retentionAmount))
        assertEquals(0, BigDecimal("10000.00").compareTo(tdsAmount))
        assertEquals(0, BigDecimal("465000.00").compareTo(netPayable))
    }

    @Test
    fun testLowStockWarningThreshold() {
        val lowStockMaterial = Material(
            id = "mat_1",
            projectId = "prj_1",
            code = "MAT-STEEL-12",
            name = "12mm TMT Rebar",
            category = MaterialCategory.STEEL,
            unit = "MT",
            currentStock = BigDecimal("3.50"),
            minStockThreshold = BigDecimal("5.00"),
            unitCost = BigDecimal("65000.00"),
            isLowStock = true
        )

        val normalMaterial = Material(
            id = "mat_2",
            projectId = "prj_1",
            code = "MAT-CEM-OPC",
            name = "OPC 53 Cement",
            category = MaterialCategory.CEMENT,
            unit = "Bags",
            currentStock = BigDecimal("450.00"),
            minStockThreshold = BigDecimal("100.00"),
            unitCost = BigDecimal("380.00"),
            isLowStock = false
        )

        assertTrue(lowStockMaterial.isLowStock)
        assertTrue(lowStockMaterial.currentStock < lowStockMaterial.minStockThreshold)
        assertFalse(normalMaterial.isLowStock)
        assertFalse(normalMaterial.currentStock < normalMaterial.minStockThreshold)
    }

    @Test
    fun testInventoryRepositoryOfflineIssuanceAndDeduction() = runBlocking {
        val projectId = "prj_commercial_tower_b"
        val initialResult = inventoryRepository.getMaterials(projectId)
        assertTrue(initialResult.isSuccess)
        val materials = initialResult.getOrNull()!!
        assertFalse(materials.isEmpty())

        val cement = materials.first { it.code == "MAT-CEM-OPC" }
        val beforeStock = cement.currentStock

        // Issue 50 bags to site
        val adjustResult = inventoryRepository.adjustStock(
            projectId = projectId,
            materialId = cement.id,
            movementType = MovementType.OUTWARD_ISSUE,
            quantity = BigDecimal("50.00"),
            referenceId = "ISSUE-SLIP-01",
            notes = "Pouring slab"
        )
        assertTrue(adjustResult.isSuccess)
        val updated = adjustResult.getOrNull()!!
        assertEquals(0, beforeStock.subtract(BigDecimal("50.00")).compareTo(updated.currentStock))

        // Attempt to issue 1,000,000 bags (insufficient stock check)
        val failResult = inventoryRepository.adjustStock(
            projectId = projectId,
            materialId = cement.id,
            movementType = MovementType.OUTWARD_ISSUE,
            quantity = BigDecimal("1000000.00")
        )
        assertTrue(failResult.isFailure)
        assertTrue(failResult.exceptionOrNull()?.message?.contains("Insufficient stock") == true)
    }

    @Test
    fun testFinanceRepositoryOfflineStepperWorkflow() = runBlocking {
        val projectId = "prj_commercial_tower_b"
        val expensesResult = financeRepository.getExpenses(projectId)
        assertTrue(expensesResult.isSuccess)
        val expenses = expensesResult.getOrNull()!!
        assertFalse(expenses.isEmpty())

        // Find submitted expense and advance it to VERIFIED
        val submitted = expenses.first { it.status == ExpenseStatus.SUBMITTED }
        val verifiedResult = financeRepository.updateExpenseStatus(projectId, submitted.id, ExpenseStatus.VERIFIED)
        assertTrue(verifiedResult.isSuccess)
        assertEquals(ExpenseStatus.VERIFIED, verifiedResult.getOrNull()?.status)

        // Find contractor bills and verify calculations
        val billsResult = financeRepository.getContractorBills(projectId)
        assertTrue(billsResult.isSuccess)
        val bills = billsResult.getOrNull()!!
        assertFalse(bills.isEmpty())

        val firstBill = bills.first()
        val expectedNet = firstBill.grossAmount.subtract(firstBill.retentionAmount).subtract(firstBill.tdsAmount)
        assertEquals(0, expectedNet.compareTo(firstBill.netPayable))
    }

    @Test
    fun testCostDashboardAggregation() = runBlocking {
        val projectId = "prj_commercial_tower_b"
        val dashboardResult = financeRepository.getCostDashboard(projectId, DashboardPeriod.TOTAL)
        assertTrue(dashboardResult.isSuccess)
        val dashboard = dashboardResult.getOrNull()!!

        assertNotNull(dashboard)
        assertTrue(dashboard.totalBudget > BigDecimal.ZERO)
        assertTrue(dashboard.actualCost > BigDecimal.ZERO)
        assertTrue(dashboard.budgetUsedPct > BigDecimal.ZERO)
        assertFalse(dashboard.costBreakdown.isEmpty())
        assertFalse(dashboard.topMaterialConsumption.isEmpty())

        // Check top material consumption ranking
        val top1 = dashboard.topMaterialConsumption.first()
        assertTrue(top1.totalCost > BigDecimal.ZERO)
        assertTrue(top1.consumptionPercentage > BigDecimal.ZERO)
    }
}
