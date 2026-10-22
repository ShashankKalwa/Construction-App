package com.example.contruction_management_app

import com.example.contruction_management_app.domain.model.SyncActionType
import com.example.contruction_management_app.domain.model.SyncStatus
import com.example.contruction_management_app.domain.repository.SyncQueueRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SyncQueueUnitTest {

    private lateinit var syncRepository: SyncQueueRepository
    private lateinit var fakeApi: FakeCmsApiService

    @Before
    fun setUp() {
        fakeApi = FakeCmsApiService()
        syncRepository = SyncQueueRepository(fakeApi)
    }

    @Test
    fun testInitialQueueHasPendingAndConfirmedItems() {
        val queue = syncRepository.getQueue()
        assertTrue(queue.isNotEmpty())
        val pendingCount = syncRepository.getPendingCount()
        assertTrue(pendingCount >= 1)
    }

    @Test
    fun testEnqueueNewOfflineItem() {
        val item = syncRepository.enqueue(
            projectId = "prj_test",
            actionType = SyncActionType.CREATE_DSR,
            title = "Daily Site Report Draft",
            subtitle = "Excavation and trenching progress",
            payloadJson = "{\"status\":\"DRAFT\"}"
        )

        assertNotNull(item.id)
        assertEquals(SyncStatus.PENDING_SYNC, item.status)
        assertEquals(0, item.retryCount)

        val updatedQueue = syncRepository.getQueue()
        assertEquals(item.id, updatedQueue.first().id)
    }

    @Test
    fun testSyncAllPendingTransitionsToConfirmed() = runBlocking {
        val initialPending = syncRepository.getPendingCount()
        assertTrue(initialPending > 0)

        val result = syncRepository.syncAllPending()
        assertTrue(result.isSuccess)
        assertEquals(initialPending, result.getOrNull())

        assertEquals(0, syncRepository.getPendingCount())
        val queue = syncRepository.getQueue()
        assertTrue(queue.all { it.status == SyncStatus.CONFIRMED })
    }
}
