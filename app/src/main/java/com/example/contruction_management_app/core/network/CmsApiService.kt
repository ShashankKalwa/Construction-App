package com.example.contruction_management_app.core.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*
import java.math.BigDecimal

data class ProjectNetworkDto(
    val id: String,
    val name: String,
    val code: String,
    val location: String,
    val budgetInr: BigDecimal,
    val status: String,
    val progressPct: Double,
    val organizationId: String
)

data class UserSummaryNetworkDto(
    val id: String,
    val fullName: String,
    val email: String,
    val avatarUrl: String?
)

data class ProjectMemberNetworkDto(
    val id: String,
    val user: UserSummaryNetworkDto,
    val assignedRole: String?,
    val joinedAt: String
)

data class ProjectDetailNetworkDto(
    val id: String,
    val name: String,
    val code: String,
    val location: String,
    val budgetInr: BigDecimal,
    val status: String,
    val progressPct: Double,
    val organizationId: String,
    val members: List<ProjectMemberNetworkDto>,
    val phasesCount: Int,
    val tasksCount: Int
)

data class TaskNetworkDto(
    val id: String,
    val phaseId: String,
    val phaseName: String?,
    val projectId: String,
    val title: String,
    val description: String?,
    val assignee: UserSummaryNetworkDto?,
    val priority: String,
    val status: String,
    val startDate: String?,
    val dueDate: String?,
    val progressPct: Double
)

data class PhaseNetworkDto(
    val id: String,
    val projectId: String,
    val name: String,
    val orderIndex: Int,
    val startDate: String?,
    val endDate: String?,
    val status: String,
    val progressPct: Double,
    val tasks: List<TaskNetworkDto> = emptyList()
)

data class CreateProjectRequest(
    val name: String,
    val code: String,
    val location: String,
    val budgetInr: BigDecimal
)

data class CreateTaskRequest(
    val title: String,
    val description: String? = null,
    val phaseId: String,
    val assigneeId: String? = null,
    val priority: String = "MEDIUM",
    val startDate: String? = null,
    val dueDate: String? = null
)

data class UpdateTaskProgressRequest(
    val progressPct: Double,
    val status: String? = null
)

data class DashboardSummaryNetworkDto(
    val activeProjectsCount: Int,
    val totalBudgetInr: BigDecimal,
    val overallProgressPct: Double,
    val pendingTasksCount: Int,
    val inProgressTasksCount: Int,
    val blockedTasksCount: Int,
    val completedTasksCount: Int,
    val roleMetrics: Map<String, String>
)

// =============================================================================
// Calculation Network DTOs
// =============================================================================

data class ConcreteNetworkRequest(
    val lengthM: Double,
    val widthM: Double,
    val depthM: Double,
    val grade: String? = "M20",
    val wastagePct: Double? = 5.0,
    val elementType: String? = "SLAB"
)

data class ConcreteNetworkResult(
    val wetVolumeM3: Double,
    val dryVolumeM3: Double,
    val grade: String,
    val mixRatio: String,
    val cementBags: Double,
    val cementKg: Double,
    val sandTonnes: Double,
    val sandCft: Double,
    val aggregateTonnes: Double,
    val aggregateCft: Double,
    val waterLiters: Double,
    val wastagePct: Double
)

data class SteelNetworkRequest(
    val diameterMm: Double,
    val lengthM: Double,
    val numberOfBars: Int,
    val wastagePct: Double? = 3.0,
    val elementType: String? = "COLUMN"
)

data class SteelNetworkResult(
    val diameterMm: Double,
    val lengthPerBarM: Double,
    val numberOfBars: Int,
    val totalLengthM: Double,
    val unitWeightKgPerM: Double,
    val rawWeightKg: Double,
    val totalWeightKg: Double,
    val totalWeightTonnes: Double,
    val bundleCount: Int,
    val wastagePct: Double
)

data class BrickworkNetworkRequest(
    val lengthM: Double,
    val heightM: Double,
    val thicknessM: Double,
    val openingsAreaSqM: Double? = 0.0,
    val mortarRatio: String? = "1:6",
    val wastagePct: Double? = 5.0
)

data class BrickworkNetworkResult(
    val grossWallVolumeM3: Double,
    val deductionsVolumeM3: Double,
    val netWallVolumeM3: Double,
    val brickCount: Double,
    val mortarDryVolumeM3: Double,
    val mortarRatio: String,
    val cementBags: Double,
    val cementKg: Double,
    val sandTonnes: Double,
    val sandCft: Double,
    val wastagePct: Double
)

data class ExcavationNetworkRequest(
    val lengthM: Double,
    val widthM: Double,
    val depthM: Double,
    val bulkingFactor: Double? = 1.25,
    val compactionFactor: Double? = 0.85,
    val truckCapacityM3: Double? = 10.0
)

data class ExcavationNetworkResult(
    val pitVolumeM3: Double,
    val looseVolumeM3: Double,
    val bulkingFactor: Double,
    val compactedBackfillM3: Double,
    val compactionFactor: Double,
    val truckCapacityM3: Double,
    val truckTripsRequired: Int
)

data class PlasterNetworkRequest(
    val lengthM: Double,
    val heightM: Double,
    val thicknessMm: Double,
    val openingsAreaSqM: Double? = 0.0,
    val mortarRatio: String? = "1:4",
    val wastagePct: Double? = 5.0
)

data class PlasterNetworkResult(
    val surfaceAreaSqM: Double,
    val wetVolumeM3: Double,
    val dryVolumeM3: Double,
    val mortarRatio: String,
    val cementBags: Double,
    val cementKg: Double,
    val sandTonnes: Double,
    val sandCft: Double,
    val wastagePct: Double
)

data class PaintNetworkRequest(
    val lengthM: Double,
    val widthM: Double,
    val heightM: Double,
    val openingsAreaSqM: Double? = 0.0,
    val includeCeiling: Boolean? = true,
    val numberOfCoats: Int? = 2,
    val includePrimer: Boolean? = true,
    val coverageRateSqMPerLiter: Double? = 10.0,
    val wastagePct: Double? = 5.0
)

data class PaintNetworkResult(
    val wallAreaSqM: Double,
    val ceilingAreaSqM: Double,
    val totalAreaSqM: Double,
    val numberOfCoats: Int,
    val primerLiters: Double,
    val paintLiters: Double,
    val cans20L: Int,
    val cans4L: Int,
    val cans1L: Int,
    val wastagePct: Double
)

data class UnitConvertNetworkRequest(
    val category: String,
    val value: Double,
    val fromUnit: String,
    val toUnit: String
)

data class UnitConvertNetworkResult(
    val category: String,
    val fromValue: Double,
    val fromUnit: String,
    val toValue: Double,
    val toUnit: String,
    val formulaUsed: String,
    val allConversions: Map<String, Double>
)

data class SaveCalculationRequest(
    val projectId: String,
    val calculatorType: String,
    val title: String,
    val elementType: String?,
    val inputParameters: String,
    val outputResults: String,
    val primaryQuantity: Double,
    val primaryUnit: String
)

data class CalculationNetworkDto(
    val id: String,
    val projectId: String,
    val calculatorType: String,
    val title: String,
    val elementType: String?,
    val inputParameters: String,
    val outputResults: String,
    val primaryQuantity: Double,
    val primaryUnit: String,
    val createdBy: String?,
    val createdAt: String
)

// =============================================================================
// Phase 4: BOQ, Measurement Book & Site Operations Network DTOs
// =============================================================================

data class BoqItemNetworkDto(
    val id: String,
    val boqId: String,
    val itemCode: String,
    val description: String,
    val category: String,
    val unit: String,
    val estimatedQuantity: BigDecimal,
    val unitRate: BigDecimal,
    val totalAmount: BigDecimal,
    val calculationId: String? = null,
    val orderIndex: Int = 0
)

data class BoqNetworkDto(
    val id: String,
    val projectId: String,
    val title: String,
    val version: Int,
    val status: String,
    val totalAmount: BigDecimal,
    val createdBy: String?,
    val approvedBy: String?,
    val items: List<BoqItemNetworkDto> = emptyList(),
    val createdAt: String,
    val updatedAt: String? = null
)

data class CreateBoqRequest(
    val title: String,
    val version: Int? = 1
)

data class CreateBoqItemRequest(
    val itemCode: String,
    val description: String,
    val category: String,
    val unit: String,
    val estimatedQuantity: BigDecimal,
    val unitRate: BigDecimal,
    val calculationId: String? = null,
    val orderIndex: Int? = 0
)

data class BoqDecisionRequest(
    val status: String,
    val rejectionReason: String? = null
)

data class MeasurementNetworkDto(
    val id: String,
    val projectId: String,
    val boqItemId: String?,
    val itemDescription: String,
    val numberOfUnits: BigDecimal,
    val lengthM: BigDecimal,
    val widthM: BigDecimal,
    val depthM: BigDecimal,
    val deductionM3: BigDecimal,
    val netQuantity: BigDecimal,
    val unit: String,
    val locationDetails: String?,
    val measuredBy: String?,
    val measuredAt: String
)

data class CreateMeasurementRequest(
    val boqItemId: String? = null,
    val itemDescription: String,
    val numberOfUnits: BigDecimal,
    val lengthM: BigDecimal,
    val widthM: BigDecimal,
    val depthM: BigDecimal,
    val deductionM3: BigDecimal = BigDecimal.ZERO,
    val unit: String = "M3",
    val locationDetails: String? = null
)

data class LaborAttendanceNetworkDto(
    val id: String? = null,
    val trade: String,
    val headcount: Int,
    val hoursWorked: BigDecimal? = null,
    val contractorName: String? = null
)

data class DailySiteReportNetworkDto(
    val id: String,
    val projectId: String,
    val reportDate: String,
    val weather: String,
    val workSummary: String,
    val plannedTomorrow: String?,
    val createdBy: String?,
    val laborAttendances: List<LaborAttendanceNetworkDto> = emptyList(),
    val totalHeadcount: Int,
    val createdAt: String
)

data class CreateDailySiteReportRequest(
    val reportDate: String,
    val weather: String,
    val workSummary: String,
    val plannedTomorrow: String? = null,
    val laborAttendances: List<LaborAttendanceNetworkDto> = emptyList()
)

data class SiteIssueNetworkDto(
    val id: String,
    val projectId: String,
    val reportId: String?,
    val title: String,
    val description: String?,
    val severity: String,
    val status: String,
    val assignedTo: String?,
    val createdBy: String?,
    val createdAt: String,
    val resolvedAt: String?
)

data class CreateSiteIssueRequest(
    val reportId: String? = null,
    val title: String,
    val description: String? = null,
    val severity: String = "MEDIUM",
    val assignedTo: String? = null
)

data class UpdateIssueStatusRequest(
    val status: String,
    val resolutionNotes: String? = null
)

// =============================================================================
// Phase 5: Inventory, Procurement, Finance & Cost Dashboard Network DTOs
// =============================================================================

data class MaterialNetworkDto(
    val id: String,
    val projectId: String,
    val code: String,
    val name: String,
    val category: String,
    val unit: String,
    val currentStock: BigDecimal,
    val minStockThreshold: BigDecimal,
    val unitCost: BigDecimal,
    val isLowStock: Boolean = false,
    val createdAt: String? = null
)

data class CreateMaterialRequest(
    val code: String,
    val name: String,
    val category: String,
    val unit: String,
    val initialStock: BigDecimal = BigDecimal.ZERO,
    val minStockThreshold: BigDecimal = BigDecimal.ZERO,
    val unitCost: BigDecimal = BigDecimal.ZERO
)

data class StockAdjustRequest(
    val movementType: String,
    val quantity: BigDecimal,
    val referenceId: String? = null,
    val notes: String? = null
)

data class MaterialRequestNetworkDto(
    val id: String,
    val projectId: String,
    val materialId: String,
    val materialName: String,
    val materialUnit: String,
    val quantity: BigDecimal,
    val requiredByDate: String?,
    val requestedBy: String?,
    val approvedBy: String?,
    val status: String,
    val phaseOrTaskDesc: String?,
    val createdAt: String? = null
)

data class CreateMaterialRequestRequest(
    val materialId: String,
    val quantity: BigDecimal,
    val requiredByDate: String? = null,
    val phaseOrTaskDesc: String? = null
)

data class VendorNetworkDto(
    val id: String,
    val organizationId: String,
    val name: String,
    val gstin: String?,
    val phone: String?,
    val email: String?,
    val category: String?,
    val paymentTerms: String?
)

data class CreateVendorRequest(
    val name: String,
    val gstin: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val category: String? = null,
    val paymentTerms: String? = null
)

data class PurchaseOrderItemNetworkDto(
    val id: String? = null,
    val purchaseOrderId: String? = null,
    val materialId: String,
    val materialName: String? = null,
    val unit: String? = null,
    val orderedQuantity: BigDecimal,
    val unitPrice: BigDecimal,
    val totalPrice: BigDecimal? = null,
    val receivedQuantity: BigDecimal = BigDecimal.ZERO
)

data class CreatePurchaseOrderItemRequest(
    val materialId: String,
    val orderedQuantity: BigDecimal,
    val unitPrice: BigDecimal
)

data class PurchaseOrderNetworkDto(
    val id: String,
    val poNumber: String,
    val projectId: String,
    val vendorId: String,
    val vendorName: String?,
    val orderDate: String,
    val deliveryDeadline: String?,
    val totalAmount: BigDecimal,
    val status: String,
    val createdBy: String?,
    val items: List<PurchaseOrderItemNetworkDto> = emptyList(),
    val createdAt: String? = null
)

data class CreatePurchaseOrderRequest(
    val poNumber: String? = null,
    val vendorId: String,
    val orderDate: String,
    val deliveryDeadline: String? = null,
    val items: List<CreatePurchaseOrderItemRequest>
)

data class GoodsReceiptNoteNetworkDto(
    val id: String,
    val purchaseOrderId: String,
    val poNumber: String?,
    val materialId: String,
    val materialName: String?,
    val unit: String?,
    val challanNumber: String,
    val receivedQuantity: BigDecimal,
    val receivedDate: String,
    val inspectedBy: String?,
    val qualityRemarks: String?,
    val createdAt: String? = null
)

data class CreateGrnRequest(
    val purchaseOrderId: String,
    val materialId: String,
    val challanNumber: String,
    val receivedQuantity: BigDecimal,
    val receivedDate: String,
    val qualityRemarks: String? = null
)

data class ExpenseNetworkDto(
    val id: String,
    val projectId: String,
    val category: String,
    val title: String,
    val amount: BigDecimal,
    val expenseDate: String,
    val receiptUrl: String?,
    val status: String,
    val submittedBy: String?,
    val verifiedBy: String?,
    val approvedBy: String?,
    val paidBy: String?,
    val createdAt: String? = null
)

data class CreateExpenseRequest(
    val category: String,
    val title: String,
    val amount: BigDecimal,
    val expenseDate: String,
    val receiptUrl: String? = null
)

data class UpdateExpenseStatusRequest(
    val status: String
)

data class ContractorBillNetworkDto(
    val id: String,
    val projectId: String,
    val contractorName: String,
    val billNumber: String,
    val billDate: String,
    val grossAmount: BigDecimal,
    val retentionPct: BigDecimal,
    val retentionAmount: BigDecimal,
    val tdsPct: BigDecimal,
    val tdsAmount: BigDecimal,
    val netPayable: BigDecimal,
    val status: String,
    val workDescription: String?,
    val certifiedBy: String?,
    val createdAt: String? = null
)

data class CreateContractorBillRequest(
    val contractorName: String,
    val billNumber: String? = null,
    val billDate: String,
    val grossAmount: BigDecimal,
    val retentionPct: BigDecimal = BigDecimal.valueOf(5.00),
    val tdsPct: BigDecimal = BigDecimal.valueOf(2.00),
    val workDescription: String? = null
)

data class UpdateBillStatusRequest(
    val status: String
)

data class CategoryCostNetworkDto(
    val category: String,
    val categoryName: String,
    val budgetAmount: BigDecimal,
    val actualAmount: BigDecimal,
    val percentage: BigDecimal
)

data class MaterialConsumptionNetworkDto(
    val materialId: String,
    val materialName: String,
    val unit: String,
    val quantityConsumed: BigDecimal,
    val totalCost: BigDecimal,
    val consumptionPercentage: BigDecimal
)

data class CostDashboardSummaryNetworkDto(
    val projectId: String,
    val projectName: String,
    val period: String,
    val totalBudget: BigDecimal,
    val actualCost: BigDecimal,
    val budgetUsedPct: BigDecimal,
    val committedCost: BigDecimal,
    val remainingBudget: BigDecimal,
    val projectedFinalCost: BigDecimal,
    val costBreakdown: List<CategoryCostNetworkDto> = emptyList(),
    val topMaterialConsumption: List<MaterialConsumptionNetworkDto> = emptyList()
)

// --- Phase 6: Documents & Versioning ---
data class DocumentVersionNetworkDto(
    val id: String,
    val documentId: String,
    val versionNumber: String,
    val fileName: String,
    val fileSizeBytes: Long,
    val mimeType: String,
    val fileUrl: String,
    val changeNotes: String?,
    val uploadedBy: String,
    val uploadedAt: String
)

data class DocumentNetworkDto(
    val id: String,
    val projectId: String,
    val organizationId: String,
    val title: String,
    val documentType: String,
    val category: String?,
    val latestVersion: String,
    val status: String,
    val createdBy: String,
    val createdAt: String,
    val updatedAt: String,
    val versions: List<DocumentVersionNetworkDto> = emptyList()
)

data class CreateDocumentRequest(
    val title: String,
    val documentType: String,
    val category: String? = null,
    val versionNumber: String = "v1.0",
    val fileName: String,
    val fileSizeBytes: Long = 0L,
    val mimeType: String = "application/pdf",
    val fileUrl: String,
    val changeNotes: String? = null
)

data class CreateDocumentVersionRequest(
    val versionNumber: String,
    val fileName: String,
    val fileSizeBytes: Long = 0L,
    val mimeType: String = "application/pdf",
    val fileUrl: String,
    val changeNotes: String? = null
)

// --- Phase 6: In-App Notifications ---
data class NotificationNetworkDto(
    val id: String,
    val organizationId: String,
    val userId: String,
    val projectId: String?,
    val title: String,
    val message: String,
    val type: String,
    val isRead: Boolean,
    val entityId: String?,
    val actionUrl: String?,
    val createdAt: String
)

data class UnreadCountResponse(
    val unreadCount: Long
)

// --- Phase 6: Audit Activity Logs ---
data class AuditLogNetworkDto(
    val id: String,
    val organizationId: String,
    val projectId: String?,
    val userId: String,
    val userName: String,
    val action: String,
    val entityType: String,
    val entityId: String,
    val details: String?,
    val ipAddress: String?,
    val createdAt: String
)

data class AuditSummaryNetworkDto(
    val totalEvents: Long,
    val approvalsCount: Long,
    val stockIssuesCount: Long,
    val documentsCount: Long,
    val paymentsCount: Long
)

interface CmsApiService {
    @GET("projects")
    suspend fun getProjects(): Response<List<ProjectNetworkDto>>

    @POST("projects")
    suspend fun createProject(@Body request: CreateProjectRequest): Response<ProjectNetworkDto>

    @GET("projects/{id}/detail")
    suspend fun getProjectDetail(@Path("id") id: String): Response<ProjectDetailNetworkDto>

    @GET("projects/{projectId}/phases")
    suspend fun getPhasesWithTasks(@Path("projectId") projectId: String): Response<List<PhaseNetworkDto>>

    @POST("projects/{projectId}/tasks")
    suspend fun createTask(
        @Path("projectId") projectId: String,
        @Body request: CreateTaskRequest
    ): Response<TaskNetworkDto>

    @PATCH("tasks/{taskId}/progress")
    suspend fun updateTaskProgress(
        @Path("taskId") taskId: String,
        @Body request: UpdateTaskProgressRequest
    ): Response<TaskNetworkDto>

    @GET("dashboard/summary")
    suspend fun getDashboardSummary(
        @Query("projectId") projectId: String? = null
    ): Response<DashboardSummaryNetworkDto>

    // --- Calculators ---
    @POST("calculators/concrete")
    suspend fun calculateConcrete(@Body req: ConcreteNetworkRequest): Response<ConcreteNetworkResult>

    @POST("calculators/steel")
    suspend fun calculateSteel(@Body req: SteelNetworkRequest): Response<SteelNetworkResult>

    @POST("calculators/brickwork")
    suspend fun calculateBrickwork(@Body req: BrickworkNetworkRequest): Response<BrickworkNetworkResult>

    @POST("calculators/excavation")
    suspend fun calculateExcavation(@Body req: ExcavationNetworkRequest): Response<ExcavationNetworkResult>

    @POST("calculators/plaster")
    suspend fun calculatePlaster(@Body req: PlasterNetworkRequest): Response<PlasterNetworkResult>

    @POST("calculators/paint")
    suspend fun calculatePaint(@Body req: PaintNetworkRequest): Response<PaintNetworkResult>

    @POST("calculators/unit-convert")
    suspend fun convertUnits(@Body req: UnitConvertNetworkRequest): Response<UnitConvertNetworkResult>

    // --- Calculation Persistence ---
    @POST("projects/{projectId}/calculations")
    suspend fun saveCalculation(
        @Path("projectId") projectId: String,
        @Body req: SaveCalculationRequest
    ): Response<CalculationNetworkDto>

    @GET("projects/{projectId}/calculations")
    suspend fun getProjectCalculations(
        @Path("projectId") projectId: String,
        @Query("type") type: String? = null
    ): Response<List<CalculationNetworkDto>>

    @GET("calculations/{id}")
    suspend fun getCalculationById(@Path("id") id: String): Response<CalculationNetworkDto>

    // --- Phase 4: BOQs ---
    @GET("projects/{projectId}/boqs")
    suspend fun getProjectBoqs(@Path("projectId") projectId: String): Response<List<BoqNetworkDto>>

    @POST("projects/{projectId}/boqs")
    suspend fun createBoq(
        @Path("projectId") projectId: String,
        @Body req: CreateBoqRequest
    ): Response<BoqNetworkDto>

    @GET("boqs/{id}")
    suspend fun getBoqById(@Path("id") id: String): Response<BoqNetworkDto>

    @POST("boqs/{id}/items")
    suspend fun addBoqItem(
        @Path("id") id: String,
        @Body req: CreateBoqItemRequest
    ): Response<BoqItemNetworkDto>

    @POST("boqs/{id}/submit")
    suspend fun submitBoq(@Path("id") id: String): Response<BoqNetworkDto>

    @POST("boqs/{id}/decision")
    suspend fun decideBoq(
        @Path("id") id: String,
        @Body req: BoqDecisionRequest
    ): Response<BoqNetworkDto>

    // --- Phase 4: Measurements ---
    @GET("projects/{projectId}/measurements")
    suspend fun getProjectMeasurements(
        @Path("projectId") projectId: String,
        @Query("boqItemId") boqItemId: String? = null
    ): Response<List<MeasurementNetworkDto>>

    @POST("projects/{projectId}/measurements")
    suspend fun createMeasurement(
        @Path("projectId") projectId: String,
        @Body req: CreateMeasurementRequest
    ): Response<MeasurementNetworkDto>

    @GET("measurements/{id}")
    suspend fun getMeasurementById(@Path("id") id: String): Response<MeasurementNetworkDto>

    // --- Phase 4: Site Operations (DSR & Issues) ---
    @GET("projects/{projectId}/daily-reports")
    suspend fun getDailyReports(@Path("projectId") projectId: String): Response<List<DailySiteReportNetworkDto>>

    @POST("projects/{projectId}/daily-reports")
    suspend fun createDailyReport(
        @Path("projectId") projectId: String,
        @Body req: CreateDailySiteReportRequest
    ): Response<DailySiteReportNetworkDto>

    @GET("daily-reports/{id}")
    suspend fun getDailyReportById(@Path("id") id: String): Response<DailySiteReportNetworkDto>

    @GET("projects/{projectId}/issues")
    suspend fun getProjectIssues(
        @Path("projectId") projectId: String,
        @Query("status") status: String? = null
    ): Response<List<SiteIssueNetworkDto>>

    @POST("projects/{projectId}/issues")
    suspend fun createIssue(
        @Path("projectId") projectId: String,
        @Body req: CreateSiteIssueRequest
    ): Response<SiteIssueNetworkDto>

    @PATCH("issues/{id}/status")
    suspend fun updateIssueStatus(
        @Path("id") id: String,
        @Body req: UpdateIssueStatusRequest
    ): Response<SiteIssueNetworkDto>

    // --- Phase 5: Inventory & Materials ---
    @GET("projects/{projectId}/materials")
    suspend fun getMaterials(@Path("projectId") projectId: String): Response<List<MaterialNetworkDto>>

    @POST("projects/{projectId}/materials")
    suspend fun createMaterial(
        @Path("projectId") projectId: String,
        @Body req: CreateMaterialRequest
    ): Response<MaterialNetworkDto>

    @POST("projects/{projectId}/materials/{materialId}/adjust")
    suspend fun adjustStock(
        @Path("projectId") projectId: String,
        @Path("materialId") materialId: String,
        @Body req: StockAdjustRequest
    ): Response<MaterialNetworkDto>

    @GET("projects/{projectId}/material-requests")
    suspend fun getMaterialRequests(@Path("projectId") projectId: String): Response<List<MaterialRequestNetworkDto>>

    @POST("projects/{projectId}/material-requests")
    suspend fun createMaterialRequest(
        @Path("projectId") projectId: String,
        @Body req: CreateMaterialRequestRequest
    ): Response<MaterialRequestNetworkDto>

    @PATCH("projects/{projectId}/material-requests/{requestId}/approve")
    suspend fun approveMaterialRequest(
        @Path("projectId") projectId: String,
        @Path("requestId") requestId: String
    ): Response<MaterialRequestNetworkDto>

    // --- Phase 5: Procurement & Vendors ---
    @GET("organizations/{organizationId}/vendors")
    suspend fun getVendors(@Path("organizationId") organizationId: String): Response<List<VendorNetworkDto>>

    @POST("organizations/{organizationId}/vendors")
    suspend fun createVendor(
        @Path("organizationId") organizationId: String,
        @Body req: CreateVendorRequest
    ): Response<VendorNetworkDto>

    @GET("projects/{projectId}/purchase-orders")
    suspend fun getPurchaseOrders(@Path("projectId") projectId: String): Response<List<PurchaseOrderNetworkDto>>

    @POST("projects/{projectId}/purchase-orders")
    suspend fun createPurchaseOrder(
        @Path("projectId") projectId: String,
        @Body req: CreatePurchaseOrderRequest
    ): Response<PurchaseOrderNetworkDto>

    @POST("projects/{projectId}/grn")
    suspend fun recordGrn(
        @Path("projectId") projectId: String,
        @Body req: CreateGrnRequest
    ): Response<GoodsReceiptNoteNetworkDto>

    @GET("projects/{projectId}/purchase-orders/{poId}/grn")
    suspend fun getGrnsForPo(
        @Path("projectId") projectId: String,
        @Path("poId") poId: String
    ): Response<List<GoodsReceiptNoteNetworkDto>>

    // --- Phase 5: Finance & Expenses ---
    @GET("projects/{projectId}/expenses")
    suspend fun getExpenses(
        @Path("projectId") projectId: String,
        @Query("category") category: String? = null
    ): Response<List<ExpenseNetworkDto>>

    @POST("projects/{projectId}/expenses")
    suspend fun createExpense(
        @Path("projectId") projectId: String,
        @Body req: CreateExpenseRequest
    ): Response<ExpenseNetworkDto>

    @PATCH("projects/{projectId}/expenses/{expenseId}/status")
    suspend fun updateExpenseStatus(
        @Path("projectId") projectId: String,
        @Path("expenseId") expenseId: String,
        @Body req: UpdateExpenseStatusRequest
    ): Response<ExpenseNetworkDto>

    @GET("projects/{projectId}/contractor-bills")
    suspend fun getContractorBills(@Path("projectId") projectId: String): Response<List<ContractorBillNetworkDto>>

    @POST("projects/{projectId}/contractor-bills")
    suspend fun createContractorBill(
        @Path("projectId") projectId: String,
        @Body req: CreateContractorBillRequest
    ): Response<ContractorBillNetworkDto>

    @PATCH("projects/{projectId}/contractor-bills/{billId}/status")
    suspend fun updateContractorBillStatus(
        @Path("projectId") projectId: String,
        @Path("billId") billId: String,
        @Body req: UpdateBillStatusRequest
    ): Response<ContractorBillNetworkDto>

    // --- Phase 5: Cost Dashboard ---
    @GET("projects/{projectId}/cost-dashboard")
    suspend fun getCostDashboard(
        @Path("projectId") projectId: String,
        @Query("period") period: String = "TOTAL"
    ): Response<CostDashboardSummaryNetworkDto>

    // --- Phase 6: Documents & Versioning ---
    @GET("projects/{projectId}/documents")
    suspend fun getDocuments(
        @Path("projectId") projectId: String,
        @Query("type") type: String? = null
    ): Response<List<DocumentNetworkDto>>

    @GET("documents/{documentId}")
    suspend fun getDocument(@Path("documentId") documentId: String): Response<DocumentNetworkDto>

    @POST("projects/{projectId}/documents")
    suspend fun createDocument(
        @Path("projectId") projectId: String,
        @Body req: CreateDocumentRequest
    ): Response<DocumentNetworkDto>

    @POST("documents/{documentId}/versions")
    suspend fun addDocumentVersion(
        @Path("documentId") documentId: String,
        @Body req: CreateDocumentVersionRequest
    ): Response<DocumentVersionNetworkDto>

    @GET("documents/{documentId}/versions")
    suspend fun getDocumentVersions(@Path("documentId") documentId: String): Response<List<DocumentVersionNetworkDto>>

    // --- Phase 6: In-App Notifications ---
    @GET("notifications")
    suspend fun getNotifications(): Response<List<NotificationNetworkDto>>

    @GET("notifications/unread-count")
    suspend fun getUnreadCount(): Response<UnreadCountResponse>

    @PATCH("notifications/{notificationId}/read")
    suspend fun markNotificationAsRead(@Path("notificationId") notificationId: String): Response<NotificationNetworkDto>

    @POST("notifications/mark-all-read")
    suspend fun markAllNotificationsAsRead(): Response<Map<String, String>>

    // --- Phase 6: Audit Activity Logs ---
    @GET("projects/{projectId}/audit-logs")
    suspend fun getAuditLogs(
        @Path("projectId") projectId: String,
        @Query("type") type: String? = null
    ): Response<List<AuditLogNetworkDto>>

    @GET("projects/{projectId}/audit-report/summary")
    suspend fun getAuditSummary(@Path("projectId") projectId: String): Response<AuditSummaryNetworkDto>
}

