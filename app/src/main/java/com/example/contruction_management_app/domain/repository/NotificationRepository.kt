package com.example.contruction_management_app.domain.repository

import com.example.contruction_management_app.core.network.CmsApiService
import com.example.contruction_management_app.core.network.NotificationNetworkDto
import com.example.contruction_management_app.domain.model.NotificationItem
import com.example.contruction_management_app.domain.model.NotificationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.CopyOnWriteArrayList

class NotificationRepository(private val apiService: CmsApiService) {

    private val localNotifications = CopyOnWriteArrayList<NotificationItem>()

    init {
        seedInitialNotifications()
    }

    private fun seedInitialNotifications() {
        localNotifications.addAll(
            listOf(
                NotificationItem(
                    id = "notif_001",
                    organizationId = "org_apex_infra",
                    userId = "usr_pm_rahul",
                    projectId = "prj_commercial_tower_b",
                    title = "Low Stock Warning: OPC 53 Cement",
                    message = "On-hand balance (85.00 Bags) is below the minimum reorder threshold (100.00 Bags).",
                    type = NotificationType.LOW_STOCK,
                    isRead = false,
                    entityId = "mat_cement_opc53",
                    actionUrl = "/inventory",
                    createdAt = "1 hour ago"
                ),
                NotificationItem(
                    id = "notif_002",
                    organizationId = "org_apex_infra",
                    userId = "usr_pm_rahul",
                    projectId = "prj_commercial_tower_b",
                    title = "Approval Required: BOQ Rev 2",
                    message = "BOQ Revision 2 totaling ₹1,48,50,000 submitted by Priya Sharma for verification.",
                    type = NotificationType.APPROVAL_REQUIRED,
                    isRead = false,
                    entityId = "boq_tower_b_rev2",
                    actionUrl = "/boq/boq_tower_b_rev2",
                    createdAt = "4 hours ago"
                ),
                NotificationItem(
                    id = "notif_003",
                    organizationId = "org_apex_infra",
                    userId = "usr_pm_rahul",
                    projectId = "prj_commercial_tower_b",
                    title = "Contractor Bill Submitted: Royal Infra RA #4",
                    message = "RA Bill #4 (Gross ₹4,85,000) submitted for work done on 6th floor slab casting.",
                    type = NotificationType.BILL_SUBMITTED,
                    isRead = true,
                    entityId = "bill_royal_infra_001",
                    actionUrl = "/finance/bills",
                    createdAt = "1 day ago"
                ),
                NotificationItem(
                    id = "notif_004",
                    organizationId = "org_apex_infra",
                    userId = "usr_pm_rahul",
                    projectId = "prj_commercial_tower_b",
                    title = "Drawing Revision: Arch Plan v2.1",
                    message = "Priya Sharma uploaded revised typical floor plan v2.1 with facade alignment notes.",
                    type = NotificationType.DOCUMENT_UPLOADED,
                    isRead = true,
                    entityId = "doc_arch_floor_plan",
                    actionUrl = "/documents/doc_arch_floor_plan",
                    createdAt = "2 days ago"
                )
            )
        )
    }

    suspend fun getNotifications(): Result<List<NotificationItem>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getNotifications()
            if (response.isSuccessful && response.body() != null) {
                val list = response.body()!!.map { mapNetworkToDomain(it) }
                localNotifications.clear()
                localNotifications.addAll(list)
                Result.success(list)
            } else {
                Result.success(localNotifications.toList())
            }
        } catch (e: Exception) {
            Result.success(localNotifications.toList())
        }
    }

    suspend fun getUnreadCount(): Long = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUnreadCount()
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.unreadCount
            } else {
                localNotifications.count { !it.isRead }.toLong()
            }
        } catch (e: Exception) {
            localNotifications.count { !it.isRead }.toLong()
        }
    }

    suspend fun markAsRead(notificationId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val index = localNotifications.indexOfFirst { it.id == notificationId }
        if (index != -1) {
            localNotifications[index] = localNotifications[index].copy(isRead = true)
        }
        try {
            apiService.markNotificationAsRead(notificationId)
        } catch (_: Exception) {}
        Result.success(Unit)
    }

    suspend fun markAllAsRead(): Result<Unit> = withContext(Dispatchers.IO) {
        for (i in localNotifications.indices) {
            localNotifications[i] = localNotifications[i].copy(isRead = true)
        }
        try {
            apiService.markAllNotificationsAsRead()
        } catch (_: Exception) {}
        Result.success(Unit)
    }

    private fun mapNetworkToDomain(dto: NotificationNetworkDto): NotificationItem {
        val type = try {
            NotificationType.valueOf(dto.type)
        } catch (e: Exception) {
            NotificationType.APPROVAL_REQUIRED
        }
        return NotificationItem(
            id = dto.id,
            organizationId = dto.organizationId,
            userId = dto.userId,
            projectId = dto.projectId,
            title = dto.title,
            message = dto.message,
            type = type,
            isRead = dto.isRead,
            entityId = dto.entityId,
            actionUrl = dto.actionUrl,
            createdAt = dto.createdAt
        )
    }
}
