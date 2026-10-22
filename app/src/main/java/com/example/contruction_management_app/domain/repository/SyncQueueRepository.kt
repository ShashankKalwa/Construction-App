package com.example.contruction_management_app.domain.repository

import com.example.contruction_management_app.core.network.CmsApiService
import com.example.contruction_management_app.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

class SyncQueueRepository(private val apiService: CmsApiService) {

    private val queueList = CopyOnWriteArrayList<SyncQueueItem>()
    private val _syncState = MutableStateFlow<List<SyncQueueItem>>(emptyList())
    val syncState: StateFlow<List<SyncQueueItem>> = _syncState.asStateFlow()

    init {
        seedInitialQueue()
    }

    private fun seedInitialQueue() {
        queueList.addAll(
            listOf(
                SyncQueueItem(
                    id = "sync_001",
                    projectId = "prj_commercial_tower_b",
                    actionType = SyncActionType.CREATE_DSR,
                    payloadJson = "{\"weather\":\"SUNNY\",\"progress\":\"Casting 4th floor slab completed\"}",
                    title = "Daily Site Report (DSR)",
                    subtitle = "4th Floor Slab Concreting - Weather: Sunny",
                    timestamp = System.currentTimeMillis() - 3600000,
                    status = SyncStatus.PENDING_SYNC,
                    retryCount = 0
                ),
                SyncQueueItem(
                    id = "sync_002",
                    projectId = "prj_commercial_tower_b",
                    actionType = SyncActionType.LOG_MEASUREMENT,
                    payloadJson = "{\"nos\":1,\"l\":12.5,\"w\":8.0,\"d\":0.15}",
                    title = "Measurement Entry",
                    subtitle = "Beam B12 Reinforcement Dimensions (15.00 m³)",
                    timestamp = System.currentTimeMillis() - 7200000,
                    status = SyncStatus.CONFIRMED,
                    retryCount = 0
                )
            )
        )
        _syncState.value = queueList.toList()
    }

    fun getPendingCount(): Int {
        return queueList.count { it.status == SyncStatus.PENDING_SYNC || it.status == SyncStatus.FAILED }
    }

    fun getQueue(): List<SyncQueueItem> = queueList.toList()

    fun enqueue(
        projectId: String,
        actionType: SyncActionType,
        title: String,
        subtitle: String,
        payloadJson: String
    ): SyncQueueItem {
        val item = SyncQueueItem(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            actionType = actionType,
            payloadJson = payloadJson,
            title = title,
            subtitle = subtitle,
            timestamp = System.currentTimeMillis(),
            status = SyncStatus.PENDING_SYNC,
            retryCount = 0
        )
        queueList.add(0, item)
        _syncState.value = queueList.toList()
        return item
    }

    suspend fun syncAllPending(): Result<Int> = withContext(Dispatchers.IO) {
        val pending = queueList.filter { it.status == SyncStatus.PENDING_SYNC || it.status == SyncStatus.FAILED }
        if (pending.isEmpty()) return@withContext Result.success(0)

        // Mark as SYNCING
        for (item in pending) {
            val idx = queueList.indexOfFirst { it.id == item.id }
            if (idx != -1) {
                queueList[idx] = queueList[idx].copy(status = SyncStatus.SYNCING)
            }
        }
        _syncState.value = queueList.toList()

        delay(800) // Brief simulation of network sync handshake

        var syncedCount = 0
        for (item in pending) {
            val idx = queueList.indexOfFirst { it.id == item.id }
            if (idx != -1) {
                // Transition to CONFIRMED
                queueList[idx] = queueList[idx].copy(
                    status = SyncStatus.CONFIRMED,
                    errorMessage = null
                )
                syncedCount++
            }
        }
        _syncState.value = queueList.toList()
        Result.success(syncedCount)
    }

    fun clearConfirmed() {
        queueList.removeAll { it.status == SyncStatus.CONFIRMED }
        _syncState.value = queueList.toList()
    }
}
