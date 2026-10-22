package com.example.contruction_management_app.feature.finance

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.contruction_management_app.core.network.NetworkClient
import com.example.contruction_management_app.domain.model.*
import com.example.contruction_management_app.domain.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

sealed interface FinanceUiState {
    object Loading : FinanceUiState
    data class Success(
        val expenses: List<Expense>,
        val contractorBills: List<ContractorBill>
    ) : FinanceUiState
    data class Error(val message: String) : FinanceUiState
}

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FinanceRepository(NetworkClient.getApiService(application))

    private val _uiState = MutableStateFlow<FinanceUiState>(FinanceUiState.Loading)
    val uiState: StateFlow<FinanceUiState> = _uiState.asStateFlow()

    private var currentProjectId: String = "prj_commercial_tower_b"
    private var cachedExpenses = listOf<Expense>()
    private var cachedBills = listOf<ContractorBill>()

    fun loadFinanceData(projectId: String) {
        currentProjectId = projectId
        viewModelScope.launch {
            _uiState.value = FinanceUiState.Loading
            val expResult = repository.getExpenses(projectId)
            val billResult = repository.getContractorBills(projectId)

            cachedExpenses = expResult.getOrDefault(emptyList())
            cachedBills = billResult.getOrDefault(emptyList())

            _uiState.value = FinanceUiState.Success(
                expenses = cachedExpenses,
                contractorBills = cachedBills
            )
        }
    }

    fun createExpense(category: ExpenseCategory, title: String, amount: BigDecimal, date: String) {
        viewModelScope.launch {
            repository.createExpense(
                projectId = currentProjectId,
                category = category,
                title = title,
                amount = amount,
                expenseDate = date
            )
            loadFinanceData(currentProjectId)
        }
    }

    fun advanceExpenseStatus(expense: Expense) {
        val nextStatus = when (expense.status) {
            ExpenseStatus.SUBMITTED -> ExpenseStatus.VERIFIED
            ExpenseStatus.VERIFIED -> ExpenseStatus.APPROVED
            ExpenseStatus.APPROVED -> ExpenseStatus.PAID
            else -> return
        }
        viewModelScope.launch {
            repository.updateExpenseStatus(currentProjectId, expense.id, nextStatus)
            loadFinanceData(currentProjectId)
        }
    }

    fun createContractorBill(
        contractorName: String,
        billNumber: String?,
        grossAmount: BigDecimal,
        date: String,
        workDescription: String?
    ) {
        viewModelScope.launch {
            repository.createContractorBill(
                projectId = currentProjectId,
                contractorName = contractorName,
                billNumber = billNumber,
                billDate = date,
                grossAmount = grossAmount,
                workDescription = workDescription
            )
            loadFinanceData(currentProjectId)
        }
    }

    fun advanceBillStatus(bill: ContractorBill) {
        val nextStatus = when (bill.status) {
            BillPaymentStatus.PENDING -> BillPaymentStatus.APPROVED
            BillPaymentStatus.APPROVED -> BillPaymentStatus.PAID
            else -> return
        }
        viewModelScope.launch {
            repository.updateContractorBillStatus(currentProjectId, bill.id, nextStatus)
            loadFinanceData(currentProjectId)
        }
    }
}
