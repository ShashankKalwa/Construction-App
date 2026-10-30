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
    private val localPoCache = ConcurrentHashMap<String, MutableList<PurchaseOrder>>()
    private val localGrnCache = ConcurrentHashMap<String, MutableList<GoodsReceiptNote>>()
    private val localVendorCache = ConcurrentHashMap<String, MutableList<Vendor>>()

    init {
        seedInitialDemoFinance("prj_commercial_tower_b")
    }

    private fun seedInitialDemoFinance(projectId: String) {
        val expenses = mutableListOf(
            Expense(
                id = "exp_001",
                projectId = projectId,
                category = ExpenseCategory.EQUIPMENT,
                title = "Hydraulic Piling Rig 5-day rental for basement",
                amount = BigDecimal("120000.00"),
                expenseDate = "2026-03-02",
                receiptUrl = null,
                status = ExpenseStatus.PAID,
                submittedBy = "Amit Patel (Supervisor)",
                verifiedBy = "Priya Sharma (Engineer)",
                approvedBy = "Rahul Varma (PM)",
                paidBy = "Accounts Dept",
                paymentReference = "TXN-PAY-001",
                createdAt = "2026-03-02T11:00:00Z"
            ),
            Expense(
                id = "exp_002",
                projectId = projectId,
                category = ExpenseCategory.UTILITIES,
                title = "Diesel fuel 800 Litres for generators and pumps",
                amount = BigDecimal("72000.00"),
                expenseDate = "2026-03-05",
                receiptUrl = null,
                status = ExpenseStatus.APPROVED,
                submittedBy = "Amit Patel (Supervisor)",
                verifiedBy = "Priya Sharma (Engineer)",
                approvedBy = "Rahul Varma (PM)",
                paidBy = null,
                createdAt = "2026-03-05T17:30:00Z"
            ),
            Expense(
                id = "exp_003",
                projectId = projectId,
                category = ExpenseCategory.SAFETY,
                title = "Safety harness, helmets and perimeter net supply",
                amount = BigDecimal("35000.00"),
                expenseDate = "2026-03-12",
                receiptUrl = null,
                status = ExpenseStatus.VERIFIED,
                submittedBy = "Amit Patel (Supervisor)",
                verifiedBy = "Priya Sharma (Engineer)",
                approvedBy = null,
                paidBy = null,
                createdAt = "2026-03-12T10:15:00Z"
            ),
            Expense(
                id = "exp_004",
                projectId = projectId,
                category = ExpenseCategory.PETTY_CASH,
                title = "Site office survey station laser calibration",
                amount = BigDecimal("12500.00"),
                expenseDate = "2026-03-15",
                receiptUrl = null,
                status = ExpenseStatus.SUBMITTED,
                submittedBy = "Amit Patel (Supervisor)",
                verifiedBy = null,
                approvedBy = null,
                paidBy = null,
                createdAt = "2026-03-15T09:00:00Z"
            )
        )
        localExpenseCache[projectId] = expenses

        val bills = mutableListOf(
            ContractorBill(
                id = "bill_001",
                projectId = projectId,
                contractorName = "Apex Earthmovers & Shoring Co.",
                billNumber = "RA-BILL-01",
                billDate = "2026-03-05",
                grossAmount = BigDecimal("400000.00"),
                retentionPct = BigDecimal("5.00"),
                retentionAmount = BigDecimal("20000.00"),
                tdsPct = BigDecimal("2.00"),
                tdsAmount = BigDecimal("8000.00"),
                netPayable = BigDecimal("372000.00"),
                status = BillPaymentStatus.APPROVED,
                workDescription = "Complete excavation and shoring for Tower B Basement",
                certifiedBy = "Rahul Varma (PM)",
                createdAt = "2026-03-05T15:00:00Z"
            ),
            ContractorBill(
                id = "bill_002",
                projectId = projectId,
                contractorName = "Southern Structural Fabrication",
                billNumber = "RA-BILL-02",
                billDate = "2026-03-18",
                grossAmount = BigDecimal("650000.00"),
                retentionPct = BigDecimal("5.00"),
                retentionAmount = BigDecimal("32500.00"),
                tdsPct = BigDecimal("2.00"),
                tdsAmount = BigDecimal("13000.00"),
                netPayable = BigDecimal("604500.00"),
                status = BillPaymentStatus.PENDING,
                workDescription = "Footing reinforcement cutting, bending and fabrication",
                certifiedBy = "Rahul Varma (PM)",
                createdAt = "2026-03-18T12:00:00Z"
            )
        )
        localBillCache[projectId] = bills

        val vendors = mutableListOf(
            Vendor(
                id = "vnd_apex_cement",
                organizationId = "org_apex_1",
                name = "UltraTech Cement Distribution Hub",
                gstin = "29AABCU1234F1Z1",
                phone = "+91 98765 43210",
                email = "sales@ultratech-hub.com",
                category = "CEMENT_SUPPLIER",
                paymentTerms = "Net 30 Days"
            ),
            Vendor(
                id = "vnd_apex_steel",
                organizationId = "org_apex_1",
                name = "Reliable Steels & TMT Depot",
                gstin = "29AAACT9876E1Z5",
                phone = "+91 98450 11223",
                email = "orders@reliablesteels.com",
                category = "STEEL_TRADER",
                paymentTerms = "Net 15 Days"
            ),
            Vendor(
                id = "vnd_apex_quarry",
                organizationId = "org_apex_1",
                name = "Apex Blue Metal & Sand Quarry",
                gstin = "29AABBA5566G1Z9",
                phone = "+91 99001 22334",
                email = "quarry@apexaggregates.in",
                category = "AGGREGATE_QUARRY",
                paymentTerms = "Advance 50%"
            )
        )
        localVendorCache["org_apex_1"] = vendors

        val poItems1 = listOf(
            PurchaseOrderItem(
                id = "poi_001",
                purchaseOrderId = "po_001",
                materialId = "mat_cem_opc",
                materialName = "OPC 53 Grade Cement",
                unit = "Bags",
                orderedQuantity = BigDecimal("500.00"),
                unitPrice = BigDecimal("380.00"),
                totalPrice = BigDecimal("190000.00"),
                receivedQuantity = BigDecimal("500.00")
            )
        )
        val poItems2 = listOf(
            PurchaseOrderItem(
                id = "poi_002",
                purchaseOrderId = "po_002",
                materialId = "mat_steel_16",
                materialName = "16mm TMT Fe550D Rebar",
                unit = "MT",
                orderedQuantity = BigDecimal("10.00"),
                unitPrice = BigDecimal("65000.00"),
                totalPrice = BigDecimal("650000.00"),
                receivedQuantity = BigDecimal.ZERO
            )
        )

        val pos = mutableListOf(
            PurchaseOrder(
                id = "po_001",
                poNumber = "PO-2026-0101",
                projectId = projectId,
                vendorId = "vnd_apex_cement",
                vendorName = "UltraTech Cement Distribution Hub",
                orderDate = "2026-03-01",
                deliveryDeadline = "2026-03-05",
                totalAmount = BigDecimal("190000.00"),
                status = PurchaseOrderStatus.COMPLETED,
                createdBy = "Rahul Varma (PM)",
                items = poItems1,
                createdAt = "2026-03-01T08:00:00Z"
            ),
            PurchaseOrder(
                id = "po_002",
                poNumber = "PO-2026-0102",
                projectId = projectId,
                vendorId = "vnd_apex_steel",
                vendorName = "Reliable Steels & TMT Depot",
                orderDate = "2026-03-10",
                deliveryDeadline = "2026-03-25",
                totalAmount = BigDecimal("650000.00"),
                status = PurchaseOrderStatus.ISSUED,
                createdBy = "Rahul Varma (PM)",
                items = poItems2,
                createdAt = "2026-03-10T10:00:00Z"
            )
        )
        localPoCache[projectId] = pos

        val grns = mutableListOf(
            GoodsReceiptNote(
                id = "grn_001",
                purchaseOrderId = "po_001",
                poNumber = "PO-2026-0101",
                materialId = "mat_cem_opc",
                materialName = "OPC 53 Grade Cement",
                unit = "Bags",
                challanNumber = "CH-2026-881",
                receivedQuantity = BigDecimal("500.00"),
                receivedDate = "2026-03-04",
                inspectedBy = "Rahul Varma (PM)",
                qualityRemarks = "OPC 53 Bags verified intact, zero moisture damage",
                createdAt = "2026-03-04T11:00:00Z"
            )
        )
        localGrnCache["po_001"] = grns
    }

    // -------------------------------------------------------------------------
    // Expenses
    // -------------------------------------------------------------------------

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
        receiptUrl: String? = null,
        idempotencyKey: String? = null
    ): Result<Expense> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createExpense(
                projectId,
                CreateExpenseRequest(category.name, title, amount, expenseDate, receiptUrl, idempotencyKey)
            )
            if (response.isSuccessful && response.body() != null) {
                val exp = mapExpenseDto(response.body()!!)
                localExpenseCache.getOrPut(projectId) { mutableListOf() }.add(0, exp)
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
                    submittedBy = "Site Supervisor",
                    verifiedBy = null,
                    approvedBy = null,
                    paidBy = null,
                    idempotencyKey = idempotencyKey,
                    createdAt = "Just now"
                )
                localExpenseCache.getOrPut(projectId) { mutableListOf() }.add(0, local)
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
                submittedBy = "Site Supervisor",
                verifiedBy = null,
                approvedBy = null,
                paidBy = null,
                idempotencyKey = idempotencyKey,
                createdAt = "Just now"
            )
            localExpenseCache.getOrPut(projectId) { mutableListOf() }.add(0, local)
            Result.success(local)
        }
    }

    suspend fun updateExpenseStatus(
        projectId: String,
        expenseId: String,
        status: ExpenseStatus,
        rejectionReason: String? = null,
        paymentReference: String? = null
    ): Result<Expense> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateExpenseStatus(
                projectId,
                expenseId,
                UpdateExpenseStatusRequest(status.name, rejectionReason, paymentReference)
            )
            if (response.isSuccessful && response.body() != null) {
                val exp = mapExpenseDto(response.body()!!)
                updateCachedExpense(projectId, exp)
                Result.success(exp)
            } else {
                updateLocalExpenseStatus(projectId, expenseId, status, rejectionReason, paymentReference)
            }
        } catch (_: Exception) {
            updateLocalExpenseStatus(projectId, expenseId, status, rejectionReason, paymentReference)
        }
    }

    private fun updateLocalExpenseStatus(
        projectId: String,
        expenseId: String,
        status: ExpenseStatus,
        rejectionReason: String?,
        paymentReference: String?
    ): Result<Expense> {
        val list = localExpenseCache[projectId] ?: return Result.failure(Exception("Project not found"))
        val idx = list.indexOfFirst { it.id == expenseId }
        if (idx < 0) return Result.failure(Exception("Expense not found"))
        val existing = list[idx]
        val updated = existing.copy(
            status = status,
            rejectionReason = rejectionReason ?: existing.rejectionReason,
            paymentReference = paymentReference ?: existing.paymentReference,
            verifiedBy = if (status == ExpenseStatus.VERIFIED) "Civil Engineer" else existing.verifiedBy,
            approvedBy = if (status == ExpenseStatus.APPROVED) "Project Manager" else existing.approvedBy,
            paidBy = if (status == ExpenseStatus.PAID) "Accounts Dept" else existing.paidBy
        )
        list[idx] = updated
        return Result.success(updated)
    }

    private fun updateCachedExpense(projectId: String, expense: Expense) {
        val list = localExpenseCache[projectId] ?: mutableListOf()
        val idx = list.indexOfFirst { it.id == expense.id }
        if (idx >= 0) list[idx] = expense else list.add(0, expense)
        localExpenseCache[projectId] = list
    }

    // -------------------------------------------------------------------------
    // Contractor Bills
    // -------------------------------------------------------------------------

    suspend fun getContractorBills(projectId: String): Result<List<ContractorBill>> =
        withContext(Dispatchers.IO) {
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
        workDescription: String? = null,
        idempotencyKey: String? = null
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
                    workDescription = workDescription,
                    idempotencyKey = idempotencyKey
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val b = mapBillDto(response.body()!!)
                localBillCache.getOrPut(projectId) { mutableListOf() }.add(0, b)
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
                    certifiedBy = "Project Manager",
                    idempotencyKey = idempotencyKey,
                    createdAt = "Just now"
                )
                localBillCache.getOrPut(projectId) { mutableListOf() }.add(0, local)
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
                certifiedBy = "Project Manager",
                idempotencyKey = idempotencyKey,
                createdAt = "Just now"
            )
            localBillCache.getOrPut(projectId) { mutableListOf() }.add(0, local)
            Result.success(local)
        }
    }

    suspend fun updateContractorBillStatus(
        projectId: String,
        billId: String,
        status: BillPaymentStatus,
        paymentReference: String? = null
    ): Result<ContractorBill> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateContractorBillStatus(projectId, billId, UpdateBillStatusRequest(status.name, paymentReference))
            if (response.isSuccessful && response.body() != null) {
                val b = mapBillDto(response.body()!!)
                updateCachedBill(projectId, b)
                Result.success(b)
            } else {
                updateLocalBillStatus(projectId, billId, status, paymentReference)
            }
        } catch (_: Exception) {
            updateLocalBillStatus(projectId, billId, status, paymentReference)
        }
    }

    private fun updateLocalBillStatus(projectId: String, billId: String, status: BillPaymentStatus, paymentReference: String?): Result<ContractorBill> {
        val list = localBillCache[projectId] ?: return Result.failure(Exception("Project not found"))
        val idx = list.indexOfFirst { it.id == billId }
        if (idx < 0) return Result.failure(Exception("Bill not found"))
        val updated = list[idx].copy(
            status = status,
            paymentReference = paymentReference ?: list[idx].paymentReference
        )
        list[idx] = updated
        return Result.success(updated)
    }

    private fun updateCachedBill(projectId: String, bill: ContractorBill) {
        val list = localBillCache[projectId] ?: mutableListOf()
        val idx = list.indexOfFirst { it.id == bill.id }
        if (idx >= 0) list[idx] = bill else list.add(0, bill)
        localBillCache[projectId] = list
    }

    // -------------------------------------------------------------------------
    // Vendors & Procurement
    // -------------------------------------------------------------------------

    suspend fun getVendors(organizationId: String): Result<List<Vendor>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getVendors(organizationId)
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                val mapped = response.body()!!.map { mapVendorDto(it) }
                localVendorCache[organizationId] = mapped.toMutableList()
                Result.success(mapped)
            } else {
                Result.success(localVendorCache[organizationId] ?: emptyList())
            }
        } catch (_: Exception) {
            Result.success(localVendorCache[organizationId] ?: emptyList())
        }
    }

    suspend fun createVendor(
        organizationId: String,
        name: String,
        gstin: String?,
        phone: String?,
        email: String?,
        category: String?,
        paymentTerms: String?
    ): Result<Vendor> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createVendor(
                organizationId,
                CreateVendorRequest(name, gstin, phone, email, category, paymentTerms)
            )
            if (response.isSuccessful && response.body() != null) {
                val v = mapVendorDto(response.body()!!)
                localVendorCache.getOrPut(organizationId) { mutableListOf() }.add(v)
                Result.success(v)
            } else {
                val local = Vendor(
                    id = "vnd_" + UUID.randomUUID().toString().take(8),
                    organizationId = organizationId,
                    name = name,
                    gstin = gstin,
                    phone = phone,
                    email = email,
                    category = category,
                    paymentTerms = paymentTerms
                )
                localVendorCache.getOrPut(organizationId) { mutableListOf() }.add(local)
                Result.success(local)
            }
        } catch (_: Exception) {
            val local = Vendor(
                id = "vnd_" + UUID.randomUUID().toString().take(8),
                organizationId = organizationId,
                name = name,
                gstin = gstin,
                phone = phone,
                email = email,
                category = category,
                paymentTerms = paymentTerms
            )
            localVendorCache.getOrPut(organizationId) { mutableListOf() }.add(local)
            Result.success(local)
        }
    }

    suspend fun createVendor(
        organizationId: String,
        name: String,
        category: VendorCategory,
        contactPerson: String? = null,
        phone: String? = null,
        email: String? = null,
        gstin: String? = null,
        paymentTerms: String? = null
    ): Result<Vendor> = createVendor(
        organizationId = organizationId,
        name = name,
        gstin = gstin,
        phone = phone,
        email = email,
        category = category.name,
        paymentTerms = paymentTerms
    )

    suspend fun getPurchaseOrders(projectId: String): Result<List<PurchaseOrder>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPurchaseOrders(projectId)
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                val mapped = response.body()!!.map { mapPoDto(it) }
                localPoCache[projectId] = mapped.toMutableList()
                Result.success(mapped)
            } else {
                Result.success(localPoCache[projectId] ?: emptyList())
            }
        } catch (_: Exception) {
            Result.success(localPoCache[projectId] ?: emptyList())
        }
    }

    suspend fun createPurchaseOrder(
        projectId: String,
        poNumber: String?,
        vendorId: String,
        orderDate: String,
        deliveryDeadline: String?,
        items: List<CreatePurchaseOrderItemRequest>,
        idempotencyKey: String? = null
    ): Result<PurchaseOrder> = withContext(Dispatchers.IO) {
        val totalAmount = items.map { it.orderedQuantity.multiply(it.unitPrice) }.reduceOrNull(BigDecimal::add) ?: BigDecimal.ZERO
        val vendorName = localVendorCache.values.flatten().find { it.id == vendorId }?.name ?: "Vendor"

        try {
            val response = apiService.createPurchaseOrder(
                projectId,
                CreatePurchaseOrderRequest(poNumber, vendorId, orderDate, deliveryDeadline, idempotencyKey, items)
            )
            if (response.isSuccessful && response.body() != null) {
                val po = mapPoDto(response.body()!!)
                localPoCache.getOrPut(projectId) { mutableListOf() }.add(0, po)
                Result.success(po)
            } else {
                val generatedPoNumber = poNumber ?: ("PO-" + (System.currentTimeMillis() % 100000))
                val localItems = items.mapIndexed { idx, it ->
                    PurchaseOrderItem(
                        id = "poi_" + UUID.randomUUID().toString().take(8),
                        purchaseOrderId = generatedPoNumber,
                        materialId = it.materialId,
                        materialName = "Material item ${idx + 1}",
                        unit = "Units",
                        orderedQuantity = it.orderedQuantity,
                        unitPrice = it.unitPrice,
                        totalPrice = it.orderedQuantity.multiply(it.unitPrice).setScale(2, RoundingMode.HALF_UP),
                        receivedQuantity = BigDecimal.ZERO
                    )
                }
                val localPo = PurchaseOrder(
                    id = "po_" + UUID.randomUUID().toString().take(8),
                    poNumber = generatedPoNumber,
                    projectId = projectId,
                    vendorId = vendorId,
                    vendorName = vendorName,
                    orderDate = orderDate,
                    deliveryDeadline = deliveryDeadline,
                    totalAmount = totalAmount,
                    status = PurchaseOrderStatus.ISSUED,
                    createdBy = "Project Manager",
                    idempotencyKey = idempotencyKey,
                    items = localItems,
                    createdAt = "Just now"
                )
                localPoCache.getOrPut(projectId) { mutableListOf() }.add(0, localPo)
                Result.success(localPo)
            }
        } catch (_: Exception) {
            val generatedPoNumber = poNumber ?: ("PO-" + (System.currentTimeMillis() % 100000))
            val localItems = items.mapIndexed { idx, it ->
                PurchaseOrderItem(
                    id = "poi_" + UUID.randomUUID().toString().take(8),
                    purchaseOrderId = generatedPoNumber,
                    materialId = it.materialId,
                    materialName = "Material item ${idx + 1}",
                    unit = "Units",
                    orderedQuantity = it.orderedQuantity,
                    unitPrice = it.unitPrice,
                    totalPrice = it.orderedQuantity.multiply(it.unitPrice).setScale(2, RoundingMode.HALF_UP),
                    receivedQuantity = BigDecimal.ZERO
                )
            }
            val localPo = PurchaseOrder(
                id = "po_" + UUID.randomUUID().toString().take(8),
                poNumber = generatedPoNumber,
                projectId = projectId,
                vendorId = vendorId,
                vendorName = vendorName,
                orderDate = orderDate,
                deliveryDeadline = deliveryDeadline,
                totalAmount = totalAmount,
                status = PurchaseOrderStatus.ISSUED,
                createdBy = "Project Manager",
                idempotencyKey = idempotencyKey,
                items = localItems,
                createdAt = "Just now"
            )
            localPoCache.getOrPut(projectId) { mutableListOf() }.add(0, localPo)
            Result.success(localPo)
        }
    }

    suspend fun createPurchaseOrder(
        projectId: String,
        vendorId: String,
        poNumber: String?,
        expectedDeliveryDate: String?,
        paymentTerms: String? = null,
        notes: String? = null,
        items: List<CreatePurchaseOrderItemRequest>,
        idempotencyKey: String? = null
    ): Result<PurchaseOrder> = createPurchaseOrder(
        projectId = projectId,
        poNumber = poNumber,
        vendorId = vendorId,
        orderDate = java.time.LocalDate.now().toString(),
        deliveryDeadline = expectedDeliveryDate,
        items = items,
        idempotencyKey = idempotencyKey
    )

    suspend fun recordGrn(
        projectId: String,
        poId: String,
        materialId: String,
        challanNumber: String,
        receivedQuantity: BigDecimal,
        receivedDate: String,
        qualityRemarks: String? = null
    ): Result<GoodsReceiptNote> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.recordGrn(
                projectId,
                CreateGrnRequest(poId, materialId, challanNumber, receivedQuantity, receivedDate, qualityRemarks)
            )
            if (response.isSuccessful && response.body() != null) {
                val grn = mapGrnDto(response.body()!!)
                localGrnCache.getOrPut(poId) { mutableListOf() }.add(0, grn)
                updateLocalPoReceivedQuantity(projectId, poId, materialId, receivedQuantity)
                Result.success(grn)
            } else {
                recordLocalGrn(projectId, poId, materialId, challanNumber, receivedQuantity, receivedDate, qualityRemarks)
            }
        } catch (_: Exception) {
            recordLocalGrn(projectId, poId, materialId, challanNumber, receivedQuantity, receivedDate, qualityRemarks)
        }
    }

    suspend fun recordGrn(
        po: PurchaseOrder,
        lineItemId: String,
        challanNumber: String,
        receivedQuantity: BigDecimal,
        deliveryDate: String,
        remarks: String? = null
    ): Result<GoodsReceiptNote> {
        val materialId = po.items.find { it.id == lineItemId }?.materialId ?: lineItemId
        return recordGrn(
            projectId = po.projectId,
            poId = po.id,
            materialId = materialId,
            challanNumber = challanNumber,
            receivedQuantity = receivedQuantity,
            receivedDate = deliveryDate,
            qualityRemarks = remarks
        )
    }

    private fun recordLocalGrn(
        projectId: String,
        poId: String,
        materialId: String,
        challanNumber: String,
        receivedQuantity: BigDecimal,
        receivedDate: String,
        qualityRemarks: String?
    ): Result<GoodsReceiptNote> {
        updateLocalPoReceivedQuantity(projectId, poId, materialId, receivedQuantity)
        val po = localPoCache[projectId]?.find { it.id == poId }
        val matItem = po?.items?.find { it.materialId == materialId }
        val grn = GoodsReceiptNote(
            id = "grn_" + UUID.randomUUID().toString().take(8),
            purchaseOrderId = poId,
            poNumber = po?.poNumber,
            materialId = materialId,
            materialName = matItem?.materialName ?: "Material",
            unit = matItem?.unit ?: "units",
            challanNumber = challanNumber,
            receivedQuantity = receivedQuantity,
            receivedDate = receivedDate,
            inspectedBy = "Storekeeper",
            qualityRemarks = qualityRemarks,
            createdAt = "Just now"
        )
        localGrnCache.getOrPut(poId) { mutableListOf() }.add(0, grn)
        return Result.success(grn)
    }

    private fun updateLocalPoReceivedQuantity(projectId: String, poId: String, materialId: String, receivedQty: BigDecimal) {
        val list = localPoCache[projectId] ?: return
        val poIdx = list.indexOfFirst { it.id == poId }
        if (poIdx < 0) return
        val po = list[poIdx]

        val updatedItems = po.items.map { item ->
            if (item.materialId == materialId) {
                item.copy(receivedQuantity = item.receivedQuantity.add(receivedQty))
            } else {
                item
            }
        }

        val allFulfilled = updatedItems.all { it.receivedQuantity >= it.orderedQuantity }
        val anyReceived = updatedItems.any { it.receivedQuantity > BigDecimal.ZERO }

        val newStatus = when {
            allFulfilled -> PurchaseOrderStatus.COMPLETED
            anyReceived -> PurchaseOrderStatus.PARTIALLY_DELIVERED
            else -> po.status
        }

        list[poIdx] = po.copy(items = updatedItems, status = newStatus)
    }

    suspend fun getGrnsForPo(projectId: String, poId: String): Result<List<GoodsReceiptNote>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getGrnsForPo(projectId, poId)
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                val mapped = response.body()!!.map { mapGrnDto(it) }
                localGrnCache[poId] = mapped.toMutableList()
                Result.success(mapped)
            } else {
                Result.success(localGrnCache[poId] ?: emptyList())
            }
        } catch (_: Exception) {
            Result.success(localGrnCache[poId] ?: emptyList())
        }
    }

    // -------------------------------------------------------------------------
    // Cost Dashboard & Summary
    // -------------------------------------------------------------------------

    suspend fun getCostDashboard(projectId: String, period: DashboardPeriod = DashboardPeriod.TOTAL): Result<CostDashboardSummary> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCostDashboard(projectId, period.name)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(mapCostSummaryDto(response.body()!!))
                } else {
                    Result.success(buildCostDashboardFromLocal(projectId, period))
                }
            } catch (_: Exception) {
                Result.success(buildCostDashboardFromLocal(projectId, period))
            }
        }

    private fun buildCostDashboardFromLocal(projectId: String, period: DashboardPeriod): CostDashboardSummary {
        val totalBudget = BigDecimal("50000000.00") // 5 Cr

        val expenses = localExpenseCache[projectId] ?: emptyList()
        val bills = localBillCache[projectId] ?: emptyList()
        val pos = localPoCache[projectId] ?: emptyList()

        val paidExpenses = expenses.filter { it.status == ExpenseStatus.PAID }.map { it.amount }.reduceOrNull(BigDecimal::add) ?: BigDecimal.ZERO
        val paidBills = bills.filter { it.status == BillPaymentStatus.PAID }.map { it.netPayable }.reduceOrNull(BigDecimal::add) ?: BigDecimal.ZERO
        val actualCost = paidExpenses.add(paidBills)

        val poCommitments = pos.filter { it.status == PurchaseOrderStatus.ISSUED || it.status == PurchaseOrderStatus.PARTIALLY_DELIVERED }
            .map { it.totalAmount }.reduceOrNull(BigDecimal::add) ?: BigDecimal.ZERO
        val approvedBills = bills.filter { it.status == BillPaymentStatus.APPROVED }
            .map { it.netPayable }.reduceOrNull(BigDecimal::add) ?: BigDecimal.ZERO
        val committedCost = poCommitments.add(approvedBills)

        val remainingBudget = totalBudget.subtract(actualCost)
        val projectedFinalCost = actualCost.add(committedCost)

        val budgetUsedPct = if (totalBudget > BigDecimal.ZERO) {
            actualCost.multiply(BigDecimal.valueOf(100)).divide(totalBudget, 1, RoundingMode.HALF_UP)
        } else BigDecimal.ZERO

        val breakdown = listOf(
            CategoryCost("MATERIALS", "Materials (Steel, Cement, Aggregates)", BigDecimal("20000000.00"), poCommitments, BigDecimal("45.0")),
            CategoryCost("LABOR", "Direct Labor & Gang Overtime", BigDecimal("12000000.00"), BigDecimal("42000.00"), BigDecimal("22.0")),
            CategoryCost("SUBCONTRACTOR", "Subcontractor RA Bills", BigDecimal("10000000.00"), approvedBills, BigDecimal("20.0")),
            CategoryCost("EQUIPMENT", "Machinery, Cranes & Rig Rental", BigDecimal("5000000.00"), BigDecimal("120000.00"), BigDecimal("10.0")),
            CategoryCost("UTILITIES", "Power, Water & Environmental Permits", BigDecimal("3000000.00"), BigDecimal("72000.00"), BigDecimal("3.0"))
        )

        val topMaterials = listOf(
            MaterialConsumption("mat_steel_16", "16mm TMT Fe550D Rebar", "MT", BigDecimal("185.0"), BigDecimal("12025000.00"), BigDecimal("44.8")),
            MaterialConsumption("mat_cem_opc", "OPC 53 Grade Cement", "Bags", BigDecimal("14200.0"), BigDecimal("5396000.00"), BigDecimal("20.1")),
            MaterialConsumption("mat_sand_m", "Manufactured M-Sand", "Ton", BigDecimal("2800.0"), BigDecimal("3500000.00"), BigDecimal("13.1"))
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

    // -------------------------------------------------------------------------
    // Mappers
    // -------------------------------------------------------------------------

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
            paymentReference = dto.paymentReference,
            rejectionReason = dto.rejectionReason,
            idempotencyKey = dto.idempotencyKey,
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
            paymentReference = dto.paymentReference,
            idempotencyKey = dto.idempotencyKey,
            createdAt = dto.createdAt
        )
    }

    private fun mapVendorDto(dto: VendorNetworkDto): Vendor {
        return Vendor(
            id = dto.id,
            organizationId = dto.organizationId,
            name = dto.name,
            gstin = dto.gstin,
            phone = dto.phone,
            email = dto.email,
            category = dto.category,
            paymentTerms = dto.paymentTerms
        )
    }

    private fun mapPoDto(dto: PurchaseOrderNetworkDto): PurchaseOrder {
        val st = try {
            PurchaseOrderStatus.valueOf(dto.status.uppercase())
        } catch (_: Exception) {
            PurchaseOrderStatus.ISSUED
        }
        val mappedItems = dto.items.map {
            PurchaseOrderItem(
                id = it.id ?: UUID.randomUUID().toString(),
                purchaseOrderId = dto.id,
                materialId = it.materialId,
                materialName = it.materialName ?: "Material",
                unit = it.unit ?: "units",
                orderedQuantity = it.orderedQuantity,
                unitPrice = it.unitPrice,
                totalPrice = it.totalPrice ?: it.orderedQuantity.multiply(it.unitPrice),
                receivedQuantity = it.receivedQuantity
            )
        }
        return PurchaseOrder(
            id = dto.id,
            poNumber = dto.poNumber,
            projectId = dto.projectId,
            vendorId = dto.vendorId,
            vendorName = dto.vendorName,
            orderDate = dto.orderDate,
            deliveryDeadline = dto.deliveryDeadline,
            totalAmount = dto.totalAmount,
            status = st,
            createdBy = dto.createdBy,
            idempotencyKey = dto.idempotencyKey,
            items = mappedItems,
            createdAt = dto.createdAt
        )
    }

    private fun mapGrnDto(dto: GoodsReceiptNoteNetworkDto): GoodsReceiptNote {
        return GoodsReceiptNote(
            id = dto.id,
            purchaseOrderId = dto.purchaseOrderId,
            poNumber = dto.poNumber,
            materialId = dto.materialId,
            materialName = dto.materialName,
            unit = dto.unit,
            challanNumber = dto.challanNumber,
            receivedQuantity = dto.receivedQuantity,
            receivedDate = dto.receivedDate,
            inspectedBy = dto.inspectedBy,
            qualityRemarks = dto.qualityRemarks,
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
