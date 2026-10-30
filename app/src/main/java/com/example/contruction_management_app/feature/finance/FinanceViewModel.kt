package com.example.contruction_management_app.feature.finance

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.contruction_management_app.core.network.CreatePurchaseOrderItemRequest
import com.example.contruction_management_app.core.network.NetworkClient
import com.example.contruction_management_app.domain.model.*
import com.example.contruction_management_app.domain.repository.FinanceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal

sealed interface ExpensesUiState {
    object Loading : ExpensesUiState
    data class Success(
        val allExpenses: List<Expense>,
        val filteredExpenses: List<Expense>,
        val selectedCategory: ExpenseCategory? = null
    ) : ExpensesUiState
    data class Error(val message: String) : ExpensesUiState
}

sealed interface BillsUiState {
    object Loading : BillsUiState
    data class Success(val bills: List<ContractorBill>) : BillsUiState
    data class Error(val message: String) : BillsUiState
}

sealed interface PurchaseOrdersUiState {
    object Loading : PurchaseOrdersUiState
    data class Success(val purchaseOrders: List<PurchaseOrder>) : PurchaseOrdersUiState
    data class Error(val message: String) : PurchaseOrdersUiState
}

sealed interface VendorsUiState {
    object Loading : VendorsUiState
    data class Success(val vendors: List<Vendor>) : VendorsUiState
    data class Error(val message: String) : VendorsUiState
}

sealed interface FinanceUiState {
    object Loading : FinanceUiState
    data class Success(
        val expenses: List<Expense>,
        val contractorBills: List<ContractorBill>
    ) : FinanceUiState
    data class Error(val message: String) : FinanceUiState
}

class FinanceViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: FinanceRepository = FinanceRepository(NetworkClient.getApiService(application))
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<FinanceUiState>(FinanceUiState.Loading)
    val uiState: StateFlow<FinanceUiState> = _uiState.asStateFlow()

    private val _expensesState = MutableStateFlow<ExpensesUiState>(ExpensesUiState.Loading)
    val expensesState: StateFlow<ExpensesUiState> = _expensesState.asStateFlow()

    private val _billsState = MutableStateFlow<BillsUiState>(BillsUiState.Loading)
    val billsState: StateFlow<BillsUiState> = _billsState.asStateFlow()

    private val _purchaseOrdersState = MutableStateFlow<PurchaseOrdersUiState>(PurchaseOrdersUiState.Loading)
    val purchaseOrdersState: StateFlow<PurchaseOrdersUiState> = _purchaseOrdersState.asStateFlow()

    private val _vendorsState = MutableStateFlow<VendorsUiState>(VendorsUiState.Loading)
    val vendorsState: StateFlow<VendorsUiState> = _vendorsState.asStateFlow()

    private val _actionMessage = MutableSharedFlow<String>()
    val actionMessage: SharedFlow<String> = _actionMessage.asSharedFlow()

    private var currentProjectId: String = "prj_commercial_tower_b"
    private var currentOrganizationId: String = "org_apex_1"

    private var allExpensesList = listOf<Expense>()
    private var currentExpenseCategory: ExpenseCategory? = null

    fun loadAll(projectId: String = currentProjectId, orgId: String = currentOrganizationId) {
        currentProjectId = projectId
        currentOrganizationId = orgId
        loadExpenses(projectId)
        loadContractorBills(projectId)
        loadPurchaseOrders(projectId)
        loadVendors(orgId)
    }

    fun loadFinanceData(projectId: String) {
        loadAll(projectId, currentOrganizationId)
    }

    // -------------------------------------------------------------------------
    // Expenses
    // -------------------------------------------------------------------------

    fun loadExpenses(projectId: String = currentProjectId) {
        currentProjectId = projectId
        viewModelScope.launch {
            _expensesState.value = ExpensesUiState.Loading
            val result = repository.getExpenses(projectId)
            result.fold(
                onSuccess = { list ->
                    allExpensesList = list
                    applyExpenseFilter()
                    updateLegacyUiState()
                },
                onFailure = {
                    _expensesState.value = ExpensesUiState.Error(it.message ?: "Failed to load expenses")
                }
            )
        }
    }

    fun filterExpenseCategory(category: ExpenseCategory?) {
        currentExpenseCategory = category
        applyExpenseFilter()
    }

    private fun applyExpenseFilter() {
        val filtered = if (currentExpenseCategory == null) {
            allExpensesList
        } else {
            allExpensesList.filter { it.category == currentExpenseCategory }
        }
        _expensesState.value = ExpensesUiState.Success(
            allExpenses = allExpensesList,
            filteredExpenses = filtered,
            selectedCategory = currentExpenseCategory
        )
    }

    fun createExpense(
        category: ExpenseCategory,
        title: String,
        amount: BigDecimal,
        date: String,
        receiptUrl: String? = null,
        onComplete: ((Boolean, String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = repository.createExpense(
                projectId = currentProjectId,
                category = category,
                title = title,
                amount = amount,
                expenseDate = date,
                receiptUrl = receiptUrl
            )
            result.fold(
                onSuccess = {
                    val msg = "Expense submitted for verification (${it.title})"
                    _actionMessage.emit(msg)
                    onComplete?.invoke(true, msg)
                    loadExpenses(currentProjectId)
                },
                onFailure = {
                    val err = it.message ?: "Failed to submit expense"
                    _actionMessage.emit(err)
                    onComplete?.invoke(false, err)
                }
            )
        }
    }

    fun advanceExpenseStatus(expense: Expense, onComplete: ((Boolean, String) -> Unit)? = null) {
        val nextStatus = when (expense.status) {
            ExpenseStatus.SUBMITTED -> ExpenseStatus.VERIFIED
            ExpenseStatus.VERIFIED -> ExpenseStatus.APPROVED
            ExpenseStatus.APPROVED -> ExpenseStatus.PAID
            else -> return
        }
        val paymentRef = if (nextStatus == ExpenseStatus.PAID) "PAY-" + System.currentTimeMillis() % 1000000 else null

        viewModelScope.launch {
            val result = repository.updateExpenseStatus(
                projectId = currentProjectId,
                expenseId = expense.id,
                status = nextStatus,
                paymentReference = paymentRef
            )
            result.fold(
                onSuccess = {
                    val msg = "Expense advanced to ${nextStatus.name} (${it.title})"
                    _actionMessage.emit(msg)
                    onComplete?.invoke(true, msg)
                    loadExpenses(currentProjectId)
                },
                onFailure = {
                    val err = it.message ?: "Failed to advance expense (check dual-control rule)"
                    _actionMessage.emit(err)
                    onComplete?.invoke(false, err)
                }
            )
        }
    }

    fun rejectExpense(expenseId: String, reason: String, onComplete: ((Boolean, String) -> Unit)? = null) {
        viewModelScope.launch {
            val result = repository.updateExpenseStatus(
                projectId = currentProjectId,
                expenseId = expenseId,
                status = ExpenseStatus.REJECTED,
                rejectionReason = reason
            )
            result.fold(
                onSuccess = {
                    val msg = "Expense rejected"
                    _actionMessage.emit(msg)
                    onComplete?.invoke(true, msg)
                    loadExpenses(currentProjectId)
                },
                onFailure = {
                    val err = it.message ?: "Failed to reject expense"
                    _actionMessage.emit(err)
                    onComplete?.invoke(false, err)
                }
            )
        }
    }

    // -------------------------------------------------------------------------
    // Contractor RA Bills
    // -------------------------------------------------------------------------

    fun loadContractorBills(projectId: String = currentProjectId) {
        currentProjectId = projectId
        viewModelScope.launch {
            _billsState.value = BillsUiState.Loading
            val result = repository.getContractorBills(projectId)
            result.fold(
                onSuccess = { list ->
                    _billsState.value = BillsUiState.Success(list)
                    updateLegacyUiState()
                },
                onFailure = {
                    _billsState.value = BillsUiState.Error(it.message ?: "Failed to load contractor bills")
                }
            )
        }
    }

    fun createContractorBill(
        contractorName: String,
        billNumber: String?,
        grossAmount: BigDecimal,
        date: String,
        retentionPct: BigDecimal = BigDecimal.valueOf(5.00),
        tdsPct: BigDecimal = BigDecimal.valueOf(2.00),
        workDescription: String? = null,
        onComplete: ((Boolean, String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = repository.createContractorBill(
                projectId = currentProjectId,
                contractorName = contractorName,
                billNumber = billNumber,
                billDate = date,
                grossAmount = grossAmount,
                retentionPct = retentionPct,
                tdsPct = tdsPct,
                workDescription = workDescription
            )
            result.fold(
                onSuccess = {
                    val msg = "Contractor bill created (${it.billNumber})"
                    _actionMessage.emit(msg)
                    onComplete?.invoke(true, msg)
                    loadContractorBills(currentProjectId)
                },
                onFailure = {
                    val err = it.message ?: "Failed to create contractor bill"
                    _actionMessage.emit(err)
                    onComplete?.invoke(false, err)
                }
            )
        }
    }

    fun advanceBillStatus(bill: ContractorBill, onComplete: ((Boolean, String) -> Unit)? = null) {
        val nextStatus = when (bill.status) {
            BillPaymentStatus.PENDING -> BillPaymentStatus.APPROVED
            BillPaymentStatus.APPROVED -> BillPaymentStatus.PAID
            else -> return
        }
        val paymentRef = if (nextStatus == BillPaymentStatus.PAID) "BILL-PAY-" + System.currentTimeMillis() % 1000000 else null

        viewModelScope.launch {
            val result = repository.updateContractorBillStatus(
                projectId = currentProjectId,
                billId = bill.id,
                status = nextStatus,
                paymentReference = paymentRef
            )
            result.fold(
                onSuccess = {
                    val msg = "Bill advanced to ${nextStatus.name} (${it.billNumber})"
                    _actionMessage.emit(msg)
                    onComplete?.invoke(true, msg)
                    loadContractorBills(currentProjectId)
                },
                onFailure = {
                    val err = it.message ?: "Failed to update bill status"
                    _actionMessage.emit(err)
                    onComplete?.invoke(false, err)
                }
            )
        }
    }

    // -------------------------------------------------------------------------
    // Purchase Orders & GRN
    // -------------------------------------------------------------------------

    fun loadPurchaseOrders(projectId: String = currentProjectId) {
        currentProjectId = projectId
        viewModelScope.launch {
            _purchaseOrdersState.value = PurchaseOrdersUiState.Loading
            val result = repository.getPurchaseOrders(projectId)
            result.fold(
                onSuccess = { list ->
                    _purchaseOrdersState.value = PurchaseOrdersUiState.Success(list)
                },
                onFailure = {
                    _purchaseOrdersState.value = PurchaseOrdersUiState.Error(it.message ?: "Failed to load purchase orders")
                }
            )
        }
    }

    fun createPurchaseOrder(
        poNumber: String?,
        vendorId: String,
        orderDate: String,
        deliveryDeadline: String?,
        items: List<CreatePurchaseOrderItemRequest>,
        onComplete: ((Boolean, String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = repository.createPurchaseOrder(
                projectId = currentProjectId,
                poNumber = poNumber,
                vendorId = vendorId,
                orderDate = orderDate,
                deliveryDeadline = deliveryDeadline,
                items = items
            )
            result.fold(
                onSuccess = {
                    val msg = "Purchase order issued (${it.poNumber})"
                    _actionMessage.emit(msg)
                    onComplete?.invoke(true, msg)
                    loadPurchaseOrders(currentProjectId)
                },
                onFailure = {
                    val err = it.message ?: "Failed to create purchase order"
                    _actionMessage.emit(err)
                    onComplete?.invoke(false, err)
                }
            )
        }
    }

    fun createPurchaseOrder(
        vendorId: String,
        poNumber: String?,
        expectedDeliveryDate: String?,
        paymentTerms: String? = null,
        notes: String? = null,
        items: List<CreatePurchaseOrderItemRequest>,
        onComplete: ((Boolean, String) -> Unit)? = null
    ) {
        val today = java.time.LocalDate.now().toString()
        createPurchaseOrder(
            poNumber = poNumber,
            vendorId = vendorId,
            orderDate = today,
            deliveryDeadline = expectedDeliveryDate,
            items = items,
            onComplete = onComplete
        )
    }

    fun recordGrn(
        po: PurchaseOrder,
        lineItemId: String,
        challanNumber: String,
        receivedQuantity: BigDecimal,
        deliveryDate: String,
        remarks: String? = null,
        onComplete: ((Boolean, String) -> Unit)? = null
    ) {
        val materialId = po.items.find { it.id == lineItemId }?.materialId ?: lineItemId
        recordGrn(
            poId = po.id,
            materialId = materialId,
            challanNumber = challanNumber,
            receivedQuantity = receivedQuantity,
            receivedDate = deliveryDate,
            qualityRemarks = remarks,
            onComplete = onComplete
        )
    }

    fun recordGrn(
        poId: String,
        materialId: String,
        challanNumber: String,
        receivedQuantity: BigDecimal,
        receivedDate: String,
        qualityRemarks: String? = null,
        onComplete: ((Boolean, String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = repository.recordGrn(
                projectId = currentProjectId,
                poId = poId,
                materialId = materialId,
                challanNumber = challanNumber,
                receivedQuantity = receivedQuantity,
                receivedDate = receivedDate,
                qualityRemarks = qualityRemarks
            )
            result.fold(
                onSuccess = {
                    val msg = "Delivery received via GRN (Challan: ${it.challanNumber})"
                    _actionMessage.emit(msg)
                    onComplete?.invoke(true, msg)
                    loadPurchaseOrders(currentProjectId)
                },
                onFailure = {
                    val err = it.message ?: "Failed to record GRN"
                    _actionMessage.emit(err)
                    onComplete?.invoke(false, err)
                }
            )
        }
    }

    // -------------------------------------------------------------------------
    // Vendors
    // -------------------------------------------------------------------------

    fun loadVendors(orgId: String = currentOrganizationId) {
        currentOrganizationId = orgId
        viewModelScope.launch {
            _vendorsState.value = VendorsUiState.Loading
            val result = repository.getVendors(orgId)
            result.fold(
                onSuccess = { list ->
                    _vendorsState.value = VendorsUiState.Success(list)
                },
                onFailure = {
                    _vendorsState.value = VendorsUiState.Error(it.message ?: "Failed to load vendors")
                }
            )
        }
    }

    fun createVendor(
        name: String,
        gstin: String?,
        phone: String?,
        email: String?,
        category: String?,
        paymentTerms: String?,
        onComplete: ((Boolean, String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = repository.createVendor(
                organizationId = currentOrganizationId,
                name = name,
                gstin = gstin,
                phone = phone,
                email = email,
                category = category,
                paymentTerms = paymentTerms
            )
            result.fold(
                onSuccess = {
                    val msg = "Vendor added (${it.name})"
                    _actionMessage.emit(msg)
                    onComplete?.invoke(true, msg)
                    loadVendors(currentOrganizationId)
                },
                onFailure = {
                    val err = it.message ?: "Failed to add vendor"
                    _actionMessage.emit(err)
                    onComplete?.invoke(false, err)
                }
            )
        }
    }

    fun createVendor(
        name: String,
        category: VendorCategory,
        contactPerson: String? = null,
        phone: String? = null,
        email: String? = null,
        gstin: String? = null,
        paymentTerms: String? = null,
        onComplete: ((Boolean, String) -> Unit)? = null
    ) {
        createVendor(
            name = name,
            gstin = gstin,
            phone = phone,
            email = email,
            category = category.name,
            paymentTerms = paymentTerms,
            onComplete = onComplete
        )
    }

    private fun updateLegacyUiState() {
        val expenses = (_expensesState.value as? ExpensesUiState.Success)?.allExpenses ?: emptyList()
        val bills = (_billsState.value as? BillsUiState.Success)?.bills ?: emptyList()
        _uiState.value = FinanceUiState.Success(expenses = expenses, contractorBills = bills)
    }
}
