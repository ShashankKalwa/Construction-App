package com.example.contruction_management_app.feature.finance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.contruction_management_app.R
import com.example.contruction_management_app.databinding.FragmentFinanceBinding
import com.example.contruction_management_app.domain.model.ExpenseCategory
import com.example.contruction_management_app.domain.model.Vendor
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class FinanceFragment : Fragment() {

    private var _binding: FragmentFinanceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FinanceViewModel by viewModels()
    private var projectId: String = "prj_commercial_tower_b"

    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var billAdapter: ContractorBillAdapter
    private lateinit var poAdapter: PurchaseOrderAdapter

    private var selectedTabIndex = 0
    private var cachedVendors: List<Vendor> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFinanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        projectId = arguments?.getString(ARG_PROJECT_ID) ?: "prj_commercial_tower_b"

        setupToolbar()
        setupTabs()
        setupCategoryChips()
        setupRecyclerViews()
        setupFab()
        observeState()

        viewModel.loadAll(projectId)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_cost_dashboard -> {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, BudgetDashboardFragment.newInstance(projectId))
                        .addToBackStack(null)
                        .commit()
                    true
                }
                R.id.action_add_vendor -> {
                    showAddVendorDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupTabs() {
        binding.tabLayoutFinance.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                selectedTabIndex = tab?.position ?: 0
                updateTabVisibility()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun updateTabVisibility() {
        when (selectedTabIndex) {
            0 -> {
                binding.layoutExpenses.visibility = View.VISIBLE
                binding.rvContractorBills.visibility = View.GONE
                binding.rvPurchaseOrders.visibility = View.GONE
                binding.fabAddFinanceItem.text = "Add Expense"
                updateEmptyStateForCurrentTab()
            }
            1 -> {
                binding.layoutExpenses.visibility = View.GONE
                binding.rvContractorBills.visibility = View.VISIBLE
                binding.rvPurchaseOrders.visibility = View.GONE
                binding.fabAddFinanceItem.text = "New RA Bill"
                updateEmptyStateForCurrentTab()
            }
            2 -> {
                binding.layoutExpenses.visibility = View.GONE
                binding.rvContractorBills.visibility = View.GONE
                binding.rvPurchaseOrders.visibility = View.VISIBLE
                binding.fabAddFinanceItem.text = "Create PO"
                updateEmptyStateForCurrentTab()
            }
        }
    }

    private fun setupCategoryChips() {
        binding.chipCategoryAll.setOnClickListener {
            viewModel.filterExpenseCategory(null)
        }
        binding.chipCategoryMaterials.setOnClickListener {
            viewModel.filterExpenseCategory(ExpenseCategory.MATERIALS)
        }
        binding.chipCategoryLabor.setOnClickListener {
            viewModel.filterExpenseCategory(ExpenseCategory.LABOR)
        }
        binding.chipCategoryEquipment.setOnClickListener {
            viewModel.filterExpenseCategory(ExpenseCategory.EQUIPMENT)
        }
        binding.chipCategorySubcontractor.setOnClickListener {
            viewModel.filterExpenseCategory(ExpenseCategory.SUBCONTRACTOR)
        }
        binding.chipCategoryPettyCash.setOnClickListener {
            viewModel.filterExpenseCategory(ExpenseCategory.PETTY_CASH)
        }
        binding.chipCategoryUtilities.setOnClickListener {
            viewModel.filterExpenseCategory(ExpenseCategory.UTILITIES)
        }
        binding.chipCategorySafety.setOnClickListener {
            viewModel.filterExpenseCategory(ExpenseCategory.SAFETY)
        }
        binding.chipCategoryOther.setOnClickListener {
            viewModel.filterExpenseCategory(ExpenseCategory.OTHER)
        }
    }

    private fun setupRecyclerViews() {
        expenseAdapter = ExpenseAdapter(
            onAdvanceStatusClick = { expense ->
                viewModel.advanceExpenseStatus(expense)
            },
            onRejectClick = { expense ->
                val dialog = RejectExpenseDialog(expense) { reason ->
                    viewModel.rejectExpense(expense.id, reason)
                }
                dialog.show(childFragmentManager, RejectExpenseDialog.TAG)
            }
        )
        binding.rvExpenses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExpenses.adapter = expenseAdapter

        billAdapter = ContractorBillAdapter { bill ->
            viewModel.advanceBillStatus(bill)
        }
        binding.rvContractorBills.layoutManager = LinearLayoutManager(requireContext())
        binding.rvContractorBills.adapter = billAdapter

        poAdapter = PurchaseOrderAdapter { po ->
            val dialog = RecordGrnDialog(po) { poId, materialId, challanNo, receivedQty, receivedDate, remarks ->
                viewModel.recordGrn(
                    poId = poId,
                    materialId = materialId,
                    challanNumber = challanNo,
                    receivedQuantity = receivedQty,
                    receivedDate = receivedDate,
                    qualityRemarks = remarks
                )
            }
            dialog.show(childFragmentManager, RecordGrnDialog.TAG)
        }
        binding.rvPurchaseOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPurchaseOrders.adapter = poAdapter
    }

    private fun setupFab() {
        binding.fabAddFinanceItem.setOnClickListener {
            when (selectedTabIndex) {
                0 -> {
                    val dialog = AddExpenseDialog { category, title, amount, date ->
                        viewModel.createExpense(category, title, amount, date)
                    }
                    dialog.show(childFragmentManager, AddExpenseDialog.TAG)
                }
                1 -> {
                    val dialog = CreateContractorBillDialog { contractor, billNo, gross, date, workDesc ->
                        viewModel.createContractorBill(contractor, billNo, gross, date, workDescription = workDesc)
                    }
                    dialog.show(childFragmentManager, CreateContractorBillDialog.TAG)
                }
                2 -> {
                    if (cachedVendors.isEmpty()) {
                        Toast.makeText(requireContext(), "Register a vendor first to issue POs", Toast.LENGTH_SHORT).show()
                        showAddVendorDialog()
                    } else {
                        val dialog = CreatePurchaseOrderDialog(cachedVendors) { vendorId, poNo, deliveryDate, terms, notes, items ->
                            viewModel.createPurchaseOrder(
                                vendorId = vendorId,
                                poNumber = poNo,
                                expectedDeliveryDate = deliveryDate,
                                paymentTerms = terms,
                                notes = notes,
                                items = items
                            )
                        }
                        dialog.show(childFragmentManager, CreatePurchaseOrderDialog.TAG)
                    }
                }
            }
        }
    }

    private fun showAddVendorDialog() {
        val dialog = AddVendorDialog { name, category, contact, phone, email, gstin, terms ->
            viewModel.createVendor(
                name = name,
                category = category,
                contactPerson = contact,
                phone = phone,
                email = email,
                gstin = gstin,
                paymentTerms = terms
            )
        }
        dialog.show(childFragmentManager, AddVendorDialog.TAG)
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.expensesState.collect { state ->
                        when (state) {
                            is ExpensesUiState.Loading -> {
                                if (selectedTabIndex == 0) binding.loadingState.root.visibility = View.VISIBLE
                            }
                            is ExpensesUiState.Success -> {
                                binding.loadingState.root.visibility = View.GONE
                                expenseAdapter.submitList(state.filteredExpenses)
                                updateEmptyStateForCurrentTab()
                            }
                            is ExpensesUiState.Error -> {
                                binding.loadingState.root.visibility = View.GONE
                                if (selectedTabIndex == 0) {
                                    binding.emptyState.root.visibility = View.VISIBLE
                                    binding.emptyState.tvEmptyTitle.text = "Error loading expenses"
                                    binding.emptyState.tvEmptySubtitle.text = state.message
                                }
                            }
                        }
                    }
                }

                launch {
                    viewModel.billsState.collect { state ->
                        when (state) {
                            is BillsUiState.Loading -> {
                                if (selectedTabIndex == 1) binding.loadingState.root.visibility = View.VISIBLE
                            }
                            is BillsUiState.Success -> {
                                binding.loadingState.root.visibility = View.GONE
                                billAdapter.submitList(state.bills)
                                updateEmptyStateForCurrentTab()
                            }
                            is BillsUiState.Error -> {
                                binding.loadingState.root.visibility = View.GONE
                                if (selectedTabIndex == 1) {
                                    binding.emptyState.root.visibility = View.VISIBLE
                                    binding.emptyState.tvEmptyTitle.text = "Error loading contractor bills"
                                    binding.emptyState.tvEmptySubtitle.text = state.message
                                }
                            }
                        }
                    }
                }

                launch {
                    viewModel.purchaseOrdersState.collect { state ->
                        when (state) {
                            is PurchaseOrdersUiState.Loading -> {
                                if (selectedTabIndex == 2) binding.loadingState.root.visibility = View.VISIBLE
                            }
                            is PurchaseOrdersUiState.Success -> {
                                binding.loadingState.root.visibility = View.GONE
                                poAdapter.submitList(state.purchaseOrders)
                                updateEmptyStateForCurrentTab()
                            }
                            is PurchaseOrdersUiState.Error -> {
                                binding.loadingState.root.visibility = View.GONE
                                if (selectedTabIndex == 2) {
                                    binding.emptyState.root.visibility = View.VISIBLE
                                    binding.emptyState.tvEmptyTitle.text = "Error loading purchase orders"
                                    binding.emptyState.tvEmptySubtitle.text = state.message
                                }
                            }
                        }
                    }
                }

                launch {
                    viewModel.vendorsState.collect { state ->
                        if (state is VendorsUiState.Success) {
                            cachedVendors = state.vendors
                        }
                    }
                }

                launch {
                    viewModel.actionMessage.collect { message ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateEmptyStateForCurrentTab() {
        val isEmpty = when (selectedTabIndex) {
            0 -> (viewModel.expensesState.value as? ExpensesUiState.Success)?.filteredExpenses?.isEmpty() ?: false
            1 -> (viewModel.billsState.value as? BillsUiState.Success)?.bills?.isEmpty() ?: false
            2 -> (viewModel.purchaseOrdersState.value as? PurchaseOrdersUiState.Success)?.purchaseOrders?.isEmpty() ?: false
            else -> false
        }

        if (isEmpty) {
            binding.emptyState.root.visibility = View.VISIBLE
            binding.emptyState.tvEmptyTitle.text = when (selectedTabIndex) {
                0 -> "No site expenses recorded"
                1 -> "No contractor RA bills"
                2 -> "No purchase orders issued"
                else -> "No records found"
            }
            binding.emptyState.tvEmptySubtitle.text = "Tap the button below to add an entry"
        } else {
            binding.emptyState.root.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PROJECT_ID = "projectId"

        fun newInstance(projectId: String): FinanceFragment {
            val frag = FinanceFragment()
            val args = Bundle()
            args.putString(ARG_PROJECT_ID, projectId)
            frag.arguments = args
            return frag
        }
    }
}
