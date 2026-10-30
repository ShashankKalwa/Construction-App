package com.example.contruction_management_app.feature.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.contruction_management_app.R
import com.example.contruction_management_app.core.auth.SessionDataStore
import com.example.contruction_management_app.databinding.FragmentProjectDetailBinding
import com.example.contruction_management_app.domain.model.ProjectDetail
import com.example.contruction_management_app.domain.model.Role
import com.example.contruction_management_app.feature.audit.AuditLogFragment
import com.example.contruction_management_app.feature.boq.BoqListFragment
import com.example.contruction_management_app.feature.documents.DocumentListFragment
import com.example.contruction_management_app.feature.finance.BudgetDashboardFragment
import com.example.contruction_management_app.feature.finance.FinanceFragment
import com.example.contruction_management_app.feature.inventory.InventoryFragment
import com.example.contruction_management_app.feature.measurement.MeasurementBookFragment
import com.example.contruction_management_app.feature.siteops.SiteOpsFragment
import com.example.contruction_management_app.feature.task.TaskListFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

class ProjectDetailFragment : Fragment() {

    private var _binding: FragmentProjectDetailBinding? = null
    private val binding get() = _binding!!

    private val projectViewModel: ProjectViewModel by activityViewModels()
    private var projectId: String = "prj_commercial_tower_b"

    private var tabMediator: TabLayoutMediator? = null
    private var activeTabs: List<TabDefinition> = emptyList()

    data class TabDefinition(
        val title: String,
        val requiredPermission: String?,
        val fragmentFactory: (String) -> Fragment
    )

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
        setupDynamicTabs()
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

        binding.tvProjectDetailStatus.setOnClickListener {
            showProjectStatusLifecycleDialog()
        }
    }

    private fun setupDynamicTabs() {
        val session = SessionDataStore(requireContext().applicationContext)

        val masterTabs = listOf(
            TabDefinition("Overview", null) { id -> ProjectOverviewTabFragment.newInstance(id) },
            TabDefinition("Phases & Tasks", "tasks:view") { id -> TaskListFragment.newInstance(id) },
            TabDefinition("BOQ", "boq:view") { id -> BoqListFragment.newInstance(id) },
            TabDefinition("MB Book", "measurements:take") { id -> MeasurementBookFragment.newInstance(id) },
            TabDefinition("Site Ops", "reports:daily_site") { id -> SiteOpsFragment.newInstance(id) },
            TabDefinition("Budget & Cost", "finance:view") { id -> BudgetDashboardFragment.newInstance(id) },
            TabDefinition("Materials", "inventory:view") { id -> InventoryFragment.newInstance(id) },
            TabDefinition("Finance", "finance:view") { id -> FinanceFragment.newInstance(id) },
            TabDefinition("Documents", "projects:view") { id -> DocumentListFragment.newInstance(id) },
            TabDefinition("Audit", "audit:view") { id -> AuditLogFragment.newInstance(id) },
            TabDefinition("Team", "projects:view") { id -> ProjectTeamFragment.newInstance(id) }
        )

        viewLifecycleOwner.lifecycleScope.launch {
            val user = session.currentUserFlow.firstOrNull()
            val permissions = session.effectivePermissionsFlow.firstOrNull() ?: emptySet()
            val role = user?.role

            val isElevatedRole = role == Role.SUPER_ADMIN || role == Role.COMPANY_ADMIN || role == Role.PROJECT_MANAGER

            activeTabs = masterTabs.filter { tab ->
                when {
                    tab.requiredPermission == null -> true
                    isElevatedRole -> true
                    permissions.contains(tab.requiredPermission) -> true
                    permissions.isEmpty() -> {
                        // Fallback role heuristics if permissions cache is not yet synced
                        when (tab.title) {
                            "Phases & Tasks" -> role == Role.CIVIL_ENGINEER || role == Role.SITE_SUPERVISOR
                            "BOQ", "MB Book" -> role == Role.CIVIL_ENGINEER
                            "Site Ops" -> role == Role.SITE_SUPERVISOR || role == Role.CIVIL_ENGINEER
                            "Materials" -> role == Role.STOREKEEPER
                            "Budget & Cost", "Finance" -> role == Role.ACCOUNTANT
                            "Team", "Documents" -> true
                            else -> false
                        }
                    }
                    else -> false
                }
            }

            if (activeTabs.isEmpty()) {
                activeTabs = listOf(masterTabs[0])
            }

            val adapter = DynamicDetailPagerAdapter(this@ProjectDetailFragment, projectId, activeTabs)
            binding.viewPagerProjectDetail.adapter = adapter

            tabMediator?.detach()
            tabMediator = TabLayoutMediator(binding.tabLayoutProject, binding.viewPagerProjectDetail) { tab, position ->
                tab.text = activeTabs.getOrNull(position)?.title ?: ""
            }
            tabMediator?.attach()
        }
    }

    private fun showProjectStatusLifecycleDialog() {
        val statuses = arrayOf("DRAFT", "PLANNING", "ACTIVE", "ON_HOLD", "COMPLETED", "ARCHIVED")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Update Project Lifecycle Status")
            .setItems(statuses) { _, which ->
                val selectedStatus = statuses[which]
                projectViewModel.updateProjectStatus(
                    projectId = projectId,
                    status = selectedStatus,
                    onSuccess = {
                        Toast.makeText(requireContext(), "Project status updated to $selectedStatus", Toast.LENGTH_SHORT).show()
                    },
                    onError = { err ->
                        Toast.makeText(requireContext(), err, Toast.LENGTH_LONG).show()
                    }
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
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
            "ACTIVE", "IN_PROGRESS" -> R.color.cms_accent
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
        tabMediator?.detach()
        tabMediator = null
        super.onDestroyView()
        _binding = null
    }

    private class DynamicDetailPagerAdapter(
        fragment: Fragment,
        private val projectId: String,
        private val tabs: List<TabDefinition>
    ) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = tabs.size

        override fun createFragment(position: Int): Fragment {
            return tabs.getOrNull(position)?.fragmentFactory?.invoke(projectId)
                ?: ProjectOverviewTabFragment.newInstance(projectId)
        }
    }
}
