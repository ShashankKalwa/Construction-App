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
    private val localProgressCache = ConcurrentHashMap<String, MutableList<WorkProgress>>()
    private val localInspectionCache = ConcurrentHashMap<String, MutableList<SiteInspection>>()
    private val localDefectCache = ConcurrentHashMap<String, MutableList<SiteDefect>>()
    private val localPhotoCache = ConcurrentHashMap<String, MutableList<SitePhoto>>()
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
            status = DsrStatus.APPROVED,
            approvedBy = "Vikram Sharma (PM)",
            approvedAt = "2026-03-04T19:30:00Z",
            approvalNotes = "Pour records verified against batch tickets. Good progress.",
            createdBy = "Site Supervisor",
            laborAttendances = attendances,
            totalHeadcount = 42,
            createdAt = "2026-03-04T18:00:00Z"
        )
        localReportCache[projectId] = mutableListOf(report)

        val progressList = mutableListOf(
            WorkProgress(
                id = "wp_1",
                projectId = projectId,
                taskId = "task_2",
                reportId = "dsr_1",
                itemName = "RCC Column Pouring C1-C8 (M35)",
                unit = "m³",
                plannedQuantity = BigDecimal("50.0"),
                reportedQuantity = BigDecimal("42.0"),
                verifiedQuantity = BigDecimal("40.0"),
                status = WorkProgressStatus.VERIFIED,
                reportedBy = "Site Supervisor",
                verifiedBy = "Vikram Sharma (PM)",
                verificationNotes = "2 m³ deduction for column C7 overpour slump test variance",
                recordedAt = "2026-03-04T18:30:00Z",
                verifiedAt = "2026-03-04T19:45:00Z"
            ),
            WorkProgress(
                id = "wp_2",
                projectId = projectId,
                taskId = "task_3",
                reportId = "dsr_1",
                itemName = "Level 1 Slab Rebar Fabrication",
                unit = "MT",
                plannedQuantity = BigDecimal("15.0"),
                reportedQuantity = BigDecimal("8.5"),
                verifiedQuantity = BigDecimal.ZERO,
                status = WorkProgressStatus.REPORTED,
                reportedBy = "Site Supervisor",
                verifiedBy = null,
                verificationNotes = null,
                recordedAt = "2026-03-04T18:30:00Z",
                verifiedAt = null
            )
        )
        localProgressCache[projectId] = progressList

        val inspections = mutableListOf(
            SiteInspection(
                id = "insp_1",
                projectId = projectId,
                title = "Ground Floor Columns Pre-Pour QC Checklist",
                inspectionType = InspectionType.QUALITY,
                status = InspectionStatus.PASSED,
                checklistData = "[{\"item\":\"Cover spacers 40mm installed @ 1m pitch\",\"passed\":true},{\"item\":\"Formwork plumbness within ±5mm tolerance\",\"passed\":true},{\"item\":\"Pour line clear of debris and slurry washed\",\"passed\":true}]",
                inspectorId = "Site Engineer",
                inspectionDate = "2026-03-04",
                location = "Grid Line A1-C8",
                notes = "All 8 columns approved for M35 concrete pour.",
                createdAt = "2026-03-04T11:00:00Z"
            ),
            SiteInspection(
                id = "insp_2",
                projectId = projectId,
                title = "Level 1 Perimeter Safety & Fall Protection",
                inspectionType = InspectionType.SAFETY,
                status = InspectionStatus.FAILED,
                checklistData = "[{\"item\":\"Perimeter guardrails 1.1m with intermediate rail\",\"passed\":false},{\"item\":\"Toe boards installed along edges\",\"passed\":false},{\"item\":\"Safety harnesses hooked to lifeline\",\"passed\":true}]",
                inspectorId = "Safety Officer",
                inspectionDate = "2026-03-04",
                location = "Level 1 East Perimeter",
                notes = "Defects auto-flagged. Work on east perimeter halted until barricades installed.",
                createdAt = "2026-03-04T14:15:00Z"
            )
        )
        localInspectionCache[projectId] = inspections

        val defects = mutableListOf(
            SiteDefect(
                id = "def_1",
                projectId = projectId,
                inspectionId = "insp_2",
                title = "Missing edge guardrails on Level 1 east face",
                description = "East elevation edge lacks top and mid guardrails. High fall hazard for slab staging crew.",
                location = "Level 1 East Perimeter",
                severity = IssueSeverity.CRITICAL,
                status = DefectStatus.OPEN,
                assignedTo = "Safety Officer",
                deadline = "2026-03-05",
                createdBy = "Safety Officer",
                createdAt = "2026-03-04T14:15:00Z",
                resolvedAt = null,
                reInspectedBy = null,
                reInspectedAt = null,
                reInspectionNotes = null
            ),
            SiteDefect(
                id = "def_2",
                projectId = projectId,
                inspectionId = "insp_1",
                title = "Column C4 cover spacer block shifted",
                description = "Rebar cage shifted by 12mm towards shuttering plate. Corrected with heavy duty PVC spacer blocks.",
                location = "Column C4 Ground Floor",
                severity = IssueSeverity.MEDIUM,
                status = DefectStatus.CLOSED,
                assignedTo = "Site Engineer",
                deadline = "2026-03-04",
                createdBy = "QC Inspector",
                createdAt = "2026-03-04T09:30:00Z",
                resolvedAt = "2026-03-04T10:45:00Z",
                reInspectedBy = "QC Inspector",
                reInspectedAt = "2026-03-04T11:00:00Z",
                reInspectionNotes = "Spacer re-anchored. Passed verification before concrete truck arrived."
            )
        )
        localDefectCache[projectId] = defects

        val photos = mutableListOf(
            SitePhoto(
                id = "photo_1",
                projectId = projectId,
                reportId = "dsr_1",
                taskId = "task_2",
                defectId = null,
                photoUrl = "https://images.unsplash.com/photo-1541888946425-d0fbb18615f8",
                caption = "RCC Pouring progress column C1-C8",
                locationTag = "Ground Floor",
                createdAt = "2026-03-04T16:00:00Z"
            )
        )
        localPhotoCache[projectId] = photos

        // Legacy issues seeding
        val issues = mutableListOf(
            SiteIssue(
                id = "issue_1",
                projectId = projectId,
                reportId = "dsr_1",
                title = "Column C4 cover block misalignment",
                description = "Rebar cage shifted by 15mm towards north shuttering plate.",
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
                description = "Slow dewatering observed after brief overnight drizzle.",
                severity = IssueSeverity.MEDIUM,
                status = IssueStatus.IN_PROGRESS,
                assignedTo = "Site Supervisor",
                createdBy = "Safety Officer",
                createdAt = "2026-03-04T08:15:00Z",
                resolvedAt = null
            )
        )
        localIssueCache[projectId] = issues
    }

    // -------------------------------------------------------------------------
    // Daily Site Reports
    // -------------------------------------------------------------------------

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
                    status = DsrStatus.SUBMITTED,
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
                status = DsrStatus.SUBMITTED,
                createdBy = "Current User",
                laborAttendances = laborAttendances,
                totalHeadcount = totalHeadcount,
                createdAt = "Just now"
            )
            localReportCache.getOrPut(projectId) { mutableListOf() }.add(0, local)
            Result.success(local)
        }
    }

    suspend fun approveDailyReport(
        projectId: String,
        reportId: String,
        action: String,
        notes: String?
    ): Result<DailySiteReport> = withContext(Dispatchers.IO) {
        try {
            val req = DsrApprovalRequest(action = action, notes = notes)
            val response = apiService.approveDailyReport(projectId, reportId, req)
            if (response.isSuccessful && response.body() != null) {
                val updated = mapDailyReportDto(response.body()!!)
                updateCachedReport(projectId, updated)
                Result.success(updated)
            } else {
                val newStatus = if (action.equals("APPROVE", ignoreCase = true)) DsrStatus.APPROVED else DsrStatus.REJECTED
                updateLocalReportStatus(projectId, reportId, newStatus, notes)
            }
        } catch (_: Exception) {
            val newStatus = if (action.equals("APPROVE", ignoreCase = true)) DsrStatus.APPROVED else DsrStatus.REJECTED
            updateLocalReportStatus(projectId, reportId, newStatus, notes)
        }
    }

    private fun updateLocalReportStatus(
        projectId: String,
        reportId: String,
        status: DsrStatus,
        notes: String?
    ): Result<DailySiteReport> {
        val list = localReportCache[projectId] ?: mutableListOf()
        val idx = list.indexOfFirst { it.id == reportId }
        if (idx >= 0) {
            val existing = list[idx]
            val updated = existing.copy(
                status = status,
                approvedBy = "Project Manager",
                approvedAt = "Just now",
                approvalNotes = notes
            )
            list[idx] = updated
            return Result.success(updated)
        }
        return Result.failure(Exception("Report not found"))
    }

    private fun updateCachedReport(projectId: String, report: DailySiteReport) {
        val list = localReportCache[projectId] ?: mutableListOf()
        val idx = list.indexOfFirst { it.id == report.id }
        if (idx >= 0) {
            list[idx] = report
        } else {
            list.add(0, report)
        }
        localReportCache[projectId] = list
    }

    // -------------------------------------------------------------------------
    // Work Progress Tracking (Reported vs Verified Quantities)
    // -------------------------------------------------------------------------

    suspend fun getWorkProgress(projectId: String): Result<List<WorkProgress>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getWorkProgress(projectId)
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    val mapped = response.body()!!.map { mapWorkProgressDto(it) }
                    localProgressCache[projectId] = mapped.toMutableList()
                    Result.success(mapped)
                } else {
                    Result.success(localProgressCache[projectId] ?: emptyList())
                }
            } catch (_: Exception) {
                Result.success(localProgressCache[projectId] ?: emptyList())
            }
        }

    suspend fun recordWorkProgress(
        projectId: String,
        taskId: String?,
        reportId: String?,
        itemName: String,
        unit: String,
        plannedQuantity: BigDecimal,
        reportedQuantity: BigDecimal
    ): Result<WorkProgress> = withContext(Dispatchers.IO) {
        try {
            val req = CreateWorkProgressRequest(
                taskId = taskId,
                reportId = reportId,
                itemName = itemName,
                unit = unit,
                plannedQuantity = plannedQuantity,
                reportedQuantity = reportedQuantity
            )
            val response = apiService.recordWorkProgress(projectId, req)
            if (response.isSuccessful && response.body() != null) {
                val created = mapWorkProgressDto(response.body()!!)
                localProgressCache.getOrPut(projectId) { mutableListOf() }.add(0, created)
                Result.success(created)
            } else {
                val local = WorkProgress(
                    id = "wp_" + UUID.randomUUID().toString().take(8),
                    projectId = projectId,
                    taskId = taskId,
                    reportId = reportId,
                    itemName = itemName,
                    unit = unit,
                    plannedQuantity = plannedQuantity,
                    reportedQuantity = reportedQuantity,
                    verifiedQuantity = BigDecimal.ZERO,
                    status = WorkProgressStatus.REPORTED,
                    reportedBy = "Site Supervisor",
                    recordedAt = "Just now"
                )
                localProgressCache.getOrPut(projectId) { mutableListOf() }.add(0, local)
                Result.success(local)
            }
        } catch (_: Exception) {
            val local = WorkProgress(
                id = "wp_" + UUID.randomUUID().toString().take(8),
                projectId = projectId,
                taskId = taskId,
                reportId = reportId,
                itemName = itemName,
                unit = unit,
                plannedQuantity = plannedQuantity,
                reportedQuantity = reportedQuantity,
                verifiedQuantity = BigDecimal.ZERO,
                status = WorkProgressStatus.REPORTED,
                reportedBy = "Site Supervisor",
                recordedAt = "Just now"
            )
            localProgressCache.getOrPut(projectId) { mutableListOf() }.add(0, local)
            Result.success(local)
        }
    }

    suspend fun verifyWorkProgress(
        projectId: String,
        progressId: String,
        action: String,
        verifiedQuantity: BigDecimal,
        notes: String?
    ): Result<WorkProgress> = withContext(Dispatchers.IO) {
        try {
            val req = VerifyWorkProgressRequest(
                action = action,
                verifiedQuantity = verifiedQuantity,
                notes = notes
            )
            val response = apiService.verifyWorkProgress(projectId, progressId, req)
            if (response.isSuccessful && response.body() != null) {
                val updated = mapWorkProgressDto(response.body()!!)
                updateCachedProgress(projectId, updated)
                Result.success(updated)
            } else {
                val newStatus = if (action.equals("VERIFY", ignoreCase = true)) WorkProgressStatus.VERIFIED else WorkProgressStatus.REJECTED
                updateLocalProgressStatus(projectId, progressId, newStatus, verifiedQuantity, notes)
            }
        } catch (_: Exception) {
            val newStatus = if (action.equals("VERIFY", ignoreCase = true)) WorkProgressStatus.VERIFIED else WorkProgressStatus.REJECTED
            updateLocalProgressStatus(projectId, progressId, newStatus, verifiedQuantity, notes)
        }
    }

    private fun updateLocalProgressStatus(
        projectId: String,
        progressId: String,
        status: WorkProgressStatus,
        verifiedQuantity: BigDecimal,
        notes: String?
    ): Result<WorkProgress> {
        val list = localProgressCache[projectId] ?: mutableListOf()
        val idx = list.indexOfFirst { it.id == progressId }
        if (idx >= 0) {
            val existing = list[idx]
            val updated = existing.copy(
                status = status,
                verifiedQuantity = verifiedQuantity,
                verifiedBy = "Civil Engineer",
                verificationNotes = notes,
                verifiedAt = "Just now"
            )
            list[idx] = updated
            return Result.success(updated)
        }
        return Result.failure(Exception("Progress record not found"))
    }

    private fun updateCachedProgress(projectId: String, progress: WorkProgress) {
        val list = localProgressCache[projectId] ?: mutableListOf()
        val idx = list.indexOfFirst { it.id == progress.id }
        if (idx >= 0) {
            list[idx] = progress
        } else {
            list.add(0, progress)
        }
        localProgressCache[projectId] = list
    }

    // -------------------------------------------------------------------------
    // Digital Site Inspections
    // -------------------------------------------------------------------------

    suspend fun getInspections(projectId: String): Result<List<SiteInspection>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getInspections(projectId)
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    val mapped = response.body()!!.map { mapInspectionDto(it) }
                    localInspectionCache[projectId] = mapped.toMutableList()
                    Result.success(mapped)
                } else {
                    Result.success(localInspectionCache[projectId] ?: emptyList())
                }
            } catch (_: Exception) {
                Result.success(localInspectionCache[projectId] ?: emptyList())
            }
        }

    suspend fun createInspection(
        projectId: String,
        title: String,
        inspectionType: InspectionType,
        status: InspectionStatus,
        checklistData: String,
        inspectionDate: String?,
        location: String?,
        notes: String?
    ): Result<SiteInspection> = withContext(Dispatchers.IO) {
        try {
            val req = CreateSiteInspectionRequest(
                title = title,
                inspectionType = inspectionType.name,
                status = status.name,
                checklistData = checklistData,
                inspectionDate = inspectionDate,
                location = location,
                notes = notes
            )
            val response = apiService.createInspection(projectId, req)
            if (response.isSuccessful && response.body() != null) {
                val created = mapInspectionDto(response.body()!!)
                localInspectionCache.getOrPut(projectId) { mutableListOf() }.add(0, created)
                Result.success(created)
            } else {
                val local = SiteInspection(
                    id = "insp_" + UUID.randomUUID().toString().take(8),
                    projectId = projectId,
                    title = title,
                    inspectionType = inspectionType,
                    status = status,
                    checklistData = checklistData,
                    inspectorId = "Current User",
                    inspectionDate = inspectionDate ?: "2026-03-04",
                    location = location,
                    notes = notes,
                    createdAt = "Just now"
                )
                localInspectionCache.getOrPut(projectId) { mutableListOf() }.add(0, local)
                Result.success(local)
            }
        } catch (_: Exception) {
            val local = SiteInspection(
                id = "insp_" + UUID.randomUUID().toString().take(8),
                projectId = projectId,
                title = title,
                inspectionType = inspectionType,
                status = status,
                checklistData = checklistData,
                inspectorId = "Current User",
                inspectionDate = inspectionDate ?: "2026-03-04",
                location = location,
                notes = notes,
                createdAt = "Just now"
            )
            localInspectionCache.getOrPut(projectId) { mutableListOf() }.add(0, local)
            Result.success(local)
        }
    }

    // -------------------------------------------------------------------------
    // Snag List & Defects Management
    // -------------------------------------------------------------------------

    suspend fun getDefects(
        projectId: String,
        statusFilter: DefectStatus? = null
    ): Result<List<SiteDefect>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDefects(projectId, statusFilter?.name)
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                val mapped = response.body()!!.map { mapDefectDto(it) }
                localDefectCache[projectId] = mapped.toMutableList()
                Result.success(mapped)
            } else {
                val cached = localDefectCache[projectId] ?: emptyList()
                val filtered = if (statusFilter != null) cached.filter { it.status == statusFilter } else cached
                Result.success(filtered)
            }
        } catch (_: Exception) {
            val cached = localDefectCache[projectId] ?: emptyList()
            val filtered = if (statusFilter != null) cached.filter { it.status == statusFilter } else cached
            Result.success(filtered)
        }
    }

    suspend fun createDefect(
        projectId: String,
        inspectionId: String?,
        title: String,
        description: String,
        location: String?,
        severity: IssueSeverity,
        assignedTo: String?,
        deadline: String?
    ): Result<SiteDefect> = withContext(Dispatchers.IO) {
        try {
            val req = CreateSiteDefectRequest(
                inspectionId = inspectionId,
                title = title,
                description = description,
                location = location,
                severity = severity.name,
                assignedTo = assignedTo,
                deadline = deadline
            )
            val response = apiService.createDefect(projectId, req)
            if (response.isSuccessful && response.body() != null) {
                val created = mapDefectDto(response.body()!!)
                localDefectCache.getOrPut(projectId) { mutableListOf() }.add(0, created)
                Result.success(created)
            } else {
                val local = SiteDefect(
                    id = "def_" + UUID.randomUUID().toString().take(8),
                    projectId = projectId,
                    inspectionId = inspectionId,
                    title = title,
                    description = description,
                    location = location,
                    severity = severity,
                    status = DefectStatus.OPEN,
                    assignedTo = assignedTo,
                    deadline = deadline,
                    createdBy = "Current User",
                    createdAt = "Just now"
                )
                localDefectCache.getOrPut(projectId) { mutableListOf() }.add(0, local)
                Result.success(local)
            }
        } catch (_: Exception) {
            val local = SiteDefect(
                id = "def_" + UUID.randomUUID().toString().take(8),
                projectId = projectId,
                inspectionId = inspectionId,
                title = title,
                description = description,
                location = location,
                severity = severity,
                status = DefectStatus.OPEN,
                assignedTo = assignedTo,
                deadline = deadline,
                createdBy = "Current User",
                createdAt = "Just now"
            )
            localDefectCache.getOrPut(projectId) { mutableListOf() }.add(0, local)
            Result.success(local)
        }
    }

    suspend fun updateDefectStatus(
        projectId: String,
        defectId: String,
        status: DefectStatus
    ): Result<SiteDefect> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateDefectStatus(defectId, UpdateSiteDefectStatusRequest(status.name))
            if (response.isSuccessful && response.body() != null) {
                val updated = mapDefectDto(response.body()!!)
                updateCachedDefect(projectId, updated)
                Result.success(updated)
            } else {
                updateLocalDefectStatus(projectId, defectId, status)
            }
        } catch (_: Exception) {
            updateLocalDefectStatus(projectId, defectId, status)
        }
    }

    suspend fun reinspectDefect(
        projectId: String,
        defectId: String,
        action: String,
        notes: String?
    ): Result<SiteDefect> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.reinspectDefect(defectId, ReinspectSiteDefectRequest(action, notes))
            if (response.isSuccessful && response.body() != null) {
                val updated = mapDefectDto(response.body()!!)
                updateCachedDefect(projectId, updated)
                Result.success(updated)
            } else {
                val newStatus = if (action.equals("APPROVE_CLOSE", ignoreCase = true)) DefectStatus.CLOSED else DefectStatus.OPEN
                updateLocalDefectReinspection(projectId, defectId, newStatus, notes)
            }
        } catch (_: Exception) {
            val newStatus = if (action.equals("APPROVE_CLOSE", ignoreCase = true)) DefectStatus.CLOSED else DefectStatus.OPEN
            updateLocalDefectReinspection(projectId, defectId, newStatus, notes)
        }
    }

    private fun updateLocalDefectStatus(projectId: String, defectId: String, status: DefectStatus): Result<SiteDefect> {
        val list = localDefectCache[projectId] ?: mutableListOf()
        val idx = list.indexOfFirst { it.id == defectId }
        if (idx >= 0) {
            val existing = list[idx]
            val resolvedAt = if (status == DefectStatus.RESOLVED || status == DefectStatus.CLOSED) "Just now" else null
            val updated = existing.copy(status = status, resolvedAt = resolvedAt)
            list[idx] = updated
            return Result.success(updated)
        }
        return Result.failure(Exception("Defect not found"))
    }

    private fun updateLocalDefectReinspection(
        projectId: String,
        defectId: String,
        status: DefectStatus,
        notes: String?
    ): Result<SiteDefect> {
        val list = localDefectCache[projectId] ?: mutableListOf()
        val idx = list.indexOfFirst { it.id == defectId }
        if (idx >= 0) {
            val existing = list[idx]
            val updated = existing.copy(
                status = status,
                reInspectedBy = "QC Inspector",
                reInspectedAt = "Just now",
                reInspectionNotes = notes
            )
            list[idx] = updated
            return Result.success(updated)
        }
        return Result.failure(Exception("Defect not found"))
    }

    private fun updateCachedDefect(projectId: String, defect: SiteDefect) {
        val list = localDefectCache[projectId] ?: mutableListOf()
        val idx = list.indexOfFirst { it.id == defect.id }
        if (idx >= 0) {
            list[idx] = defect
        } else {
            list.add(0, defect)
        }
        localDefectCache[projectId] = list
    }

    // -------------------------------------------------------------------------
    // Site Photo Evidence
    // -------------------------------------------------------------------------

    suspend fun getSitePhotos(projectId: String): Result<List<SitePhoto>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSitePhotos(projectId)
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    val mapped = response.body()!!.map { mapSitePhotoDto(it) }
                    localPhotoCache[projectId] = mapped.toMutableList()
                    Result.success(mapped)
                } else {
                    Result.success(localPhotoCache[projectId] ?: emptyList())
                }
            } catch (_: Exception) {
                Result.success(localPhotoCache[projectId] ?: emptyList())
            }
        }

    suspend fun createSitePhoto(
        projectId: String,
        reportId: String?,
        taskId: String?,
        defectId: String?,
        photoUrl: String,
        caption: String?,
        locationTag: String?
    ): Result<SitePhoto> = withContext(Dispatchers.IO) {
        try {
            val req = CreateSitePhotoRequest(
                reportId = reportId,
                taskId = taskId,
                defectId = defectId,
                photoUrl = photoUrl,
                caption = caption,
                locationTag = locationTag
            )
            val response = apiService.createSitePhoto(projectId, req)
            if (response.isSuccessful && response.body() != null) {
                val created = mapSitePhotoDto(response.body()!!)
                localPhotoCache.getOrPut(projectId) { mutableListOf() }.add(0, created)
                Result.success(created)
            } else {
                val local = SitePhoto(
                    id = "photo_" + UUID.randomUUID().toString().take(8),
                    projectId = projectId,
                    reportId = reportId,
                    taskId = taskId,
                    defectId = defectId,
                    photoUrl = photoUrl,
                    caption = caption,
                    locationTag = locationTag,
                    createdAt = "Just now"
                )
                localPhotoCache.getOrPut(projectId) { mutableListOf() }.add(0, local)
                Result.success(local)
            }
        } catch (_: Exception) {
            val local = SitePhoto(
                id = "photo_" + UUID.randomUUID().toString().take(8),
                projectId = projectId,
                reportId = reportId,
                taskId = taskId,
                defectId = defectId,
                photoUrl = photoUrl,
                caption = caption,
                locationTag = locationTag,
                createdAt = "Just now"
            )
            localPhotoCache.getOrPut(projectId) { mutableListOf() }.add(0, local)
            Result.success(local)
        }
    }

    // -------------------------------------------------------------------------
    // Legacy Site Issues (Backward Compatibility)
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Mappers
    // -------------------------------------------------------------------------

    private fun mapDailyReportDto(dto: DailySiteReportNetworkDto): DailySiteReport {
        val weather = try {
            WeatherCondition.valueOf(dto.weather.uppercase())
        } catch (_: Exception) {
            WeatherCondition.SUNNY
        }
        val status = try {
            DsrStatus.valueOf(dto.status.uppercase())
        } catch (_: Exception) {
            DsrStatus.SUBMITTED
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
            status = status,
            approvedBy = dto.approvedBy,
            approvedAt = dto.approvedAt,
            approvalNotes = dto.approvalNotes,
            createdBy = dto.createdBy,
            laborAttendances = attendances,
            totalHeadcount = dto.totalHeadcount,
            createdAt = dto.createdAt
        )
    }

    private fun mapWorkProgressDto(dto: WorkProgressNetworkDto): WorkProgress {
        val status = try {
            WorkProgressStatus.valueOf(dto.status.uppercase())
        } catch (_: Exception) {
            WorkProgressStatus.REPORTED
        }
        return WorkProgress(
            id = dto.id,
            projectId = dto.projectId,
            taskId = dto.taskId,
            reportId = dto.reportId,
            itemName = dto.itemName,
            unit = dto.unit,
            plannedQuantity = dto.plannedQuantity,
            reportedQuantity = dto.reportedQuantity,
            verifiedQuantity = dto.verifiedQuantity,
            status = status,
            reportedBy = dto.reportedBy,
            verifiedBy = dto.verifiedBy,
            verificationNotes = dto.verificationNotes,
            recordedAt = dto.recordedAt,
            verifiedAt = dto.verifiedAt
        )
    }

    private fun mapInspectionDto(dto: SiteInspectionNetworkDto): SiteInspection {
        val type = try {
            InspectionType.valueOf(dto.inspectionType.uppercase())
        } catch (_: Exception) {
            InspectionType.QUALITY
        }
        val status = try {
            InspectionStatus.valueOf(dto.status.uppercase())
        } catch (_: Exception) {
            InspectionStatus.PASSED
        }
        return SiteInspection(
            id = dto.id,
            projectId = dto.projectId,
            title = dto.title,
            inspectionType = type,
            status = status,
            checklistData = dto.checklistData,
            inspectorId = dto.inspectorId,
            inspectionDate = dto.inspectionDate,
            location = dto.location,
            notes = dto.notes,
            createdAt = dto.createdAt
        )
    }

    private fun mapDefectDto(dto: SiteDefectNetworkDto): SiteDefect {
        val severity = try {
            IssueSeverity.valueOf(dto.severity.uppercase())
        } catch (_: Exception) {
            IssueSeverity.MEDIUM
        }
        val status = try {
            DefectStatus.valueOf(dto.status.uppercase())
        } catch (_: Exception) {
            DefectStatus.OPEN
        }
        return SiteDefect(
            id = dto.id,
            projectId = dto.projectId,
            inspectionId = dto.inspectionId,
            title = dto.title,
            description = dto.description,
            location = dto.location,
            severity = severity,
            status = status,
            assignedTo = dto.assignedTo,
            deadline = dto.deadline,
            createdBy = dto.createdBy,
            createdAt = dto.createdAt,
            resolvedAt = dto.resolvedAt,
            reInspectedBy = dto.reInspectedBy,
            reInspectedAt = dto.reInspectedAt,
            reInspectionNotes = dto.reInspectionNotes
        )
    }

    private fun mapSitePhotoDto(dto: SitePhotoNetworkDto): SitePhoto {
        return SitePhoto(
            id = dto.id,
            projectId = dto.projectId,
            reportId = dto.reportId,
            taskId = dto.taskId,
            defectId = dto.defectId,
            photoUrl = dto.photoUrl,
            caption = dto.caption,
            locationTag = dto.locationTag,
            createdAt = dto.capturedAt
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
