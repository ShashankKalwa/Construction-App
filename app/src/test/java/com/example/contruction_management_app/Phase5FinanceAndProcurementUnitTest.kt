package com.example.contruction_management_app

import com.example.contruction_management_app.core.network.CreatePurchaseOrderItemRequest
import com.example.contruction_management_app.domain.model.*
import com.example.contruction_management_app.domain.repository.FinanceRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode

class Phase5FinanceAndProcurementUnitTest {

    private lateinit var fakeApiService: FakeCmsApiService
    private lateinit var repository: FinanceRepository

    private val testProjectId = "prj_commercial_tower_b"
    private val testOrgId = "org_apex_1"

    @Before
    fun setup() {
        fakeApiService = FakeCmsApiService()
        repository = FinanceRepository(fakeApiService)
    }

    @Test
    fun testExpenseStepperProgressionAndDualControlRejection() = runBlocking {
        // 1. Initial seeded expenses check
        val expensesResult = repository.getExpenses(testProjectId)
        assertTrue(expensesResult.isSuccess)
        val initialExpenses = expensesResult.getOrThrow()
        assertTrue("Expected seeded expenses", initialExpenses.isNotEmpty())

        // 2. Submit new expense
        val submitResult = repository.createExpense(
            projectId = testProjectId,
            category = ExpenseCategory.EQUIPMENT,
            title = "Mobile Crane Hire for Truss Erection",
            amount = BigDecimal("45000.00"),
            expenseDate = "2026-03-22"
        )
        assertTrue(submitResult.isSuccess)
        val newExpense = submitResult.getOrThrow()
        assertEquals(ExpenseStatus.SUBMITTED, newExpense.status)
        assertEquals(BigDecimal("45000.00"), newExpense.amount)

        // 3. Step 1: Verify Expense
        val verifyResult = repository.updateExpenseStatus(
            projectId = testProjectId,
            expenseId = newExpense.id,
            status = ExpenseStatus.VERIFIED
        )
        assertTrue(verifyResult.isSuccess)
        val verifiedExpense = verifyResult.getOrThrow()
        assertEquals(ExpenseStatus.VERIFIED, verifiedExpense.status)
        assertNotNull(verifiedExpense.verifiedBy)

        // 4. Step 2: Approve Expense
        val approveResult = repository.updateExpenseStatus(
            projectId = testProjectId,
            expenseId = verifiedExpense.id,
            status = ExpenseStatus.APPROVED
        )
        assertTrue(approveResult.isSuccess)
        val approvedExpense = approveResult.getOrThrow()
        assertEquals(ExpenseStatus.APPROVED, approvedExpense.status)
        assertNotNull(approvedExpense.approvedBy)

        // 5. Step 3: Disburse & Mark Paid with Payment Reference
        val payResult = repository.updateExpenseStatus(
            projectId = testProjectId,
            expenseId = approvedExpense.id,
            status = ExpenseStatus.PAID,
            paymentReference = "CMS-NEFT-99182"
        )
        assertTrue(payResult.isSuccess)
        val paidExpense = payResult.getOrThrow()
        assertEquals(ExpenseStatus.PAID, paidExpense.status)
        assertEquals("CMS-NEFT-99182", paidExpense.paymentReference)

        // 6. Test Rejection flow with Audit Reason
        val submitToReject = repository.createExpense(
            projectId = testProjectId,
            category = ExpenseCategory.OTHER,
            title = "Personal transport claim without toll receipt",
            amount = BigDecimal("3500.00"),
            expenseDate = "2026-03-23"
        ).getOrThrow()

        val rejectResult = repository.updateExpenseStatus(
            projectId = testProjectId,
            expenseId = submitToReject.id,
            status = ExpenseStatus.REJECTED,
            rejectionReason = "Missing GST invoice and odometer log"
        )
        assertTrue(rejectResult.isSuccess)
        val rejectedExpense = rejectResult.getOrThrow()
        assertEquals(ExpenseStatus.REJECTED, rejectedExpense.status)
        assertEquals("Missing GST invoice and odometer log", rejectedExpense.rejectionReason)
    }

    @Test
    fun testContractorRaBillRetentionAndTdsStatutoryDeductions() = runBlocking {
        // 1. Initial bills check
        val billsResult = repository.getContractorBills(testProjectId)
        assertTrue(billsResult.isSuccess)
        val bills = billsResult.getOrThrow()
        assertTrue(bills.isNotEmpty())

        // 2. Create RA Bill: Gross ₹10,00,000 with 5% Retention and 2% TDS
        val gross = BigDecimal("1000000.00")
        val retentionPct = BigDecimal("5.00")
        val tdsPct = BigDecimal("2.00")

        val createBillResult = repository.createContractorBill(
            projectId = testProjectId,
            contractorName = "Chaitanya Structural Steel Works Pvt Ltd",
            billNumber = "RA-BILL-03",
            billDate = "2026-03-24",
            grossAmount = gross,
            retentionPct = retentionPct,
            tdsPct = tdsPct,
            workDescription = "Basement slab reinforcement beam binding & concrete placing"
        )
        assertTrue(createBillResult.isSuccess)
        val newBill = createBillResult.getOrThrow()

        // Verify accurate financial calculations
        assertEquals(BigDecimal("50000.00"), newBill.retentionAmount) // 5% of 10L
        assertEquals(BigDecimal("20000.00"), newBill.tdsAmount)       // 2% of 10L
        assertEquals(BigDecimal("930000.00"), newBill.netPayable)     // 10L - 50k - 20k = 9.3L
        assertEquals(BillPaymentStatus.PENDING, newBill.status)

        // 3. Approve Bill
        val approveResult = repository.updateContractorBillStatus(
            projectId = testProjectId,
            billId = newBill.id,
            status = BillPaymentStatus.APPROVED
        )
        assertTrue(approveResult.isSuccess)
        assertEquals(BillPaymentStatus.APPROVED, approveResult.getOrThrow().status)

        // 4. Disburse & Pay Bill
        val payResult = repository.updateContractorBillStatus(
            projectId = testProjectId,
            billId = newBill.id,
            status = BillPaymentStatus.PAID,
            paymentReference = "RTGS-HDFC-88271"
        )
        assertTrue(payResult.isSuccess)
        val paidBill = payResult.getOrThrow()
        assertEquals(BillPaymentStatus.PAID, paidBill.status)
        assertEquals("RTGS-HDFC-88271", paidBill.paymentReference)
    }

    @Test
    fun testVendorRegistrationAndProcurementListing() = runBlocking {
        // 1. Fetch vendors
        val initialVendors = repository.getVendors(testOrgId).getOrThrow()
        assertTrue("Expected seeded vendors", initialVendors.isNotEmpty())

        // 2. Register new vendor
        val createVendorResult = repository.createVendor(
            organizationId = testOrgId,
            name = "Southern Ready-Mix Concrete Solutions",
            category = VendorCategory.MATERIALS,
            contactPerson = "S. Raghavan",
            phone = "+91 99887 76655",
            email = "sales@southernrmc.com",
            gstin = "29AAACS1234D1Z2",
            paymentTerms = "NET_30"
        )
        assertTrue(createVendorResult.isSuccess)
        val vendor = createVendorResult.getOrThrow()
        assertEquals("Southern Ready-Mix Concrete Solutions", vendor.name)
        assertEquals(VendorCategory.MATERIALS, vendor.categoryEnum)
        assertEquals("29AAACS1234D1Z2", vendor.gstin)

        // 3. Verify in repository
        val updatedVendors = repository.getVendors(testOrgId).getOrThrow()
        assertTrue(updatedVendors.any { it.id == vendor.id })
    }

    @Test
    fun testPurchaseOrderCommitmentAndGoodsReceiptNoteLifecycle() = runBlocking {
        // 1. Create PO for 20 MT Rebar @ ₹65,000/MT
        val lineItem = CreatePurchaseOrderItemRequest(
            materialId = "mat_steel_20",
            orderedQuantity = BigDecimal("20.00"),
            unitPrice = BigDecimal("65000.00")
        )

        val poResult = repository.createPurchaseOrder(
            projectId = testProjectId,
            vendorId = "vnd_apex_steel",
            poNumber = "PO-2026-0202",
            expectedDeliveryDate = "2026-04-05",
            paymentTerms = "NET_30",
            notes = "Test mill certificate required with delivery",
            items = listOf(lineItem)
        )
        assertTrue(poResult.isSuccess)
        val po = poResult.getOrThrow()

        // 20 MT * 65,000 = 13,00,000
        val expectedTotal = BigDecimal("1300000.00")

        assertEquals(PurchaseOrderStatus.ISSUED, po.status)
        assertEquals(expectedTotal, po.totalAmount.setScale(2, RoundingMode.HALF_UP))
        assertEquals(1, po.items.size)
        val poItem = po.items.first()
        assertEquals(BigDecimal("20.00"), poItem.orderedQuantity)
        assertEquals(BigDecimal.ZERO, poItem.receivedQuantity)

        // 2. Record GRN delivery: 20 MT arrived at site
        val grnResult = repository.recordGrn(
            po = po,
            lineItemId = poItem.id,
            challanNumber = "DC-STEEL-8891",
            receivedQuantity = BigDecimal("20.00"),
            deliveryDate = "2026-04-04",
            remarks = "Weighed on site weighbridge: 20.02 MT gross, approved"
        )
        assertTrue(grnResult.isSuccess)
        val grn = grnResult.getOrThrow()
        assertEquals(po.id, grn.purchaseOrderId)
        assertEquals("DC-STEEL-8891", grn.challanNumber)
        assertEquals(BigDecimal("20.00"), grn.receivedQuantity)

        // 3. Verify PO status updated to COMPLETED because all ordered items were received
        val updatedPos = repository.getPurchaseOrders(testProjectId).getOrThrow()
        val completedPo = updatedPos.find { it.id == po.id }
        assertNotNull(completedPo)
        assertEquals(PurchaseOrderStatus.COMPLETED, completedPo!!.status)
        assertEquals(BigDecimal("20.00"), completedPo.items.first().receivedQuantity)
    }

    @Test
    fun testCostDashboardMetricsCommitmentVsActualCost() = runBlocking {
        val dashboardResult = repository.getCostDashboard(testProjectId, DashboardPeriod.TOTAL)
        assertTrue(dashboardResult.isSuccess)
        val summary = dashboardResult.getOrThrow()

        // 1. Verify project details & budget
        assertEquals("Commercial Tower B", summary.projectName)
        assertEquals(BigDecimal("50000000.00"), summary.totalBudget) // 5 Cr

        // 2. Actual cost must represent only PAID expenses and PAID bills
        assertTrue("Actual cost should be positive", summary.actualCost > BigDecimal.ZERO)

        // 3. Committed cost must include active POs and APPROVED bills waiting for payment
        assertTrue("Committed cost should be positive", summary.committedCost >= BigDecimal.ZERO)

        // 4. Remaining budget = Total Budget - Actual Cost
        assertEquals(summary.totalBudget.subtract(summary.actualCost), summary.remainingBudget)

        // 5. Projected final cost = Actual Cost + Committed Cost
        assertEquals(summary.actualCost.add(summary.committedCost), summary.projectedFinalCost)

        // 6. Verify breakdown categories exist
        assertFalse(summary.costBreakdown.isEmpty())
        assertTrue(summary.costBreakdown.any { it.category == "MATERIALS" })
        assertTrue(summary.costBreakdown.any { it.category == "LABOR" })
        assertTrue(summary.costBreakdown.any { it.category == "SUBCONTRACTOR" })

        // 7. Verify top materials consumption
        assertFalse(summary.topMaterialConsumption.isEmpty())
        assertTrue(summary.topMaterialConsumption.any { it.materialId.contains("steel") })
    }
}

