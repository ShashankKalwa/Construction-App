package com.example.contruction_management_app.feature.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.contruction_management_app.R
import com.example.contruction_management_app.databinding.FragmentProjectDetailBinding
import com.example.contruction_management_app.domain.model.ProjectDetail
import com.example.contruction_management_app.feature.task.TaskListFragment
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

import com.example.contruction_management_app.feature.boq.BoqListFragment
import com.example.contruction_management_app.feature.documents.DocumentListFragment
import com.example.contruction_management_app.feature.audit.AuditLogFragment
import com.example.contruction_management_app.feature.finance.BudgetDashboardFragment
import com.example.contruction_management_app.feature.finance.FinanceFragment
import com.example.contruction_management_app.feature.inventory.InventoryFragment
import com.example.contruction_management_app.feature.measurement.MeasurementBookFragment
import com.example.contruction_management_app.feature.siteops.SiteOpsFragment

class ProjectDetailFragment : Fragment() {

    private var _binding: FragmentProjectDetailBinding? = null
    private val binding get() = _binding!!

    private val projectViewModel: ProjectViewModel by activityViewModels()
    private var projectId: String = "prj_commercial_tower_b"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProjectDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        projectId = arguments?.getString("projectId") ?: "prj_commercial_tower_b"

        setupToolbar()
        setupViewPager()
        observeDetail()

        projectViewModel.loadProjectDetail(projectId)
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.viewError.btnRetry.setOnClickListener {
            projectViewModel.loadProjectDetail(projectId)
        }
    }

    private fun setupViewPager() {
        val adapter = DetailPagerAdapter(this, projectId)
        binding.viewPagerProjectDetail.adapter = adapter

        TabLayoutMediator(binding.tabLayoutProject, binding.viewPagerProjectDetail) { tab, position ->
            tab.text = when (position) {
                0 -> "Overview"
                1 -> "Phases & Tasks"
                2 -> "BOQ"
                3 -> "MB Book"
                4 -> "Site Ops"
                5 -> "Budget & Cost"
                6 -> "Materials"
                7 -> "Finance"
                8 -> "Documents"
                9 -> "Audit"
                10 -> "Team"
                else -> ""
            }
        }.attach()
    }

    private fun observeDetail() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                projectViewModel.detailState.collect { state ->
                    when (state) {
                        is ProjectDetailUiState.Loading -> {
                            binding.viewLoading.root.visibility = View.VISIBLE
                            binding.viewError.root.visibility = View.GONE
                        }
                        is ProjectDetailUiState.Success -> {
                            binding.viewLoading.root.visibility = View.GONE
                            binding.viewError.root.visibility = View.GONE
                            bindHero(state.project)
                        }
                        is ProjectDetailUiState.Error -> {
                            binding.viewLoading.root.visibility = View.GONE
                            binding.viewError.root.visibility = View.VISIBLE
                            binding.viewError.tvErrorMessage.text = state.message
                        }
                    }
                }
            }
        }
    }

    private fun bindHero(p: ProjectDetail) {
        val context = requireContext()
        binding.tvProjectDetailTitle.text = p.name
        binding.tvProjectDetailStatus.text = p.status.replace("_", " ")

        val statusColorRes = when (p.status.uppercase()) {
            "COMPLETED" -> R.color.cms_success
            "IN_PROGRESS" -> R.color.cms_accent
            "PLANNING" -> R.color.cms_info
            "ON_HOLD" -> R.color.cms_warning
            else -> R.color.cms_text_secondary
        }
        val statusColor = ContextCompat.getColor(context, statusColorRes)
        binding.tvProjectDetailStatus.setTextColor(statusColor)

        binding.tvHeroCode.text = p.code
        binding.tvHeroLocation.text = p.location
        binding.tvHeroBudget.text = "Budget: " + formatBudget(p.budgetInr)

        val progressInt = p.progressPct.toInt().coerceIn(0, 100)
        binding.progressHero.progress = progressInt
        binding.tvHeroProgressPct.text = "$progressInt%"
    }

    private fun formatBudget(budget: BigDecimal): String {
        val cr = BigDecimal("10000000")
        val lakh = BigDecimal("100000")
        return when {
            budget >= cr -> "₹" + budget.divide(cr, 2, RoundingMode.HALF_UP).toPlainString() + " Cr"
            budget >= lakh -> "₹" + budget.divide(lakh, 2, RoundingMode.HALF_UP).toPlainString() + " L"
            else -> "₹" + budget.toPlainString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class DetailPagerAdapter(
        fragment: Fragment,
        private val projectId: String
    ) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = 11

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ProjectOverviewTabFragment.newInstance()
                1 -> TaskListFragment.newInstance(projectId)
                2 -> BoqListFragment.newInstance(projectId)
                3 -> MeasurementBookFragment.newInstance(projectId)
                4 -> SiteOpsFragment.newInstance(projectId)
                5 -> BudgetDashboardFragment.newInstance(projectId)
                6 -> InventoryFragment.newInstance(projectId)
                7 -> FinanceFragment.newInstance(projectId)
                8 -> DocumentListFragment.newInstance(projectId)
                9 -> AuditLogFragment.newInstance(projectId)
                10 -> ProjectTeamFragment.newInstance()
                else -> ProjectOverviewTabFragment.newInstance()
            }
        }
    }
}
