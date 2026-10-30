package com.example.contruction_management_app

import com.example.contruction_management_app.domain.model.*
import com.example.contruction_management_app.domain.repository.AuditRepository
import com.example.contruction_management_app.domain.repository.NotificationRepository
import com.example.contruction_management_app.domain.repository.SyncQueueRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class Phase6ReportingAndSyncUnitTest {

    private lateinit var fakeApiService: FakeCmsApiService
    private lateinit var notificationRepository: NotificationRepository
    private lateinit var syncQueueRepository: SyncQueueRepository
    private lateinit var auditRepository: AuditRepository

    private val testProjectId = "prj_commercial_tower_b"

    @Before
    fun setup() {
        fakeApiService = FakeCmsApiService()
        notificationRepository = NotificationRepository(fakeApiService)
        syncQueueRepository = SyncQueueRepository(fakeApiService)
        auditRepository = AuditRepository(fakeApiService)
    }

    @Test
    fun testNotificationFetchingAndUnreadCountLifecycle() = runBlocking {
        // 1. Fetch initial notifications
        val notifsResult = notificationRepository.getNotifications()
        assertTrue(notifsResult.isSuccess)
        val notifs = notifsResult.getOrThrow()
        assertTrue("Expected seeded notifications", notifs.isNotEmpty())

        // 2. Verify unread count
        val unreadCount = notificationRepository.getUnreadCount()
        assertTrue("Expected unread alerts", unreadCount >= 2L)

        // 3. Mark single notification as read
        val firstUnread = notifs.first { !it.isRead }
        val markResult = notificationRepository.markAsRead(firstUnread.id)
        assertTrue(markResult.isSuccess)

        // 4. Mark all as read
        val markAllResult = notificationRepository.markAllAsRead()
        assertTrue(markAllResult.isSuccess)
        val updatedNotifs = notificationRepository.getNotifications().getOrThrow()
        assertTrue("All notifications should now be marked as read", updatedNotifs.all { it.isRead })
    }

    @Test
    fun testSyncQueueEnqueueAndBatchReconciliation() = runBlocking {
        // 1. Check initial queue
        val initialQueue = syncQueueRepository.getQueue()
        assertTrue(initialQueue.isNotEmpty())

        // 2. Enqueue offline draft
        val newItem = syncQueueRepository.enqueue(
            projectId = testProjectId,
            actionType = SyncActionType.CREATE_DSR,
            title = "Daily Site Report (Offline)",
            subtitle = "Excavation and shoring progress - Rain delayed",
            payloadJson = "{\"weather\":\"RAINY\",\"labor\":18}"
        )
        assertNotNull(newItem.id)
        assertNotNull(newItem.idempotencyKey)
        assertEquals(SyncStatus.PENDING_SYNC, newItem.status)

        // 3. Sync all pending items
        val syncResult = syncQueueRepository.syncAllPending()
        assertTrue(syncResult.isSuccess)
        val syncedCount = syncResult.getOrThrow()
        assertTrue("At least 1 item should have been synced", syncedCount >= 1)

        // 4. Verify item is now CONFIRMED
        val updatedQueue = syncQueueRepository.getQueue()
        val syncedItem = updatedQueue.find { it.id == newItem.id }
        assertNotNull(syncedItem)
        assertEquals(SyncStatus.CONFIRMED, syncedItem!!.status)

        // 5. Clear confirmed items
        syncQueueRepository.clearConfirmed()
        val queueAfterClear = syncQueueRepository.getQueue()
        assertFalse(queueAfterClear.any { it.id == newItem.id })
    }

    @Test
    fun testSyncQueueConflictDetectionAndResolution() = runBlocking {
        // 1. Verify seeded conflict item
        val conflictCount = syncQueueRepository.getConflictCount()
        assertTrue("Expected at least 1 seeded conflict", conflictCount >= 1)

        val conflictItem = syncQueueRepository.getQueue().first { it.status == SyncStatus.CONFLICT }
        assertEquals(SyncActionType.UPDATE_TASK_PROGRESS, conflictItem.actionType)
        assertNotNull(conflictItem.serverConflictData)

        // 2. Test Merge resolution
        val resolveMergeSuccess = syncQueueRepository.resolveConflict(conflictItem.id, ConflictResolution.MERGE)
        assertTrue(resolveMergeSuccess)
        val mergedItem = syncQueueRepository.getQueue().first { it.id == conflictItem.id }
        assertEquals(SyncStatus.CONFIRMED, mergedItem.status)
        assertTrue(mergedItem.subtitle.contains("[Merged]"))

        // 3. Enqueue another conflicting item and test Force Overwrite
        val anotherItem = syncQueueRepository.enqueue(
            projectId = testProjectId,
            actionType = SyncActionType.LOG_MEASUREMENT,
            title = "Conflicting Measurement",
            subtitle = "Local dims: 10x5m",
            payloadJson = "{\"l\":10,\"w\":5}"
        )
        // Simulate conflict detection from server HTTP 409
        val idx = syncQueueRepository.getQueue().indexOfFirst { it.id == anotherItem.id }
        assertTrue(idx >= 0)

        val resolveOverwriteSuccess = syncQueueRepository.resolveConflict(anotherItem.id, ConflictResolution.OVERWRITE_SERVER)
        assertTrue(resolveOverwriteSuccess)
        val overwrittenItem = syncQueueRepository.getQueue().first { it.id == anotherItem.id }
        assertEquals(SyncStatus.PENDING_SYNC, overwrittenItem.status)
        assertNull(overwrittenItem.serverConflictData)

        // 4. Test Discard Local
        val resolveDiscardSuccess = syncQueueRepository.resolveConflict(anotherItem.id, ConflictResolution.DISCARD_LOCAL)
        assertTrue(resolveDiscardSuccess)
        assertNull(syncQueueRepository.getQueue().find { it.id == anotherItem.id })
    }

    @Test
    fun testAuditLogRetrievalAndSummaryFiltering() = runBlocking {
        // 1. Fetch audit logs
        val logsResult = auditRepository.getAuditLogs(testProjectId)
        assertTrue(logsResult.isSuccess)
        val logs = logsResult.getOrThrow()
        assertTrue(logs.isNotEmpty())

        // Verify key audit fields
        val firstLog = logs.first()
        assertNotNull(firstLog.userName)
        assertNotNull(firstLog.action)
        assertNotNull(firstLog.entityType)
        assertNotNull(firstLog.details)

        // 2. Test entity type filter
        val expenseFilterResult = auditRepository.getAuditLogs(testProjectId, AuditEntityType.EXPENSE)
        assertTrue(expenseFilterResult.isSuccess)
        val expenseLogs = expenseFilterResult.getOrThrow()
        assertTrue(expenseLogs.all { it.entityType == AuditEntityType.EXPENSE })

        // 3. Fetch summary metrics
        val summaryResult = auditRepository.getAuditSummary(testProjectId)
        assertTrue(summaryResult.isSuccess)
        val summary = summaryResult.getOrThrow()
        assertTrue("Total events must be positive", summary.totalEvents > 0)
        assertTrue("Approvals count must be non-negative", summary.approvalsCount >= 0)
    }

    @Test
    fun testDashboardSummaryTelemetryModel() {
        val summary = DashboardSummary(
            activeProjectsCount = 2,
            totalBudgetDisplay = "₹12.00 Cr",
            overallProgressPct = 58.5,
            pendingTasksCount = 7,
            inProgressTasksCount = 14,
            blockedTasksCount = 2,
            completedTasksCount = 45,
            roleMetrics = mapOf(
                "Active Projects" to "2",
                "Total Budget" to "₹12.00 Cr",
                "Avg Progress" to "58%",
                "Blocked Tasks" to "2"
            )
        )

        assertEquals(2, summary.activeProjectsCount)
        assertEquals("₹12.00 Cr", summary.totalBudgetDisplay)
        assertEquals(58.5, summary.overallProgressPct, 0.01)
        assertEquals(7, summary.pendingTasksCount)
        assertEquals(14, summary.inProgressTasksCount)
        assertEquals(2, summary.blockedTasksCount)
        assertEquals(45, summary.completedTasksCount)
        assertEquals("2", summary.roleMetrics["Blocked Tasks"])
    }
}

