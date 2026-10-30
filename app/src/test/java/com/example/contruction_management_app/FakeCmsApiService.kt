package com.example.contruction_management_app

import com.example.contruction_management_app.core.network.*
import retrofit2.Response
import java.math.BigDecimal

open class FakeCmsApiService : CmsApiService {
    override suspend fun login(request: LoginNetworkRequest): Response<LoginNetworkResponse> = throw UnsupportedOperationException()
    override suspend fun getMe(): Response<UserMeNetworkResponse> = throw UnsupportedOperationException()
    override suspend fun getProjects(): Response<List<ProjectNetworkDto>> = Response.success(emptyList())
    override suspend fun createProject(request: CreateProjectRequest): Response<ProjectNetworkDto> = throw UnsupportedOperationException()
    override suspend fun updateProject(id: String, request: UpdateProjectRequest): Response<ProjectNetworkDto> = throw UnsupportedOperationException()
    override suspend fun updateProjectStatus(id: String, request: UpdateProjectStatusRequest): Response<ProjectNetworkDto> = throw UnsupportedOperationException()
    override suspend fun getProjectDetail(id: String): Response<ProjectDetailNetworkDto> = throw UnsupportedOperationException()
    override suspend fun getProjectMembers(id: String): Response<List<ProjectMemberNetworkDto>> = Response.success(emptyList())
    override suspend fun assignProjectMember(id: String, request: AssignMemberRequest): Response<ProjectMemberNetworkDto> = throw UnsupportedOperationException()
    override suspend fun removeProjectMember(id: String, userId: String): Response<Unit> = Response.success(Unit)
    override suspend fun getPhasesWithTasks(projectId: String): Response<List<PhaseNetworkDto>> = throw UnsupportedOperationException()
    override suspend fun createPhase(projectId: String, request: CreatePhaseRequest): Response<PhaseNetworkDto> = throw UnsupportedOperationException()
    override suspend fun createTask(projectId: String, request: CreateTaskRequest): Response<TaskNetworkDto> = throw UnsupportedOperationException()
    override suspend fun updateTaskProgress(taskId: String, request: UpdateTaskProgressRequest): Response<TaskNetworkDto> = throw UnsupportedOperationException()
    override suspend fun submitTaskReview(taskId: String, request: TaskSubmitReviewRequest): Response<TaskNetworkDto> = throw UnsupportedOperationException()
    override suspend fun reviewTask(taskId: String, request: TaskReviewRequest): Response<TaskNetworkDto> = throw UnsupportedOperationException()
    override suspend fun getMilestones(projectId: String): Response<List<MilestoneNetworkDto>> = Response.success(emptyList())
    override suspend fun createMilestone(projectId: String, request: CreateMilestoneRequest): Response<MilestoneNetworkDto> = throw UnsupportedOperationException()
    override suspend fun updateMilestoneStatus(milestoneId: String, request: UpdateMilestoneStatusRequest): Response<MilestoneNetworkDto> = throw UnsupportedOperationException()
    override suspend fun getDashboardSummary(projectId: String?): Response<DashboardSummaryNetworkDto> = Response.success(
        DashboardSummaryNetworkDto(
            activeProjectsCount = 1,
            totalBudgetInr = BigDecimal("54000000.00"),
            overallProgressPct = 42.0,
            pendingTasksCount = 3,
            inProgressTasksCount = 5,
            blockedTasksCount = 1,
            completedTasksCount = 10,
            roleMetrics = mapOf("Active Projects" to "1", "Blocked Tasks" to "1")
        )
    )
    override suspend fun calculateConcrete(req: ConcreteNetworkRequest): Response<ConcreteNetworkResult> = throw UnsupportedOperationException()
    override suspend fun calculateSteel(req: SteelNetworkRequest): Response<SteelNetworkResult> = throw UnsupportedOperationException()
    override suspend fun calculateBrickwork(req: BrickworkNetworkRequest): Response<BrickworkNetworkResult> = throw UnsupportedOperationException()
    override suspend fun calculateExcavation(req: ExcavationNetworkRequest): Response<ExcavationNetworkResult> = throw UnsupportedOperationException()
    override suspend fun calculatePlaster(req: PlasterNetworkRequest): Response<PlasterNetworkResult> = throw UnsupportedOperationException()
    override suspend fun calculatePaint(req: PaintNetworkRequest): Response<PaintNetworkResult> = throw UnsupportedOperationException()
    override suspend fun convertUnits(req: UnitConvertNetworkRequest): Response<UnitConvertNetworkResult> = throw UnsupportedOperationException()
    override suspend fun saveCalculation(projectId: String, req: SaveCalculationRequest): Response<CalculationNetworkDto> = throw UnsupportedOperationException()
    override suspend fun getProjectCalculations(projectId: String, type: String?): Response<List<CalculationNetworkDto>> = Response.success(emptyList())
    override suspend fun getCalculationById(id: String): Response<CalculationNetworkDto> = throw UnsupportedOperationException()

    // BOQs
    override suspend fun getProjectBoqs(projectId: String): Response<List<BoqNetworkDto>> = Response.success(emptyList())
    override suspend fun createBoq(projectId: String, req: CreateBoqRequest): Response<BoqNetworkDto> = throw UnsupportedOperationException()
    override suspend fun getBoqById(id: String): Response<BoqNetworkDto> = throw UnsupportedOperationException()
    override suspend fun addBoqItem(id: String, req: CreateBoqItemRequest): Response<BoqItemNetworkDto> = throw UnsupportedOperationException()
    override suspend fun submitBoq(id: String): Response<BoqNetworkDto> = throw UnsupportedOperationException()
    override suspend fun decideBoq(id: String, req: BoqDecisionRequest): Response<BoqNetworkDto> = throw UnsupportedOperationException()

    // Measurements
    override suspend fun getProjectMeasurements(projectId: String, boqItemId: String?): Response<List<MeasurementNetworkDto>> = Response.success(emptyList())
    override suspend fun createMeasurement(projectId: String, req: CreateMeasurementRequest): Response<MeasurementNetworkDto> = throw UnsupportedOperationException()
    override suspend fun getMeasurementById(id: String): Response<MeasurementNetworkDto> = throw UnsupportedOperationException()

    // Site Ops
    override suspend fun getDailyReports(projectId: String): Response<List<DailySiteReportNetworkDto>> = Response.success(emptyList())
    override suspend fun createDailyReport(projectId: String, req: CreateDailySiteReportRequest): Response<DailySiteReportNetworkDto> = throw UnsupportedOperationException()
    override suspend fun approveDailyReport(projectId: String, reportId: String, req: DsrApprovalRequest): Response<DailySiteReportNetworkDto> = throw UnsupportedOperationException()

    // Work Progress
    override suspend fun getWorkProgress(projectId: String): Response<List<WorkProgressNetworkDto>> = Response.success(emptyList())
    override suspend fun recordWorkProgress(projectId: String, req: CreateWorkProgressRequest): Response<WorkProgressNetworkDto> = throw UnsupportedOperationException()
    override suspend fun verifyWorkProgress(projectId: String, progressId: String, req: VerifyWorkProgressRequest): Response<WorkProgressNetworkDto> = throw UnsupportedOperationException()

    // Inspections
    override suspend fun getInspections(projectId: String): Response<List<SiteInspectionNetworkDto>> = Response.success(emptyList())
    override suspend fun createInspection(projectId: String, req: CreateSiteInspectionRequest): Response<SiteInspectionNetworkDto> = throw UnsupportedOperationException()

    // Defects & Snags
    override suspend fun getDefects(projectId: String, status: String?): Response<List<SiteDefectNetworkDto>> = Response.success(emptyList())
    override suspend fun createDefect(projectId: String, req: CreateSiteDefectRequest): Response<SiteDefectNetworkDto> = throw UnsupportedOperationException()
    override suspend fun updateDefectStatus(id: String, req: UpdateSiteDefectStatusRequest): Response<SiteDefectNetworkDto> = throw UnsupportedOperationException()
    override suspend fun reinspectDefect(id: String, req: ReinspectSiteDefectRequest): Response<SiteDefectNetworkDto> = throw UnsupportedOperationException()

    // Photos
    override suspend fun getSitePhotos(projectId: String): Response<List<SitePhotoNetworkDto>> = Response.success(emptyList())
    override suspend fun createSitePhoto(projectId: String, req: CreateSitePhotoRequest): Response<SitePhotoNetworkDto> = throw UnsupportedOperationException()

    // Legacy Issues
    override suspend fun getProjectIssues(projectId: String, status: String?): Response<List<SiteIssueNetworkDto>> = Response.success(emptyList())
    override suspend fun createIssue(projectId: String, req: CreateSiteIssueRequest): Response<SiteIssueNetworkDto> = throw UnsupportedOperationException()
    override suspend fun updateIssueStatus(id: String, req: UpdateIssueStatusRequest): Response<SiteIssueNetworkDto> = throw UnsupportedOperationException()

    // Phase 4 / Materials & Inventory
    override suspend fun getMaterials(projectId: String): Response<List<MaterialNetworkDto>> = Response.success(emptyList())
    override suspend fun createMaterial(projectId: String, req: CreateMaterialRequest): Response<MaterialNetworkDto> = throw UnsupportedOperationException()
    override suspend fun adjustStock(projectId: String, materialId: String, req: StockAdjustRequest): Response<MaterialNetworkDto> = throw UnsupportedOperationException()
    override suspend fun getMaterialRequests(projectId: String): Response<List<MaterialRequestNetworkDto>> = Response.success(emptyList())
    override suspend fun createMaterialRequest(projectId: String, req: CreateMaterialRequestRequest): Response<MaterialRequestNetworkDto> = throw UnsupportedOperationException()
    override suspend fun approveMaterialRequest(projectId: String, requestId: String): Response<MaterialRequestNetworkDto> = throw UnsupportedOperationException()
    override suspend fun rejectMaterialRequest(projectId: String, requestId: String, req: RejectMaterialRequestRequest): Response<MaterialRequestNetworkDto> = throw UnsupportedOperationException()
    override suspend fun issueMaterialRequest(projectId: String, requestId: String, req: IssueMaterialRequestRequest): Response<MaterialRequestNetworkDto> = throw UnsupportedOperationException()
    override suspend fun getLowStockMaterials(projectId: String): Response<List<MaterialNetworkDto>> = Response.success(
        listOf(
            MaterialNetworkDto(
                id = "mat_steel_12",
                projectId = projectId,
                code = "MAT-STEEL-12",
                name = "12mm TMT Fe550D Rebar",
                category = "STEEL",
                unit = "MT",
                currentStock = BigDecimal("3.50"),
                minStockThreshold = BigDecimal("6.00"),
                unitCost = BigDecimal("66000.00"),
                isLowStock = true,
                createdAt = "2026-03-01T08:00:00Z"
            )
        )
    )
    override suspend fun getProjectStockLedger(projectId: String): Response<List<StockMovementNetworkDto>> = Response.success(emptyList())
    override suspend fun getStockMovementsForMaterial(projectId: String, materialId: String): Response<List<StockMovementNetworkDto>> = Response.success(emptyList())
    override suspend fun returnStock(projectId: String, materialId: String, req: ReturnStockRequest): Response<MaterialNetworkDto> = throw UnsupportedOperationException()

    // Phase 5: Procurement
    override suspend fun getVendors(organizationId: String): Response<List<VendorNetworkDto>> = Response.success(emptyList())
    override suspend fun createVendor(organizationId: String, req: CreateVendorRequest): Response<VendorNetworkDto> = throw UnsupportedOperationException()
    override suspend fun getPurchaseOrders(projectId: String): Response<List<PurchaseOrderNetworkDto>> = Response.success(emptyList())
    override suspend fun createPurchaseOrder(projectId: String, req: CreatePurchaseOrderRequest): Response<PurchaseOrderNetworkDto> = throw UnsupportedOperationException()
    override suspend fun recordGrn(projectId: String, req: CreateGrnRequest): Response<GoodsReceiptNoteNetworkDto> = throw UnsupportedOperationException()
    override suspend fun getGrnsForPo(projectId: String, poId: String): Response<List<GoodsReceiptNoteNetworkDto>> = Response.success(emptyList())

    // Phase 5: Finance
    override suspend fun getExpenses(projectId: String, category: String?): Response<List<ExpenseNetworkDto>> = Response.success(emptyList())
    override suspend fun createExpense(projectId: String, req: CreateExpenseRequest): Response<ExpenseNetworkDto> = throw UnsupportedOperationException()
    override suspend fun updateExpenseStatus(projectId: String, expenseId: String, req: UpdateExpenseStatusRequest): Response<ExpenseNetworkDto> = throw UnsupportedOperationException()
    override suspend fun getContractorBills(projectId: String): Response<List<ContractorBillNetworkDto>> = Response.success(emptyList())
    override suspend fun createContractorBill(projectId: String, req: CreateContractorBillRequest): Response<ContractorBillNetworkDto> = throw UnsupportedOperationException()
    override suspend fun updateContractorBillStatus(projectId: String, billId: String, req: UpdateBillStatusRequest): Response<ContractorBillNetworkDto> = throw UnsupportedOperationException()

    // Phase 5: Cost Dashboard
    override suspend fun getCostDashboard(projectId: String, period: String): Response<CostDashboardSummaryNetworkDto> = throw UnsupportedOperationException()

    // Phase 6: Documents
    override suspend fun getDocuments(projectId: String, type: String?): Response<List<DocumentNetworkDto>> = Response.success(emptyList())
    override suspend fun getDocument(documentId: String): Response<DocumentNetworkDto> = throw UnsupportedOperationException()
    override suspend fun createDocument(projectId: String, req: CreateDocumentRequest): Response<DocumentNetworkDto> = throw UnsupportedOperationException()
    override suspend fun addDocumentVersion(documentId: String, req: CreateDocumentVersionRequest): Response<DocumentVersionNetworkDto> = throw UnsupportedOperationException()
    override suspend fun getDocumentVersions(documentId: String): Response<List<DocumentVersionNetworkDto>> = Response.success(emptyList())

    // Phase 6: Notifications
    private val fakeNotifications = mutableListOf(
        NotificationNetworkDto(
            id = "notif_001",
            organizationId = "org_apex_infra",
            userId = "usr_pm_rahul",
            projectId = "prj_commercial_tower_b",
            title = "Contractor RA Bill Awaiting Approval",
            message = "Apex Earthmovers submitted RA Bill #01 (₹4,00,000)",
            type = "APPROVAL_REQUIRED",
            isRead = false,
            entityId = "bill_001",
            actionUrl = "/finance/bills",
            createdAt = "2026-03-05T10:00:00Z"
        ),
        NotificationNetworkDto(
            id = "notif_002",
            organizationId = "org_apex_infra",
            userId = "usr_pm_rahul",
            projectId = "prj_commercial_tower_b",
            title = "Low Stock Alert: 12mm TMT Rebar",
            message = "Current stock 3.50 MT below threshold",
            type = "STOCK_ALERT",
            isRead = false,
            entityId = "mat_steel_12",
            actionUrl = "/inventory",
            createdAt = "2026-03-06T08:30:00Z"
        )
    )

    override suspend fun getNotifications(): Response<List<NotificationNetworkDto>> = Response.success(fakeNotifications.toList())
    override suspend fun getUnreadCount(): Response<UnreadCountResponse> = Response.success(
        UnreadCountResponse(fakeNotifications.count { !it.isRead }.toLong())
    )
    override suspend fun markNotificationAsRead(notificationId: String): Response<NotificationNetworkDto> {
        val idx = fakeNotifications.indexOfFirst { it.id == notificationId }
        val updated = if (idx >= 0) {
            val item = fakeNotifications[idx].copy(isRead = true)
            fakeNotifications[idx] = item
            item
        } else {
            NotificationNetworkDto(
                id = notificationId,
                organizationId = "org_apex_infra",
                userId = "usr_pm_rahul",
                projectId = "prj_commercial_tower_b",
                title = "Notification",
                message = "Read",
                type = "APPROVAL_REQUIRED",
                isRead = true,
                entityId = null,
                actionUrl = null,
                createdAt = "2026-03-06T08:30:00Z"
            )
        }
        return Response.success(updated)
    }
    override suspend fun markAllNotificationsAsRead(): Response<Map<String, String>> {
        for (i in fakeNotifications.indices) {
            fakeNotifications[i] = fakeNotifications[i].copy(isRead = true)
        }
        return Response.success(mapOf("status" to "ok"))
    }

    // Phase 6: Audit Logs
    override suspend fun getAuditLogs(projectId: String, type: String?): Response<List<AuditLogNetworkDto>> = Response.success(
        listOf(
            AuditLogNetworkDto(
                id = "aud_001",
                organizationId = "org_apex_infra",
                projectId = projectId,
                userId = "usr_pm_rahul",
                userName = "Rahul Verma",
                action = "APPROVE",
                entityType = "EXPENSE",
                entityId = "exp_001",
                details = "Approved site rig expense ₹120,000",
                ipAddress = "192.168.1.101",
                createdAt = "2026-03-05T16:45:00Z"
            ),
            AuditLogNetworkDto(
                id = "aud_002",
                organizationId = "org_apex_infra",
                projectId = projectId,
                userId = "usr_storekeeper_06",
                userName = "Ramesh Yadav",
                action = "ISSUE_STOCK",
                entityType = "STOCK",
                entityId = "mat_cem_opc",
                details = "Issued 200 bags of cement",
                ipAddress = "192.168.1.112",
                createdAt = "2026-03-04T11:20:00Z"
            )
        )
    )
    override suspend fun getAuditSummary(projectId: String): Response<AuditSummaryNetworkDto> = Response.success(
        AuditSummaryNetworkDto(
            totalEvents = 5,
            approvalsCount = 2,
            stockIssuesCount = 1,
            documentsCount = 1,
            paymentsCount = 1
        )
    )
}

