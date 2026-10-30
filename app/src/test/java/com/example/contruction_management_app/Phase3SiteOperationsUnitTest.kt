package com.example.contruction_management_app

import com.example.contruction_management_app.domain.model.*
import com.example.contruction_management_app.domain.repository.SiteOperationsRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class Phase3SiteOperationsUnitTest {

    private lateinit var fakeApiService: FakeCmsApiService
    private lateinit var repository: SiteOperationsRepository

    private val testProjectId = "prj_commercial_tower_b"

    @Before
    fun setup() {
        fakeApiService = FakeCmsApiService()
        repository = SiteOperationsRepository(fakeApiService)
    }

    @Test
    fun testDailyReportCreationAndApprovalWorkflow() = runBlocking {
        // 1. Initial seeded reports check
        val reportsResult = repository.getDailyReports(testProjectId)
        assertTrue(reportsResult.isSuccess)
        val reports = reportsResult.getOrThrow()
        assertFalse(reports.isEmpty())
        val seeded = reports.first()
        assertEquals(DsrStatus.APPROVED, seeded.status)

        // 2. Create new DSR
        val attendances = listOf(
            LaborAttendance("la_10", LaborTrade.MASON, 10, BigDecimal("8.0"), "Apex Masonry"),
            LaborAttendance("la_11", LaborTrade.HELPER, 15, BigDecimal("8.0"), "General Pool")
        )
        val createResult = repository.createDailyReport(
            projectId = testProjectId,
            reportDate = "2026-03-05",
            weather = WeatherCondition.SUNNY,
            workSummary = "Formwork stripping for columns C1-C8. Level 1 deck preparation.",
            plannedTomorrow = "Level 1 slab bottom rebar placement.",
            laborAttendances = attendances
        )
        assertTrue(createResult.isSuccess)
        val newReport = createResult.getOrThrow()
        assertEquals("2026-03-05", newReport.reportDate)
        assertEquals(25, newReport.totalHeadcount)
        assertEquals(DsrStatus.SUBMITTED, newReport.status)

        // 3. Approve DSR
        val approveResult = repository.approveDailyReport(
            projectId = testProjectId,
            reportId = newReport.id,
            action = "APPROVE",
            notes = "Checked stripping records. Good progress."
        )
        assertTrue(approveResult.isSuccess)
        val approvedReport = approveResult.getOrThrow()
        assertEquals(DsrStatus.APPROVED, approvedReport.status)
        assertEquals("Checked stripping records. Good progress.", approvedReport.approvalNotes)
    }

    @Test
    fun testWorkProgressReportedVsVerifiedDualControl() = runBlocking {
        // 1. Initial seeded progress check
        val listResult = repository.getWorkProgress(testProjectId)
        assertTrue(listResult.isSuccess)
        val list = listResult.getOrThrow()
        assertTrue(list.any { it.status == WorkProgressStatus.VERIFIED })

        // 2. Record new work progress
        val recordResult = repository.recordWorkProgress(
            projectId = testProjectId,
            taskId = "task_3",
            reportId = "dsr_1",
            itemName = "Level 1 Beam Rebar Fabrication",
            unit = "MT",
            plannedQuantity = BigDecimal("20.0"),
            reportedQuantity = BigDecimal("14.5")
        )
        assertTrue(recordResult.isSuccess)
        val recorded = recordResult.getOrThrow()
        assertEquals("Level 1 Beam Rebar Fabrication", recorded.itemName)
        assertEquals(BigDecimal("14.5"), recorded.reportedQuantity)
        assertEquals(BigDecimal.ZERO, recorded.verifiedQuantity)
        assertEquals(WorkProgressStatus.REPORTED, recorded.status)

        // 3. Physical quantity verification with slight deduction
        val verifyResult = repository.verifyWorkProgress(
            projectId = testProjectId,
            progressId = recorded.id,
            action = "VERIFY",
            verifiedQuantity = BigDecimal("14.0"),
            notes = "0.5 MT rebar bundle pending tying wire inspection"
        )
        assertTrue(verifyResult.isSuccess)
        val verified = verifyResult.getOrThrow()
        assertEquals(WorkProgressStatus.VERIFIED, verified.status)
        assertEquals(BigDecimal("14.0"), verified.verifiedQuantity)
        assertNotNull(verified.verifiedBy)
        assertEquals("0.5 MT rebar bundle pending tying wire inspection", verified.verificationNotes)
    }

    @Test
    fun testDigitalInspectionChecklistPassAndFail() = runBlocking {
        // 1. Passed Inspection
        val passChecklist = """[{"item":"Rebar cover","passed":true},{"item":"Plumbness","passed":true}]"""
        val passResult = repository.createInspection(
            projectId = testProjectId,
            title = "Level 1 Deck Alignment Inspection",
            inspectionType = InspectionType.STRUCTURAL,
            status = InspectionStatus.PASSED,
            checklistData = passChecklist,
            inspectionDate = "2026-03-05",
            location = "Level 1 Slab",
            notes = "All tolerances within IS 456 spec"
        )
        assertTrue(passResult.isSuccess)
        assertEquals(InspectionStatus.PASSED, passResult.getOrThrow().status)

        // 2. Failed Inspection
        val failChecklist = """[{"item":"Guardrail top rail 1.1m","passed":false},{"item":"Lifeline attached","passed":true}]"""
        val failResult = repository.createInspection(
            projectId = testProjectId,
            title = "Level 1 Staging Edge Safety Inspection",
            inspectionType = InspectionType.SAFETY,
            status = InspectionStatus.FAILED,
            checklistData = failChecklist,
            inspectionDate = "2026-03-05",
            location = "Level 1 West Face",
            notes = "Missing edge guardrail barricade"
        )
        assertTrue(failResult.isSuccess)
        assertEquals(InspectionStatus.FAILED, failResult.getOrThrow().status)
    }

    @Test
    fun testDefectClosedLoopLifecycle() = runBlocking {
        // 1. Log Defect
        val createResult = repository.createDefect(
            projectId = testProjectId,
            inspectionId = null,
            title = "Honeycombing at Column C3 base",
            description = "Aggregate pocket observed after stripping formwork. Depth approx 25mm.",
            location = "Column C3 Ground Floor",
            severity = IssueSeverity.HIGH,
            assignedTo = "Site Engineer",
            deadline = "2026-03-07"
        )
        assertTrue(createResult.isSuccess)
        val defect = createResult.getOrThrow()
        assertEquals(DefectStatus.OPEN, defect.status)

        // 2. Submitter rectifies defect -> RESOLVED
        val resolveResult = repository.updateDefectStatus(testProjectId, defect.id, DefectStatus.RESOLVED)
        assertTrue(resolveResult.isSuccess)
        val resolvedDefect = resolveResult.getOrThrow()
        assertEquals(DefectStatus.RESOLVED, resolvedDefect.status)

        // 3. QC Re-inspection Rejection -> returns to OPEN
        val rejectResult = repository.reinspectDefect(
            projectId = testProjectId,
            defectId = defect.id,
            action = "REJECT_REOPEN",
            notes = "Non-shrink grout not cured sufficiently. Patching incomplete."
        )
        assertTrue(rejectResult.isSuccess)
        val reopenedDefect = rejectResult.getOrThrow()
        assertEquals(DefectStatus.OPEN, reopenedDefect.status)

        // 4. Submitter fixes again -> RESOLVED
        repository.updateDefectStatus(testProjectId, defect.id, DefectStatus.RESOLVED)

        // 5. QC Re-inspection Approval -> CLOSED
        val closeResult = repository.reinspectDefect(
            projectId = testProjectId,
            defectId = defect.id,
            action = "APPROVE_CLOSE",
            notes = "Epoxy injection and micro-concrete patching verified. Sounding test passed."
        )
        assertTrue(closeResult.isSuccess)
        val closedDefect = closeResult.getOrThrow()
        assertEquals(DefectStatus.CLOSED, closedDefect.status)
        assertEquals("QC Inspector", closedDefect.reInspectedBy)
    }
}
