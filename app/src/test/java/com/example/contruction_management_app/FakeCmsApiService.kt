package com.example.contruction_management_app

import com.example.contruction_management_app.core.network.*
import retrofit2.Response

open class FakeCmsApiService : CmsApiService {
    override suspend fun getProjects(): Response<List<ProjectNetworkDto>> = Response.success(emptyList())
    override suspend fun createProject(request: CreateProjectRequest): Response<ProjectNetworkDto> = throw UnsupportedOperationException()
    override suspend fun getProjectDetail(id: String): Response<ProjectDetailNetworkDto> = throw UnsupportedOperationException()
    override suspend fun getPhasesWithTasks(projectId: String): Response<List<PhaseNetworkDto>> = throw UnsupportedOperationException()
    override suspend fun createTask(projectId: String, request: CreateTaskRequest): Response<TaskNetworkDto> = throw UnsupportedOperationException()
    override suspend fun updateTaskProgress(taskId: String, request: UpdateTaskProgressRequest): Response<TaskNetworkDto> = throw UnsupportedOperationException()
    override suspend fun getDashboardSummary(projectId: String?): Response<DashboardSummaryNetworkDto> = throw UnsupportedOperationException()
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
    override suspend fun getDailyReportById(id: String): Response<DailySiteReportNetworkDto> = throw UnsupportedOperationException()
    override suspend fun getProjectIssues(projectId: String, status: String?): Response<List<SiteIssueNetworkDto>> = Response.success(emptyList())
    override suspend fun createIssue(projectId: String, req: CreateSiteIssueRequest): Response<SiteIssueNetworkDto> = throw UnsupportedOperationException()
    override suspend fun updateIssueStatus(id: String, req: UpdateIssueStatusRequest): Response<SiteIssueNetworkDto> = throw UnsupportedOperationException()

    // Phase 5: Materials
    override suspend fun getMaterials(projectId: String): Response<List<MaterialNetworkDto>> = Response.success(emptyList())
    override suspend fun createMaterial(projectId: String, req: CreateMaterialRequest): Response<MaterialNetworkDto> = throw UnsupportedOperationException()
    override suspend fun adjustStock(projectId: String, materialId: String, req: StockAdjustRequest): Response<MaterialNetworkDto> = throw UnsupportedOperationException()
    override suspend fun getMaterialRequests(projectId: String): Response<List<MaterialRequestNetworkDto>> = Response.success(emptyList())
    override suspend fun createMaterialRequest(projectId: String, req: CreateMaterialRequestRequest): Response<MaterialRequestNetworkDto> = throw UnsupportedOperationException()
    override suspend fun approveMaterialRequest(projectId: String, requestId: String): Response<MaterialRequestNetworkDto> = throw UnsupportedOperationException()

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
    override suspend fun getNotifications(): Response<List<NotificationNetworkDto>> = Response.success(emptyList())
    override suspend fun getUnreadCount(): Response<UnreadCountResponse> = Response.success(UnreadCountResponse(0))
    override suspend fun markNotificationAsRead(notificationId: String): Response<NotificationNetworkDto> = throw UnsupportedOperationException()
    override suspend fun markAllNotificationsAsRead(): Response<Map<String, String>> = Response.success(emptyMap())

    // Phase 6: Audit Logs
    override suspend fun getAuditLogs(projectId: String, type: String?): Response<List<AuditLogNetworkDto>> = Response.success(emptyList())
    override suspend fun getAuditSummary(projectId: String): Response<AuditSummaryNetworkDto> = throw UnsupportedOperationException()
}

