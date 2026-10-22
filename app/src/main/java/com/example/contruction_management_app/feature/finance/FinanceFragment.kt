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
import com.example.contruction_management_app.databinding.FragmentFinanceBinding
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class FinanceFragment : Fragment() {

    private var _binding: FragmentFinanceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FinanceViewModel by viewModels()
    private var projectId: String = "prj_commercial_tower_b"

    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var billAdapter: ContractorBillAdapter

    private var selectedTabIndex = 0

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
        setupRecyclerViews()
        setupFab()
        observeState()

        viewModel.loadFinanceData(projectId)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
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
        if (selectedTabIndex == 0) {
            binding.rvExpenses.visibility = View.VISIBLE
            binding.rvContractorBills.visibility = View.GONE
            binding.fabAddFinanceItem.text = "Add Expense"
        } else {
            binding.rvExpenses.visibility = View.GONE
            binding.rvContractorBills.visibility = View.VISIBLE
            binding.fabAddFinanceItem.text = "New RA Bill"
        }
    }

    private fun setupRecyclerViews() {
        expenseAdapter = ExpenseAdapter { expense ->
            viewModel.advanceExpenseStatus(expense)
            Toast.makeText(requireContext(), "Updated status for ${expense.title}", Toast.LENGTH_SHORT).show()
        }
        binding.rvExpenses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExpenses.adapter = expenseAdapter

        billAdapter = ContractorBillAdapter { bill ->
            viewModel.advanceBillStatus(bill)
            Toast.makeText(requireContext(), "Updated status for ${bill.billNumber}", Toast.LENGTH_SHORT).show()
        }
        binding.rvContractorBills.layoutManager = LinearLayoutManager(requireContext())
        binding.rvContractorBills.adapter = billAdapter
    }

    private fun setupFab() {
        binding.fabAddFinanceItem.setOnClickListener {
            if (selectedTabIndex == 0) {
                val dialog = AddExpenseDialog { category, title, amount, date ->
                    viewModel.createExpense(category, title, amount, date)
                    Toast.makeText(requireContext(), "Logged expense: $title", Toast.LENGTH_SHORT).show()
                }
                dialog.show(childFragmentManager, AddExpenseDialog.TAG)
            } else {
                val dialog = CreateContractorBillDialog { contractor, billNo, gross, date, workDesc ->
                    viewModel.createContractorBill(contractor, billNo, gross, date, workDesc)
                    Toast.makeText(requireContext(), "Certified RA bill for $contractor", Toast.LENGTH_SHORT).show()
                }
                dialog.show(childFragmentManager, CreateContractorBillDialog.TAG)
            }
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is FinanceUiState.Loading -> {
                            binding.loadingState.root.visibility = View.VISIBLE
                            binding.emptyState.root.visibility = View.GONE
                        }
                        is FinanceUiState.Success -> {
                            binding.loadingState.root.visibility = View.GONE
                            expenseAdapter.submitList(state.expenses)
                            billAdapter.submitList(state.contractorBills)

                            val isEmpty = if (selectedTabIndex == 0) state.expenses.isEmpty() else state.contractorBills.isEmpty()
                            if (isEmpty) {
                                binding.emptyState.root.visibility = View.VISIBLE
                                binding.emptyState.tvEmptyTitle.text = if (selectedTabIndex == 0) "No expenses recorded" else "No contractor bills"
                                binding.emptyState.tvEmptySubtitle.text = "Tap the button below to add an entry"
                            } else {
                                binding.emptyState.root.visibility = View.GONE
                            }
                        }
                        is FinanceUiState.Error -> {
                            binding.loadingState.root.visibility = View.GONE
                            binding.emptyState.root.visibility = View.VISIBLE
                            binding.emptyState.tvEmptyTitle.text = "Error loading finance records"
                            binding.emptyState.tvEmptySubtitle.text = state.message
                        }
                    }
                }
            }
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
