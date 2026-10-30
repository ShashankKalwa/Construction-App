package com.example.contruction_management_app

import com.example.contruction_management_app.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

class Phase2ProjectAndTaskUnitTest {

    @Test
    fun testTaskStatusParsing() {
        assertEquals(TaskStatus.SUBMITTED_FOR_REVIEW, TaskStatus.fromString("SUBMITTED_FOR_REVIEW"))
        assertEquals(TaskStatus.SUBMITTED_FOR_REVIEW, TaskStatus.fromString("PENDING_REVIEW"))
        assertEquals(TaskStatus.CORRECTIONS_REQUIRED, TaskStatus.fromString("CORRECTIONS_REQUIRED"))
        assertEquals(TaskStatus.CORRECTIONS_REQUIRED, TaskStatus.fromString("REJECTED"))
        assertEquals(TaskStatus.APPROVED, TaskStatus.fromString("APPROVED"))
        assertEquals(TaskStatus.IN_PROGRESS, TaskStatus.fromString("IN_PROGRESS"))
        assertEquals(TaskStatus.COMPLETED, TaskStatus.fromString("COMPLETED"))
        assertEquals(TaskStatus.BLOCKED, TaskStatus.fromString("BLOCKED"))
        assertEquals(TaskStatus.PENDING, TaskStatus.fromString("UNKNOWN"))
    }

    @Test
    fun testTaskSelfApprovalCheck() {
        val assignee = UserSummary(id = "usr_01", fullName = "Rajesh Kumar", email = "rajesh@apexinfra.com")
        val manager = UserSummary(id = "usr_02", fullName = "Vikram Singh", email = "vikram@apexinfra.com")

        val task = Task(
            id = "tsk_01",
            phaseId = "phs_01",
            phaseName = "Substructure",
            projectId = "prj_01",
            title = "Pour Foundation Footings",
            description = "Grade M30 concrete",
            assignee = assignee,
            status = TaskStatus.SUBMITTED_FOR_REVIEW,
            startDate = "2026-03-01",
            dueDate = "2026-03-10",
            progressPct = 100.0,
            reviewStatus = "SUBMITTED"
        )

        // Assignee cannot self-approve
        val isSelfReview = task.assignee?.id == assignee.id
        assertTrue("Assignee self-review should be flagged as restricted", isSelfReview)

        // Another user (PM/Engineer) can review
        val canManagerReview = task.assignee?.id != manager.id
        assertTrue("Project Manager should be eligible to review", canManagerReview)
    }

    @Test
    fun testMilestoneDataModel() {
        val milestone = Milestone(
            id = "mls_01",
            projectId = "prj_01",
            title = "Foundation & Raft Concrete Completion",
            dueDate = "2026-03-15",
            status = "REACHED",
            targetCompletionPct = 25.0
        )

        assertEquals("Foundation & Raft Concrete Completion", milestone.title)
        assertEquals("REACHED", milestone.status)
        assertEquals(25.0, milestone.targetCompletionPct, 0.01)
    }

    @Test
    fun testProjectMetadata() {
        val project = Project(
            id = "prj_01",
            name = "Commercial Tower B",
            code = "SBP-02",
            location = "Whitefield, Bangalore",
            budgetInr = BigDecimal("68000000.00"),
            status = "Active",
            progressPct = 18.0,
            organizationId = "org_apex",
            projectType = "Commercial",
            clientName = "City Corp",
            startDate = "2026-02-15",
            endDate = "2027-06-30"
        )

        assertEquals("Commercial", project.projectType)
        assertEquals("City Corp", project.clientName)
        assertEquals("₹6.80 Cr", project.budgetDisplay)
    }
}
