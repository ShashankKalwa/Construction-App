package com.example.contruction_management_app.domain.repository

import com.example.contruction_management_app.core.network.AuditLogNetworkDto
import com.example.contruction_management_app.core.network.AuditSummaryNetworkDto
import com.example.contruction_management_app.core.network.CmsApiService
import com.example.contruction_management_app.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class AuditRepository(private val apiService: CmsApiService) {

    private val localAuditCache = ConcurrentHashMap<String, MutableList<AuditLogItem>>()

    init {
        seedInitialDemoAudit("prj_commercial_tower_b")
    }

    private fun seedInitialDemoAudit(projectId: String) {
        val list = mutableListOf(
            AuditLogItem(
                id = "audit_001",
                organizationId = "org_apex_infra",
                projectId = projectId,
                userId = "usr_pm_rahul",
                userName = "Rahul Verma",
                action = AuditAction.ISSUE_STOCK,
                entityType = AuditEntityType.STOCK,
                entityId = "mat_cement_opc53",
                details = "Issued 65.00 Bags of OPC 53 Cement to Task 4th Floor Slab Casting. Balance reduced to 85.00 Bags.",
                ipAddress = "192.168.1.42",
                createdAt = "2 hours ago"
            ),
            AuditLogItem(
                id = "audit_002",
                organizationId = "org_apex_infra",
                projectId = projectId,
                userId = "usr_pm_rahul",
                userName = "Rahul Verma",
                action = AuditAction.APPROVE,
                entityType = AuditEntityType.CONTRACTOR_BILL,
                entityId = "bill_royal_infra_001",
                details = "Approved Contractor RA Bill #4. Gross: ₹4,85,000. Retention (5%): ₹24,250. TDS (2%): ₹9,700. Net Payable: ₹4,51,050.",
                ipAddress = "192.168.1.42",
                createdAt = "1 day ago"
            ),
            AuditLogItem(
                id = "audit_003",
                organizationId = "org_apex_infra",
                projectId = projectId,
                userId = "usr_eng_priya",
                userName = "Priya Sharma",
                action = AuditAction.CREATE,
                entityType = AuditEntityType.DOCUMENT,
                entityId = "doc_arch_floor_plan",
                details = "Uploaded new revision v2.1 for Typical Floor Plan with facade alignment adjustments.",
                ipAddress = "192.168.1.88",
                createdAt = "2 days ago"
            ),
            AuditLogItem(
                id = "audit_004",
                organizationId = "org_apex_infra",
                projectId = projectId,
                userId = "usr_eng_priya",
                userName = "Priya Sharma",
                action = AuditAction.CREATE,
                entityType = AuditEntityType.BOQ,
                entityId = "boq_tower_b_rev2",
                details = "Created BOQ Revision 2 with revised superstructure reinforcement rates.",
                ipAddress = "192.168.1.88",
                createdAt = "3 days ago"
            ),
            AuditLogItem(
                id = "audit_005",
                organizationId = "org_apex_infra",
                projectId = projectId,
                userId = "usr_pm_rahul",
                userName = "Rahul Verma",
                action = AuditAction.APPROVE,
                entityType = AuditEntityType.EXPENSE,
                entityId = "exp_diesel_gen_001",
                details = "Approved site diesel expenditure of ₹14,500 for heavy crane operation.",
                ipAddress = "192.168.1.42",
                createdAt = "5 days ago"
            )
        )
        localAuditCache[projectId] = list
    }

    suspend fun getAuditLogs(projectId: String, typeFilter: AuditEntityType? = null): Result<List<AuditLogItem>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAuditLogs(projectId, typeFilter?.name)
                if (response.isSuccessful && response.body() != null) {
                    val networkLogs = response.body()!!.map { mapNetworkToDomain(it) }
                    localAuditCache[projectId] = networkLogs.toMutableList()
                    val filtered = if (typeFilter != null) networkLogs.filter { it.entityType == typeFilter } else networkDocs(networkLogs)
                    Result.success(filtered)
                } else {
                    val cached = localAuditCache[projectId] ?: emptyList()
                    val filtered = if (typeFilter != null) cached.filter { it.entityType == typeFilter } else cached
                    Result.success(filtered)
                }
            } catch (e: Exception) {
                val cached = localAuditCache[projectId] ?: emptyList()
                val filtered = if (typeFilter != null) cached.filter { it.entityType == typeFilter } else cached
                Result.success(filtered)
            }
        }

    private fun networkDocs(list: List<AuditLogItem>): List<AuditLogItem> = list

    suspend fun getAuditSummary(projectId: String): Result<AuditSummary> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAuditSummary(projectId)
            if (response.isSuccessful && response.body() != null) {
                val b = response.body()!!
                Result.success(
                    AuditSummary(
                        totalEvents = b.totalEvents,
                        approvalsCount = b.approvalsCount,
                        stockIssuesCount = b.stockIssuesCount,
                        documentsCount = b.documentsCount,
                        paymentsCount = b.paymentsCount
                    )
                )
            } else {
                computeLocalSummary(projectId)
            }
        } catch (e: Exception) {
            computeLocalSummary(projectId)
        }
    }

    private fun computeLocalSummary(projectId: String): Result<AuditSummary> {
        val list = localAuditCache[projectId] ?: emptyList()
        val total = list.size.toLong()
        val approvals = list.count { it.action == AuditAction.APPROVE }.toLong()
        val stock = list.count { it.action == AuditAction.ISSUE_STOCK || it.entityType == AuditEntityType.STOCK }.toLong()
        val docs = list.count { it.entityType == AuditEntityType.DOCUMENT }.toLong()
        val payments = list.count { it.action == AuditAction.PAY_BILL || it.entityType == AuditEntityType.CONTRACTOR_BILL }.toLong()
        return Result.success(AuditSummary(total, approvals, stock, docs, payments))
    }

    private fun mapNetworkToDomain(dto: AuditLogNetworkDto): AuditLogItem {
        val action = try {
            AuditAction.valueOf(dto.action)
        } catch (e: Exception) {
            AuditAction.CREATE
        }
        val entityType = try {
            AuditEntityType.valueOf(dto.entityType)
        } catch (e: Exception) {
            AuditEntityType.PROJECT
        }
        return AuditLogItem(
            id = dto.id,
            organizationId = dto.organizationId,
            projectId = dto.projectId,
            userId = dto.userId,
            userName = dto.userName,
            action = action,
            entityType = entityType,
            entityId = dto.entityId,
            details = dto.details,
            ipAddress = dto.ipAddress,
            createdAt = dto.createdAt
        )
    }
}
