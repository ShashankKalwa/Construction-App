package com.example.contruction_management_app.domain.repository

import com.example.contruction_management_app.core.network.*
import com.example.contruction_management_app.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class FinanceRepository(private val apiService: CmsApiService) {

    private val localExpenseCache = ConcurrentHashMap<String, MutableList<Expense>>()
    private val localBillCache = ConcurrentHashMap<String, MutableList<ContractorBill>>()

    init {
        seedInitialDemoFinance("prj_commercial_tower_b")
    }

    private fun seedInitialDemoFinance(projectId: String) {
        val expenses = mutableListOf(
            Expense(
                id = "exp_001",
                projectId = projectId,
                category = ExpenseCategory.EQUIPMENT,
                title = "Diesel fuel for JCB excavator & transit mixer",
                amount = BigDecimal("18500.00"),
                expenseDate = "2026-03-18",
                receiptUrl = null,
                status = ExpenseStatus.PAID,
                submittedBy = "Site Engineer",
                verifiedBy = "Project Manager",
                approvedBy = "Finance Head",
                paidBy = "Accounts Dept",
                createdAt = "2026-03-18T11:00:00Z"
            ),
            Expense(
                id = "exp_002",
                projectId = projectId,
                category = ExpenseCategory.LABOR,
                title = "Daily wage helper gang overtime for evening pour",
                amount = BigDecimal("8200.00"),
                expenseDate = "2026-03-19",
                receiptUrl = null,
                status = ExpenseStatus.APPROVED,
                submittedBy = "Site Engineer",
                verifiedBy = "Project Manager",
                approvedBy = "Finance Head",
                paidBy = null,
                createdAt = "2026-03-19T17:30:00Z"
            ),
            Expense(
                id = "exp_003",
                projectId = projectId,
                category = ExpenseCategory.UTILITIES,
                title = "Temporary power connection bill & water tanker supply",
                amount = BigDecimal("14350.00"),
                expenseDate = "2026-03-20",
                receiptUrl = null,
                status = ExpenseStatus.VERIFIED,
                submittedBy = "Site Admin",
                verifiedBy = "Project Manager",
                approvedBy = null,
                paidBy = null,
                createdAt = "2026-03-20T10:15:00Z"
            ),
            Expense(
                id = "exp_004",
                projectId = projectId,
                category = ExpenseCategory.MATERIALS,
                title = "Safety gear: 25 Helmets, hi-vis jackets & steel-toe gumboots",
                amount = BigDecimal("12500.00"),
                expenseDate = "2026-03-21",
                receiptUrl = null,
                status = ExpenseStatus.SUBMITTED,
                submittedBy = "Safety Officer",
                verifiedBy = null,
                approvedBy = null,
                paidBy = null,
                createdAt = "2026-03-21T09:00:00Z"
            )
        )
        localExpenseCache[projectId] = expenses

        val bills = mutableListOf(
            ContractorBill(
                id = "bill_001",
                projectId = projectId,
                contractorName = "Apex Formwork & Shuttering Contractors",
                billNumber = "AFW-RA-01",
                billDate = "2026-03-15",
                grossAmount = BigDecimal("350000.00"),
                retentionPct = BigDecimal("5.00"),
                retentionAmount = BigDecimal("17500.00"),
                tdsPct = BigDecimal("2.00"),
                tdsAmount = BigDecimal("7000.00"),
                netPayable = BigDecimal("325500.00"),
                status = BillPaymentStatus.APPROVED,
                workDescription = "Basement slab & retaining wall shuttering staging",
                certifiedBy = "Project Manager",
                createdAt = "2026-03-15T15:00:00Z"
            ),
            ContractorBill(
                id = "bill_002",
                projectId = projectId,
                contractorName = "Southern Steel Fabricators & Bar Benders",
                billNumber = "SRF-RA-02",
                billDate = "2026-03-20",
                grossAmount = BigDecimal("480000.00"),
                retentionPct = BigDecimal("5.00"),
                retentionAmount = BigDecimal("24000.00"),
                tdsPct = BigDecimal("2.00"),
                tdsAmount = BigDecimal("9600.00"),
                netPayable = BigDecimal("446400.00"),
                status = BillPaymentStatus.PENDING,
                workDescription = "Cutting, bending and placing 68 MT reinforcement rebar",
                certifiedBy = "Site Engineer",
                createdAt = "2026-03-20T12:00:00Z"
            )
        )
        localBillCache[projectId] = bills
    }

    suspend fun getExpenses(projectId: String, category: ExpenseCategory? = null): Result<List<Expense>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getExpenses(projectId, category?.name)
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    val mapped = response.body()!!.map { mapExpenseDto(it) }
                    localExpenseCache[projectId] = mapped.toMutableList()
                    Result.success(mapped)
                } else {
                    val all = localExpenseCache[projectId] ?: emptyList()
                    val filtered = if (category != null) all.filter { it.category == category } else all
                    Result.success(filtered)
                }
            } catch (_: Exception) {
                val all = localExpenseCache[projectId] ?: emptyList()
                val filtered = if (category != null) all.filter { it.category == category } else all
                Result.success(filtered)
            }
        }

    suspend fun createExpense(
        projectId: String,
        category: ExpenseCategory,
        title: String,
        amount: BigDecimal,
        expenseDate: String,
        receiptUrl: String? = null
    ): Result<Expense> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createExpense(
                projectId,
                CreateExpenseRequest(category.name, title, amount, expenseDate, receiptUrl)
            )
            if (response.isSuccessful && response.body() != null) {
                val exp = mapExpenseDto(response.body()!!)
                localExpenseCache.getOrPut(projectId) { mutableListOf() }.add(exp)
                Result.success(exp)
            } else {
                val local = Expense(
                    id = "exp_" + UUID.randomUUID().toString().take(8),
                    projectId = projectId,
                    category = category,
                    title = title,
                    amount = amount,
                    expenseDate = expenseDate,
                    receiptUrl = receiptUrl,
                    status = ExpenseStatus.SUBMITTED,
                    submittedBy = "Local User",
                    verifiedBy = null,
                    approvedBy = null,
                    paidBy = null,
                    createdAt = "Just now"
                )
                localExpenseCache.getOrPut(projectId) { mutableListOf() }.add(local)
                Result.success(local)
            }
        } catch (_: Exception) {
            val local = Expense(
                id = "exp_" + UUID.randomUUID().toString().take(8),
                projectId = projectId,
                category = category,
                title = title,
                amount = amount,
                expenseDate = expenseDate,
                receiptUrl = receiptUrl,
                status = ExpenseStatus.SUBMITTED,
                submittedBy = "Local User",
                verifiedBy = null,
                approvedBy = null,
                paidBy = null,
                createdAt = "Just now"
            )
            localExpenseCache.getOrPut(projectId) { mutableListOf() }.add(local)
            Result.success(local)
        }
    }

    suspend fun updateExpenseStatus(projectId: String, expenseId: String, status: ExpenseStatus): Result<Expense> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateExpenseStatus(projectId, expenseId, UpdateExpenseStatusRequest(status.name))
                if (response.isSuccessful && response.body() != null) {
                    val exp = mapExpenseDto(response.body()!!)
                    updateCachedExpense(projectId, exp)
                    Result.success(exp)
                } else {
                    updateLocalExpenseStatus(projectId, expenseId, status)
                }
            } catch (_: Exception) {
                updateLocalExpenseStatus(projectId, expenseId, status)
            }
        }

    private fun updateLocalExpenseStatus(projectId: String, expenseId: String, status: ExpenseStatus): Result<Expense> {
        val list = localExpenseCache[projectId] ?: return Result.failure(Exception("Project not found"))
        val idx = list.indexOfFirst { it.id == expenseId }
        if (idx < 0) return Result.failure(Exception("Expense not found"))
        val existing = list[idx]
        val updated = when (status) {
            ExpenseStatus.VERIFIED -> existing.copy(status = status, verifiedBy = "Project Manager")
            ExpenseStatus.APPROVED -> existing.copy(status = status, approvedBy = "Finance Head")
            ExpenseStatus.PAID -> existing.copy(status = status, paidBy = "Accounts Dept")
            else -> existing.copy(status = status)
        }
        list[idx] = updated
        return Result.success(updated)
    }

    private fun updateCachedExpense(projectId: String, exp: Expense) {
        val list = localExpenseCache[projectId] ?: mutableListOf()
        val idx = list.indexOfFirst { it.id == exp.id }
        if (idx >= 0) list[idx] = exp else list.add(exp)
        localExpenseCache[projectId] = list
    }

    suspend fun getContractorBills(projectId: String): Result<List<ContractorBill>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getContractorBills(projectId)
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                val mapped = response.body()!!.map { mapBillDto(it) }
                localBillCache[projectId] = mapped.toMutableList()
                Result.success(mapped)
            } else {
                Result.success(localBillCache[projectId] ?: emptyList())
            }
        } catch (_: Exception) {
            Result.success(localBillCache[projectId] ?: emptyList())
        }
    }

    suspend fun createContractorBill(
        projectId: String,
        contractorName: String,
        billNumber: String?,
        billDate: String,
        grossAmount: BigDecimal,
        retentionPct: BigDecimal = BigDecimal.valueOf(5.00),
        tdsPct: BigDecimal = BigDecimal.valueOf(2.00),
        workDescription: String?
    ): Result<ContractorBill> = withContext(Dispatchers.IO) {
        val retentionAmount = grossAmount.multiply(retentionPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
        val tdsAmount = grossAmount.multiply(tdsPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
        val netPayable = grossAmount.subtract(retentionAmount).subtract(tdsAmount)

        try {
            val response = apiService.createContractorBill(
                projectId,
                CreateContractorBillRequest(
                    contractorName = contractorName,
                    billNumber = billNumber,
                    billDate = billDate,
                    grossAmount = grossAmount,
                    retentionPct = retentionPct,
                    tdsPct = tdsPct,
                    workDescription = workDescription
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val b = mapBillDto(response.body()!!)
                localBillCache.getOrPut(projectId) { mutableListOf() }.add(b)
                Result.success(b)
            } else {
                val local = ContractorBill(
                    id = "bill_" + UUID.randomUUID().toString().take(8),
                    projectId = projectId,
                    contractorName = contractorName,
                    billNumber = billNumber ?: ("BILL-" + (System.currentTimeMillis() % 10000)),
                    billDate = billDate,
                    grossAmount = grossAmount,
                    retentionPct = retentionPct,
                    retentionAmount = retentionAmount,
                    tdsPct = tdsPct,
                    tdsAmount = tdsAmount,
                    netPayable = netPayable,
                    status = BillPaymentStatus.PENDING,
                    workDescription = workDescription,
                    certifiedBy = "Local User",
                    createdAt = "Just now"
                )
                localBillCache.getOrPut(projectId) { mutableListOf() }.add(local)
                Result.success(local)
            }
        } catch (_: Exception) {
            val local = ContractorBill(
                id = "bill_" + UUID.randomUUID().toString().take(8),
                projectId = projectId,
                contractorName = contractorName,
                billNumber = billNumber ?: ("BILL-" + (System.currentTimeMillis() % 10000)),
                billDate = billDate,
                grossAmount = grossAmount,
                retentionPct = retentionPct,
                retentionAmount = retentionAmount,
                tdsPct = tdsPct,
                tdsAmount = tdsAmount,
                netPayable = netPayable,
                status = BillPaymentStatus.PENDING,
                workDescription = workDescription,
                certifiedBy = "Local User",
                createdAt = "Just now"
            )
            localBillCache.getOrPut(projectId) { mutableListOf() }.add(local)
            Result.success(local)
        }
    }

    suspend fun updateContractorBillStatus(projectId: String, billId: String, status: BillPaymentStatus): Result<ContractorBill> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateContractorBillStatus(projectId, billId, UpdateBillStatusRequest(status.name))
                if (response.isSuccessful && response.body() != null) {
                    val b = mapBillDto(response.body()!!)
                    updateCachedBill(projectId, b)
                    Result.success(b)
                } else {
                    updateLocalBillStatus(projectId, billId, status)
                }
            } catch (_: Exception) {
                updateLocalBillStatus(projectId, billId, status)
            }
        }

    private fun updateLocalBillStatus(projectId: String, billId: String, status: BillPaymentStatus): Result<ContractorBill> {
        val list = localBillCache[projectId] ?: return Result.failure(Exception("Project not found"))
        val idx = list.indexOfFirst { it.id == billId }
        if (idx < 0) return Result.failure(Exception("Bill not found"))
        val updated = list[idx].copy(status = status)
        list[idx] = updated
        return Result.success(updated)
    }

    private fun updateCachedBill(projectId: String, bill: ContractorBill) {
        val list = localBillCache[projectId] ?: mutableListOf()
        val idx = list.indexOfFirst { it.id == bill.id }
        if (idx >= 0) list[idx] = bill else list.add(bill)
        localBillCache[projectId] = list
    }

    suspend fun getCostDashboard(projectId: String, period: DashboardPeriod = DashboardPeriod.TOTAL): Result<CostDashboardSummary> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCostDashboard(projectId, period.name)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(mapCostSummaryDto(response.body()!!))
                } else {
                    Result.success(buildDemoCostDashboard(projectId, period))
                }
            } catch (_: Exception) {
                Result.success(buildDemoCostDashboard(projectId, period))
            }
        }

    private fun buildDemoCostDashboard(projectId: String, period: DashboardPeriod): CostDashboardSummary {
        val totalBudget = BigDecimal("85000000.00") // 8.5 Cr
        val actualCost = BigDecimal("54200000.00")  // 5.42 Cr
        val budgetUsedPct = BigDecimal("63.8")
        val committedCost = BigDecimal("18200000.00") // 1.82 Cr
        val remainingBudget = BigDecimal("30800000.00")
        val projectedFinalCost = BigDecimal("86500000.00")

        val breakdown = listOf(
            CategoryCost("MATERIALS", "Materials (Steel, Cement, Sand)", BigDecimal("40000000.00"), BigDecimal("26800000.00"), BigDecimal("49.4")),
            CategoryCost("LABOR", "Direct Labor & Gang Overtime", BigDecimal("18000000.00"), BigDecimal("12100000.00"), BigDecimal("22.3")),
            CategoryCost("SUBCONTRACTOR", "Subcontractor & Specialist RA Bills", BigDecimal("15000000.00"), BigDecimal("9400000.00"), BigDecimal("17.3")),
            CategoryCost("EQUIPMENT", "Plant, Machinery & Cranes", BigDecimal("8000000.00"), BigDecimal("4100000.00"), BigDecimal("7.6")),
            CategoryCost("UTILITIES", "Temporary Power, Water & Permits", BigDecimal("4000000.00"), BigDecimal("1800000.00"), BigDecimal("3.4"))
        )

        val topMaterials = listOf(
            MaterialConsumption("mat_steel_16", "16mm TMT Fe550D Rebar", "MT", BigDecimal("185.0"), BigDecimal("12025000.00"), BigDecimal("44.8")),
            MaterialConsumption("mat_cem_opc", "OPC 53 Grade Cement", "Bags", BigDecimal("14200.0"), BigDecimal("5396000.00"), BigDecimal("20.1")),
            MaterialConsumption("mat_sand_m", "Manufactured M-Sand", "Ton", BigDecimal("2800.0"), BigDecimal("3500000.00"), BigDecimal("13.1")),
            MaterialConsumption("mat_agg_20", "20mm Coarse Aggregate", "Ton", BigDecimal("2400.0"), BigDecimal("2640000.00"), BigDecimal("9.9")),
            MaterialConsumption("mat_brk_red", "Class 1 Red Clay Bricks", "Pcs", BigDecimal("180000.0"), BigDecimal("1710000.00"), BigDecimal("6.4"))
        )

        return CostDashboardSummary(
            projectId = projectId,
            projectName = "Commercial Tower B",
            period = period,
            totalBudget = totalBudget,
            actualCost = actualCost,
            budgetUsedPct = budgetUsedPct,
            committedCost = committedCost,
            remainingBudget = remainingBudget,
            projectedFinalCost = projectedFinalCost,
            costBreakdown = breakdown,
            topMaterialConsumption = topMaterials
        )
    }

    private fun mapExpenseDto(dto: ExpenseNetworkDto): Expense {
        val cat = try {
            ExpenseCategory.valueOf(dto.category.uppercase())
        } catch (_: Exception) {
            ExpenseCategory.MATERIALS
        }
        val st = try {
            ExpenseStatus.valueOf(dto.status.uppercase())
        } catch (_: Exception) {
            ExpenseStatus.SUBMITTED
        }
        return Expense(
            id = dto.id,
            projectId = dto.projectId,
            category = cat,
            title = dto.title,
            amount = dto.amount,
            expenseDate = dto.expenseDate,
            receiptUrl = dto.receiptUrl,
            status = st,
            submittedBy = dto.submittedBy,
            verifiedBy = dto.verifiedBy,
            approvedBy = dto.approvedBy,
            paidBy = dto.paidBy,
            createdAt = dto.createdAt
        )
    }

    private fun mapBillDto(dto: ContractorBillNetworkDto): ContractorBill {
        val st = try {
            BillPaymentStatus.valueOf(dto.status.uppercase())
        } catch (_: Exception) {
            BillPaymentStatus.PENDING
        }
        return ContractorBill(
            id = dto.id,
            projectId = dto.projectId,
            contractorName = dto.contractorName,
            billNumber = dto.billNumber,
            billDate = dto.billDate,
            grossAmount = dto.grossAmount,
            retentionPct = dto.retentionPct,
            retentionAmount = dto.retentionAmount,
            tdsPct = dto.tdsPct,
            tdsAmount = dto.tdsAmount,
            netPayable = dto.netPayable,
            status = st,
            workDescription = dto.workDescription,
            certifiedBy = dto.certifiedBy,
            createdAt = dto.createdAt
        )
    }

    private fun mapCostSummaryDto(dto: CostDashboardSummaryNetworkDto): CostDashboardSummary {
        val p = try {
            DashboardPeriod.valueOf(dto.period.uppercase())
        } catch (_: Exception) {
            DashboardPeriod.TOTAL
        }
        return CostDashboardSummary(
            projectId = dto.projectId,
            projectName = dto.projectName,
            period = p,
            totalBudget = dto.totalBudget,
            actualCost = dto.actualCost,
            budgetUsedPct = dto.budgetUsedPct,
            committedCost = dto.committedCost,
            remainingBudget = dto.remainingBudget,
            projectedFinalCost = dto.projectedFinalCost,
            costBreakdown = dto.costBreakdown.map {
                CategoryCost(it.category, it.categoryName, it.budgetAmount, it.actualAmount, it.percentage)
            },
            topMaterialConsumption = dto.topMaterialConsumption.map {
                MaterialConsumption(it.materialId, it.materialName, it.unit, it.quantityConsumed, it.totalCost, it.consumptionPercentage)
            }
        )
    }
}
