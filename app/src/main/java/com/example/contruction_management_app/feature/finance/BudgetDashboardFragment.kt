package com.example.contruction_management_app.feature.finance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.contruction_management_app.R
import com.example.contruction_management_app.databinding.FragmentBudgetDashboardBinding
import com.example.contruction_management_app.domain.model.CostDashboardSummary
import com.example.contruction_management_app.domain.model.DashboardPeriod
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

class BudgetDashboardFragment : Fragment() {

    private var _binding: FragmentBudgetDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BudgetDashboardViewModel by viewModels()
    private var projectId: String = "prj_commercial_tower_b"
    private lateinit var consumptionAdapter: MaterialConsumptionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        projectId = arguments?.getString(ARG_PROJECT_ID) ?: "prj_commercial_tower_b"

        setupToolbar()
        setupPeriodChips()
        setupRecyclerView()
        observeState()

        viewModel.loadDashboard(projectId, DashboardPeriod.TOTAL)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupPeriodChips() {
        binding.chipGroupPeriod.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val period = when (checkedIds.first()) {
                R.id.chipWeek -> DashboardPeriod.WEEK
                R.id.chipMonth -> DashboardPeriod.MONTH
                R.id.chipQuarter -> DashboardPeriod.QUARTER
                else -> DashboardPeriod.TOTAL
            }
            viewModel.setPeriod(period)
        }
    }

    private fun setupRecyclerView() {
        consumptionAdapter = MaterialConsumptionAdapter()
        binding.rvMaterialConsumption.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMaterialConsumption.adapter = consumptionAdapter
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is BudgetDashboardUiState.Loading -> {
                            // Can show subtle loading if needed
                        }
                        is BudgetDashboardUiState.Success -> {
                            bindSummary(state.summary)
                        }
                        is BudgetDashboardUiState.Error -> {
                            // Keep previous or fallback
                        }
                    }
                }
            }
        }
    }

    private fun bindSummary(summary: CostDashboardSummary) {
        binding.toolbar.subtitle = summary.projectName

        // Stat Card 1
        binding.tvActualCost.text = formatCurrencyInr(summary.actualCost)
        binding.tvCommittedCost.text = "Committed: ${formatCurrencyInr(summary.committedCost)}"

        // Stat Card 2
        binding.tvBudgetUsedPct.text = "${summary.budgetUsedPct}%"
        binding.tvTotalBudget.text = "of ${formatCurrencyInr(summary.totalBudget)} Budget"

        // Remaining Balance
        binding.tvRemainingBudget.text = formatCurrencyInr(summary.remainingBudget)
        binding.progressBudget.progress = summary.budgetUsedPct.toInt().coerceIn(0, 100)

        // Category breakdown
        summary.costBreakdown.forEach { cat ->
            when (cat.category.uppercase()) {
                "MATERIALS" -> {
                    binding.tvMaterialsCost.text = "${formatCurrencyInr(cat.actualAmount)} (${cat.percentage}%)"
                    binding.progressMaterials.progress = cat.percentage.toInt().coerceIn(0, 100)
                }
                "LABOR" -> {
                    binding.tvLaborCost.text = "${formatCurrencyInr(cat.actualAmount)} (${cat.percentage}%)"
                    binding.progressLabor.progress = cat.percentage.toInt().coerceIn(0, 100)
                }
                "SUBCONTRACTOR" -> {
                    binding.tvSubcontractorCost.text = "${formatCurrencyInr(cat.actualAmount)} (${cat.percentage}%)"
                    binding.progressSubcontractor.progress = cat.percentage.toInt().coerceIn(0, 100)
                }
                "EQUIPMENT" -> {
                    binding.tvEquipmentCost.text = "${formatCurrencyInr(cat.actualAmount)} (${cat.percentage}%)"
                    binding.progressEquipment.progress = cat.percentage.toInt().coerceIn(0, 100)
                }
            }
        }

        // Top Material Consumption list
        consumptionAdapter.submitList(summary.topMaterialConsumption)
    }

    private fun formatCurrencyInr(amount: BigDecimal): String {
        return if (amount.compareTo(BigDecimal("10000000")) >= 0) {
            val cr = amount.divide(BigDecimal("10000000"), 2, java.math.RoundingMode.HALF_UP)
            "₹$cr Cr"
        } else if (amount.compareTo(BigDecimal("100000")) >= 0) {
            val l = amount.divide(BigDecimal("100000"), 1, java.math.RoundingMode.HALF_UP)
            "₹$l L"
        } else {
            val inrFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
                maximumFractionDigits = 0
            }
            inrFormat.format(amount)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PROJECT_ID = "projectId"

        fun newInstance(projectId: String): BudgetDashboardFragment {
            val frag = BudgetDashboardFragment()
            val args = Bundle()
            args.putString(ARG_PROJECT_ID, projectId)
            frag.arguments = args
            return frag
        }
    }
}
