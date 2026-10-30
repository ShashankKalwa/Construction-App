package com.example.contruction_management_app

import com.example.contruction_management_app.domain.model.MaterialCategory
import com.example.contruction_management_app.domain.model.MaterialRequestStatus
import com.example.contruction_management_app.domain.model.MovementType
import com.example.contruction_management_app.domain.repository.InventoryRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class Phase4MaterialInventoryUnitTest {

    private lateinit var fakeApiService: FakeCmsApiService
    private lateinit var repository: InventoryRepository

    private val testProjectId = "prj_commercial_tower_b"

    @Before
    fun setup() {
        fakeApiService = FakeCmsApiService()
        repository = InventoryRepository(fakeApiService)
    }

    @Test
    fun testMaterialsListingAndLowStockSafetyDetection() = runBlocking {
        // 1. Fetch materials
        val materialsResult = repository.getMaterials(testProjectId)
        assertTrue(materialsResult.isSuccess)
        val materials = materialsResult.getOrThrow()
        assertTrue("Expected seeded materials", materials.size >= 5)

        // Verify cement and steel presence
        val cement = materials.find { it.id == "mat_cem_opc" }
        assertNotNull(cement)
        assertEquals(MaterialCategory.CEMENT, cement!!.category)
        assertEquals(BigDecimal("450.00"), cement.currentStock)

        // 2. Fetch low-stock materials
        val lowStockResult = repository.getLowStockMaterials(testProjectId)
        assertTrue(lowStockResult.isSuccess)
        val lowStockList = lowStockResult.getOrThrow()
        assertFalse("Expected low stock alerts", lowStockList.isEmpty())

        // 12mm rebar has 3.50 MT vs 6.00 MT threshold -> should be flagged
        val rebar12 = lowStockList.find { it.id == "mat_steel_12" }
        assertNotNull("12mm Rebar should be flagged as low stock", rebar12)
        assertTrue(rebar12!!.isLowStock)
        assertTrue(rebar12.currentStock < rebar12.minStockThreshold)
    }

    @Test
    fun testMaterialRequestCreationApprovalAndRejectionWorkflow() = runBlocking {
        // 1. Check seeded requests
        val initialRequests = repository.getMaterialRequests(testProjectId).getOrThrow()
        assertTrue(initialRequests.isNotEmpty())

        // 2. Create new requisition (MRN)
        val createResult = repository.createMaterialRequest(
            projectId = testProjectId,
            materialId = "mat_cem_opc",
            quantity = BigDecimal("80.00"),
            requiredByDate = "2026-03-30",
            phaseOrTaskDesc = "Basement retaining wall waterproofing plaster"
        )
        assertTrue(createResult.isSuccess)
        val newReq = createResult.getOrThrow()
        assertEquals("mat_cem_opc", newReq.materialId)
        assertEquals(BigDecimal("80.00"), newReq.quantity)
        assertEquals(MaterialRequestStatus.PENDING, newReq.status)

        // 3. Approve requisition
        val approveResult = repository.approveMaterialRequest(testProjectId, newReq.id)
        assertTrue(approveResult.isSuccess)
        val approvedReq = approveResult.getOrThrow()
        assertEquals(MaterialRequestStatus.APPROVED, approvedReq.status)
        assertNotNull(approvedReq.approvedBy)

        // 4. Create another requisition and reject with reason
        val req2Result = repository.createMaterialRequest(
            projectId = testProjectId,
            materialId = "mat_sand_m",
            quantity = BigDecimal("50.00"),
            requiredByDate = "2026-04-01",
            phaseOrTaskDesc = "Unplanned blockwork"
        )
        val req2 = req2Result.getOrThrow()

        val rejectResult = repository.rejectMaterialRequest(
            projectId = testProjectId,
            requestId = req2.id,
            reason = "Budget limit reached for masonry phase this month"
        )
        assertTrue(rejectResult.isSuccess)
        val rejectedReq = rejectResult.getOrThrow()
        assertEquals(MaterialRequestStatus.REJECTED, rejectedReq.status)
        assertEquals("Budget limit reached for masonry phase this month", rejectedReq.rejectionReason)
    }

    @Test
    fun testStorekeeperStockIssuanceWithAtomicStockDeductionAndLedgerRecording() = runBlocking {
        // 1. Initial stock of 16mm rebar is 14.50 MT
        val materialsInitial = repository.getMaterials(testProjectId).getOrThrow()
        val rebar16Initial = materialsInitial.find { it.id == "mat_steel_16" }!!
        val startingStock = rebar16Initial.currentStock

        // 2. Create and approve a requisition for 4.50 MT
        val reqResult = repository.createMaterialRequest(
            projectId = testProjectId,
            materialId = "mat_steel_16",
            quantity = BigDecimal("4.50"),
            requiredByDate = "2026-03-25",
            phaseOrTaskDesc = "Column reinforcement cage"
        )
        val req = reqResult.getOrThrow()
        repository.approveMaterialRequest(testProjectId, req.id)

        // 3. Storekeeper issues stock against approved request
        val issueResult = repository.issueMaterialRequest(
            projectId = testProjectId,
            requestId = req.id,
            notes = "Gate pass GP-2026-901"
        )
        assertTrue(issueResult.isSuccess)
        val issuedReq = issueResult.getOrThrow()
        assertEquals(MaterialRequestStatus.ISSUED, issuedReq.status)
        assertNotNull(issuedReq.issuedBy)

        // 4. Verify warehouse stock deducted
        val materialsAfter = repository.getMaterials(testProjectId).getOrThrow()
        val rebar16After = materialsAfter.find { it.id == "mat_steel_16" }!!
        val expectedStock = startingStock.subtract(BigDecimal("4.50"))
        assertEquals(expectedStock, rebar16After.currentStock)

        // 5. Verify Stock Ledger has OUTWARD_ISSUE recorded
        val ledger = repository.getStockLedger(testProjectId).getOrThrow()
        val latestMovement = ledger.firstOrNull { it.materialId == "mat_steel_16" && it.movementType == MovementType.OUTWARD_ISSUE }
        assertNotNull("Outward movement should be in ledger", latestMovement)
        assertEquals(BigDecimal("-4.50"), latestMovement!!.quantity)
        assertEquals(expectedStock, latestMovement.balanceAfter)
    }

    @Test
    fun testInsufficientStockPreventsIssuance() = runBlocking {
        // 1. Create a request for an excessive quantity (e.g. 999.00 MT)
        val excessiveReqResult = repository.createMaterialRequest(
            projectId = testProjectId,
            materialId = "mat_steel_16",
            quantity = BigDecimal("999.00"),
            requiredByDate = "2026-03-28",
            phaseOrTaskDesc = "Bridge deck foundation"
        )
        val excessiveReq = excessiveReqResult.getOrThrow()
        repository.approveMaterialRequest(testProjectId, excessiveReq.id)

        // 2. Attempting to issue should fail due to insufficient stock
        val issueResult = repository.issueMaterialRequest(testProjectId, excessiveReq.id)
        assertTrue("Issuance should fail when quantity exceeds available stock", issueResult.isFailure)
        val errorMsg = issueResult.exceptionOrNull()?.message ?: ""
        assertTrue("Error should mention insufficient stock", errorMsg.contains("Insufficient stock", ignoreCase = true))
    }

    @Test
    fun testStockReturnWorkflowCreditsInventoryAndRecordsLedger() = runBlocking {
        // 1. Initial cement stock is 450.00 Bags
        val materialsInitial = repository.getMaterials(testProjectId).getOrThrow()
        val cementInitial = materialsInitial.find { it.id == "mat_cem_opc" }!!
        val stockBeforeReturn = cementInitial.currentStock

        // 2. Site team returns 25.00 Bags unused surplus
        val returnResult = repository.returnStock(
            projectId = testProjectId,
            materialId = "mat_cem_opc",
            quantity = BigDecimal("25.00"),
            referenceId = "RET-2026-01",
            notes = "Surplus bags from footing pour, bags intact and dry"
        )
        assertTrue(returnResult.isSuccess)
        val returnedMat = returnResult.getOrThrow()
        val expectedNewStock = stockBeforeReturn.add(BigDecimal("25.00"))
        assertEquals(expectedNewStock, returnedMat.currentStock)

        // 3. Verify stock ledger records RETURN movement
        val ledger = repository.getStockLedger(testProjectId).getOrThrow()
        val returnMovement = ledger.firstOrNull { it.referenceId == "RET-2026-01" }
        assertNotNull("Return movement should be recorded in ledger", returnMovement)
        assertEquals(MovementType.RETURN, returnMovement!!.movementType)
        assertEquals(BigDecimal("25.00"), returnMovement.quantity)
        assertEquals(expectedNewStock, returnMovement.balanceAfter)
    }

    @Test
    fun testStockAdjustmentAuditTrail() = runBlocking {
        // 1. Perform Inward GRN adjustment
        val adjustResult = repository.adjustStock(
            projectId = testProjectId,
            materialId = "mat_sand_m",
            movementType = MovementType.INWARD_GRN,
            quantity = BigDecimal("40.00"),
            referenceId = "PO-SAND-77",
            notes = "Direct quarry delivery tipper truck #DL-01-AB-1234"
        )
        assertTrue(adjustResult.isSuccess)

        // 2. Verify stock ledger contains INWARD_GRN movement
        val ledger = repository.getStockLedger(testProjectId).getOrThrow()
        val entry = ledger.firstOrNull { it.referenceId == "PO-SAND-77" }
        assertNotNull(entry)
        assertEquals(MovementType.INWARD_GRN, entry!!.movementType)
        assertEquals(BigDecimal("40.00"), entry.quantity)
    }
}

