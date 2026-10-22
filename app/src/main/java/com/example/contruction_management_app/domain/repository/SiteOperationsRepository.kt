package com.example.contruction_management_app.domain.repository

import com.example.contruction_management_app.core.network.*
import com.example.contruction_management_app.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class SiteOperationsRepository(private val apiService: CmsApiService) {

    private val localReportCache = ConcurrentHashMap<String, MutableList<DailySiteReport>>()
    private val localIssueCache = ConcurrentHashMap<String, MutableList<SiteIssue>>()

    init {
        seedInitialDemoSiteOps("prj_commercial_tower_b")
    }

    private fun seedInitialDemoSiteOps(projectId: String) {
        val attendances = listOf(
            LaborAttendance("la_1", LaborTrade.MASON, 8, BigDecimal("8.0"), "Apex Masonry Works"),
            LaborAttendance("la_2", LaborTrade.STEEL_FIXER, 12, BigDecimal("8.5"), "Reliable Reinforcements Ltd"),
            LaborAttendance("la_3", LaborTrade.CARPENTER, 6, BigDecimal("8.0"), "Modern Shuttering Corp"),
            LaborAttendance("la_4", LaborTrade.HELPER, 16, BigDecimal("8.0"), "General Site Labor Pool")
        )

        val report = DailySiteReport(
            id = "dsr_1",
            projectId = projectId,
            reportDate = "2026-03-04",
            weather = WeatherCondition.SUNNY,
            workSummary = "Pouring RCC columns C1 through C8 on Ground Floor. Shuttering and scaffolding for Level 1 slab in progress. Total concrete poured: 42 m³.",
            plannedTomorrow = "Continue rebar fabrication for Level 1 slab. Strip column forms after 24 hrs. Inspection with structural consultant at 2:00 PM.",
            createdBy = "Site Supervisor",
            laborAttendances = attendances,
            totalHeadcount = 42,
            createdAt = "2026-03-04T18:00:00Z"
        )
        localReportCache[projectId] = mutableListOf(report)

        val issues = mutableListOf(
            SiteIssue(
                id = "issue_1",
                projectId = projectId,
                reportId = "dsr_1",
                title = "Column C4 cover block misalignment",
                description = "Rebar cage shifted by 15mm towards north shuttering plate. Corrective cover block spacer required prior to pour.",
                severity = IssueSeverity.HIGH,
                status = IssueStatus.RESOLVED,
                assignedTo = "Site Engineer",
                createdBy = "QC Inspector",
                createdAt = "2026-03-04T09:30:00Z",
                resolvedAt = "2026-03-04T11:00:00Z"
            ),
            SiteIssue(
                id = "issue_2",
                projectId = projectId,
                reportId = "dsr_1",
                title = "Water ponding near excavated grid line D",
                description = "Slow dewatering observed after brief overnight drizzle. Need auxiliary 5HP pump stationed at south sump.",
                severity = IssueSeverity.MEDIUM,
                status = IssueStatus.IN_PROGRESS,
                assignedTo = "Site Supervisor",
                createdBy = "Safety Officer",
                createdAt = "2026-03-04T08:15:00Z",
                resolvedAt = null
            ),
            SiteIssue(
                id = "issue_3",
                projectId = projectId,
                reportId = null,
                title = "Missing edge protection handrails on stairwell shaft",
                description = "Stairwell shaft opening at podium level missing double guardrails and toe boards.",
                severity = IssueSeverity.CRITICAL,
                status = IssueStatus.OPEN,
                assignedTo = "Safety Officer",
                createdBy = "Project Manager",
                createdAt = "2026-03-04T14:20:00Z",
                resolvedAt = null
            )
        )
        localIssueCache[projectId] = issues
    }

    suspend fun getDailyReports(projectId: String): Result<List<DailySiteReport>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getDailyReports(projectId)
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    val mapped = response.body()!!.map { mapDailyReportDto(it) }
                    localReportCache[projectId] = mapped.toMutableList()
                    Result.success(mapped)
                } else {
                    Result.success(localReportCache[projectId] ?: emptyList())
                }
            } catch (_: Exception) {
                Result.success(localReportCache[projectId] ?: emptyList())
            }
        }

    suspend fun createDailyReport(
        projectId: String,
        reportDate: String,
        weather: WeatherCondition,
        workSummary: String,
        plannedTomorrow: String?,
        laborAttendances: List<LaborAttendance>
    ): Result<DailySiteReport> = withContext(Dispatchers.IO) {
        val totalHeadcount = laborAttendances.sumOf { it.headcount }
        try {
            val netLaborList = laborAttendances.map {
                LaborAttendanceNetworkDto(
                    trade = it.trade.name,
                    headcount = it.headcount,
                    hoursWorked = it.hoursWorked,
                    contractorName = it.contractorName
                )
            }
            val req = CreateDailySiteReportRequest(
                reportDate = reportDate,
                weather = weather.name,
                workSummary = workSummary,
                plannedTomorrow = plannedTomorrow,
                laborAttendances = netLaborList
            )
            val response = apiService.createDailyReport(projectId, req)
            if (response.isSuccessful && response.body() != null) {
                val created = mapDailyReportDto(response.body()!!)
                localReportCache.getOrPut(projectId) { mutableListOf() }.add(0, created)
                Result.success(created)
            } else {
                val local = DailySiteReport(
                    id = "dsr_" + UUID.randomUUID().toString().take(8),
                    projectId = projectId,
                    reportDate = reportDate,
                    weather = weather,
                    workSummary = workSummary,
                    plannedTomorrow = plannedTomorrow,
                    createdBy = "Current User",
                    laborAttendances = laborAttendances,
                    totalHeadcount = totalHeadcount,
                    createdAt = "Just now"
                )
                localReportCache.getOrPut(projectId) { mutableListOf() }.add(0, local)
                Result.success(local)
            }
        } catch (_: Exception) {
            val local = DailySiteReport(
                id = "dsr_" + UUID.randomUUID().toString().take(8),
                projectId = projectId,
                reportDate = reportDate,
                weather = weather,
                workSummary = workSummary,
                plannedTomorrow = plannedTomorrow,
                createdBy = "Current User",
                laborAttendances = laborAttendances,
                totalHeadcount = totalHeadcount,
                createdAt = "Just now"
            )
            localReportCache.getOrPut(projectId) { mutableListOf() }.add(0, local)
            Result.success(local)
        }
    }

    suspend fun getIssues(
        projectId: String,
        statusFilter: IssueStatus? = null
    ): Result<List<SiteIssue>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProjectIssues(projectId, statusFilter?.name)
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                val mapped = response.body()!!.map { mapIssueDto(it) }
                localIssueCache[projectId] = mapped.toMutableList()
                Result.success(mapped)
            } else {
                val cached = localIssueCache[projectId] ?: emptyList()
                val filtered = if (statusFilter != null) cached.filter { it.status == statusFilter } else cached
                Result.success(filtered)
            }
        } catch (_: Exception) {
            val cached = localIssueCache[projectId] ?: emptyList()
            val filtered = if (statusFilter != null) cached.filter { it.status == statusFilter } else cached
            Result.success(filtered)
        }
    }

    suspend fun createIssue(
        projectId: String,
        reportId: String?,
        title: String,
        description: String?,
        severity: IssueSeverity,
        assignedTo: String?
    ): Result<SiteIssue> = withContext(Dispatchers.IO) {
        try {
            val req = CreateSiteIssueRequest(
                reportId = reportId,
                title = title,
                description = description,
                severity = severity.name,
                assignedTo = assignedTo
            )
            val response = apiService.createIssue(projectId, req)
            if (response.isSuccessful && response.body() != null) {
                val created = mapIssueDto(response.body()!!)
                localIssueCache.getOrPut(projectId) { mutableListOf() }.add(0, created)
                Result.success(created)
            } else {
                val local = SiteIssue(
                    id = "issue_" + UUID.randomUUID().toString().take(8),
                    projectId = projectId,
                    reportId = reportId,
                    title = title,
                    description = description,
                    severity = severity,
                    status = IssueStatus.OPEN,
                    assignedTo = assignedTo,
                    createdBy = "Current User",
                    createdAt = "Just now",
                    resolvedAt = null
                )
                localIssueCache.getOrPut(projectId) { mutableListOf() }.add(0, local)
                Result.success(local)
            }
        } catch (_: Exception) {
            val local = SiteIssue(
                id = "issue_" + UUID.randomUUID().toString().take(8),
                projectId = projectId,
                reportId = reportId,
                title = title,
                description = description,
                severity = severity,
                status = IssueStatus.OPEN,
                assignedTo = assignedTo,
                createdBy = "Current User",
                createdAt = "Just now",
                resolvedAt = null
            )
            localIssueCache.getOrPut(projectId) { mutableListOf() }.add(0, local)
            Result.success(local)
        }
    }

    suspend fun updateIssueStatus(
        projectId: String,
        issueId: String,
        status: IssueStatus,
        resolutionNotes: String? = null
    ): Result<SiteIssue> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateIssueStatus(
                issueId,
                UpdateIssueStatusRequest(status.name, resolutionNotes)
            )
            if (response.isSuccessful && response.body() != null) {
                val updated = mapIssueDto(response.body()!!)
                updateCachedIssue(projectId, updated)
                Result.success(updated)
            } else {
                updateLocalIssueStatus(projectId, issueId, status)
            }
        } catch (_: Exception) {
            updateLocalIssueStatus(projectId, issueId, status)
        }
    }

    private fun updateLocalIssueStatus(projectId: String, issueId: String, status: IssueStatus): Result<SiteIssue> {
        val list = localIssueCache[projectId] ?: mutableListOf()
        val idx = list.indexOfFirst { it.id == issueId }
        if (idx >= 0) {
            val existing = list[idx]
            val resolvedAt = if (status == IssueStatus.RESOLVED || status == IssueStatus.CLOSED) "Just now" else null
            val updated = existing.copy(status = status, resolvedAt = resolvedAt)
            list[idx] = updated
            return Result.success(updated)
        }
        return Result.failure(Exception("Issue not found"))
    }

    private fun updateCachedIssue(projectId: String, issue: SiteIssue) {
        val list = localIssueCache[projectId] ?: mutableListOf()
        val idx = list.indexOfFirst { it.id == issue.id }
        if (idx >= 0) {
            list[idx] = issue
        } else {
            list.add(0, issue)
        }
        localIssueCache[projectId] = list
    }

    private fun mapDailyReportDto(dto: DailySiteReportNetworkDto): DailySiteReport {
        val weather = try {
            WeatherCondition.valueOf(dto.weather.uppercase())
        } catch (_: Exception) {
            WeatherCondition.SUNNY
        }
        val attendances = dto.laborAttendances.map {
            val trade = try {
                LaborTrade.valueOf(it.trade.uppercase())
            } catch (_: Exception) {
                LaborTrade.HELPER
            }
            LaborAttendance(
                id = it.id,
                trade = trade,
                headcount = it.headcount,
                hoursWorked = it.hoursWorked,
                contractorName = it.contractorName
            )
        }
        return DailySiteReport(
            id = dto.id,
            projectId = dto.projectId,
            reportDate = dto.reportDate,
            weather = weather,
            workSummary = dto.workSummary,
            plannedTomorrow = dto.plannedTomorrow,
            createdBy = dto.createdBy,
            laborAttendances = attendances,
            totalHeadcount = dto.totalHeadcount,
            createdAt = dto.createdAt
        )
    }

    private fun mapIssueDto(dto: SiteIssueNetworkDto): SiteIssue {
        val severity = try {
            IssueSeverity.valueOf(dto.severity.uppercase())
        } catch (_: Exception) {
            IssueSeverity.MEDIUM
        }
        val status = try {
            IssueStatus.valueOf(dto.status.uppercase())
        } catch (_: Exception) {
            IssueStatus.OPEN
        }
        return SiteIssue(
            id = dto.id,
            projectId = dto.projectId,
            reportId = dto.reportId,
            title = dto.title,
            description = dto.description,
            severity = severity,
            status = status,
            assignedTo = dto.assignedTo,
            createdBy = dto.createdBy,
            createdAt = dto.createdAt,
            resolvedAt = dto.resolvedAt
        )
    }
}
