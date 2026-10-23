package com.example.contruction_management_app.feature.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.contruction_management_app.R
import com.example.contruction_management_app.core.util.SkeletonAnimator
import com.example.contruction_management_app.databinding.FragmentHomeDashboardBinding
import com.example.contruction_management_app.domain.model.Project
import com.example.contruction_management_app.feature.auth.AuthUiState
import com.example.contruction_management_app.feature.auth.AuthViewModel
import com.example.contruction_management_app.feature.project.CreateProjectBottomSheetDialog
import com.example.contruction_management_app.feature.project.ProjectViewModel
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

class HomeDashboardFragment : Fragment() {

    private var _binding: FragmentHomeDashboardBinding? = null
    private val binding get() = _binding!!

    private val dashboardViewModel: HomeDashboardViewModel by viewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    private val projectViewModel: ProjectViewModel by activityViewModels()
    private val notificationViewModel: com.example.contruction_management_app.feature.notifications.NotificationViewModel by activityViewModels()

    private var currentProjects: List<Project> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeData()
    }

    private fun setupListeners() {
        binding.swipeRefreshHome.setOnRefreshListener {
            dashboardViewModel.loadDashboard(forceRefresh = true)
        }

        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
        }

        binding.btnNotifications.setOnClickListener {
            com.example.contruction_management_app.feature.notifications.NotificationBottomSheetDialog.newInstance()
                .show(parentFragmentManager, com.example.contruction_management_app.feature.notifications.NotificationBottomSheetDialog.TAG)
        }

        binding.btnSyncQueue.setOnClickListener {
            com.example.contruction_management_app.feature.sync.SyncQueueDialog.newInstance()
                .show(parentFragmentManager, com.example.contruction_management_app.feature.sync.SyncQueueDialog.TAG)
        }

        binding.cardProjectSwitcher.setOnClickListener {
            showProjectSwitcherMenu()
        }

        binding.tileNewProject.setOnClickListener {
            val dialog = CreateProjectBottomSheetDialog(projectViewModel) {
                dashboardViewModel.loadDashboard(forceRefresh = true)
            }
            dialog.show(parentFragmentManager, CreateProjectBottomSheetDialog.TAG)
        }

        binding.chipGroupCategories.setOnCheckedStateChangeListener { _, checkedIds ->
            val cat = when (checkedIds.firstOrNull()) {
                R.id.chipFoundation -> "foundation"
                R.id.chipStructure -> "structure"
                R.id.chipMep -> "mep"
                R.id.chipFinishes -> "finishes"
                else -> "all"
            }
            dashboardViewModel.selectCategory(cat)
        }
    }

    private fun showProjectSwitcherMenu() {
        if (currentProjects.isEmpty()) return
        val popup = PopupMenu(requireContext(), binding.cardProjectSwitcher)
        currentProjects.forEachIndexed { index, project ->
            popup.menu.add(0, index, index, "${project.code} - ${project.name}")
        }
        popup.setOnMenuItemClickListener { item ->
            val project = currentProjects.getOrNull(item.itemId)
            if (project != null) {
                dashboardViewModel.selectProject(project.id)
            }
            true
        }
        popup.show()
    }

    private fun observeData() {
        // Observe Auth User
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.uiState.collect { authState ->
                    if (authState is AuthUiState.Authenticated) {
                        binding.tvUserName.text = authState.user.fullName
                        binding.tvUserRole.text = authState.user.role.name
                        binding.tvOrgName.text = authState.user.organizationName
                    }
                }
            }
        }

        // Observe Notifications Unread Badge
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                notificationViewModel.unreadBadgeCount.collect { count ->
                    if (count > 0) {
                        binding.tvNotificationCount.visibility = View.VISIBLE
                        binding.tvNotificationCount.text = if (count > 9) "9+" else count.toString()
                    } else {
                        binding.tvNotificationCount.visibility = View.GONE
                    }
                }
            }
        }

        // Observe Dashboard State
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                dashboardViewModel.uiState.collect { state ->
                    when (state) {
                        is HomeDashboardUiState.Loading -> {
                            binding.swipeRefreshHome.isRefreshing = false
                            binding.layoutSkeleton.root.visibility = View.VISIBLE
                            binding.layoutContent.visibility = View.GONE
                            SkeletonAnimator.startPulsing(binding.layoutSkeleton.root)
                        }
                        is HomeDashboardUiState.Success -> {
                            SkeletonAnimator.stopPulsing(binding.layoutSkeleton.root)
                            binding.layoutSkeleton.root.visibility = View.GONE
                            binding.layoutContent.visibility = View.VISIBLE
                            binding.swipeRefreshHome.isRefreshing = state.isRefreshing
                            currentProjects = state.projects

                            val active = state.activeProject
                            if (active != null) {
                                binding.tvActiveProjectName.text = active.name
                                bindActiveProjectCard(active)
                            }

                            // Stat 1: Overall Progress
                            binding.cardStatProgress.tvStatLabel.text = "OVERALL PROGRESS"
                            binding.cardStatProgress.tvStatValue.text = "${state.summary.overallProgressPct.toInt()}%"
                            binding.cardStatProgress.tvStatSubtitle.text = "Target: 80% on track"

                            // Stat 2: Total Budget
                            binding.cardStatBudget.tvStatLabel.text = "PORTFOLIO BUDGET"
                            binding.cardStatBudget.tvStatValue.text = state.summary.totalBudgetDisplay
                            binding.cardStatBudget.tvStatSubtitle.text = "${state.summary.activeProjectsCount} active project(s)"

                            // Stat 3: In Progress Tasks
                            binding.cardStatTasks.tvStatLabel.text = "ACTIVE TASKS"
                            binding.cardStatTasks.tvStatValue.text = "${state.summary.inProgressTasksCount}"
                            binding.cardStatTasks.tvStatSubtitle.text = "${state.summary.blockedTasksCount} Blocked"

                            // Stat 4: Completed Tasks
                            binding.cardStatStock.tvStatLabel.text = "COMPLETED"
                            binding.cardStatStock.tvStatValue.text = "${state.summary.completedTasksCount}"
                            binding.cardStatStock.tvStatSubtitle.text = "${state.summary.pendingTasksCount} Pending"
                        }
                        is HomeDashboardUiState.Error -> {
                            binding.swipeRefreshHome.isRefreshing = false
                        }
                    }
                }
            }
        }
    }

    private fun bindActiveProjectCard(project: Project) {
        binding.activeProjectCard.tvProjectName.text = project.name
        binding.activeProjectCard.tvProjectCode.text = project.code
        binding.activeProjectCard.tvProjectLocation.text = project.location
        binding.activeProjectCard.tvProjectStatus.text = project.status.replace("_", " ")
        binding.activeProjectCard.tvProjectBudget.text = "Budget: " + formatBudget(project.budgetInr)

        val progressInt = project.progressPct.toInt().coerceIn(0, 100)
        binding.activeProjectCard.progressProject.progress = progressInt
        binding.activeProjectCard.tvProjectProgressPct.text = "$progressInt%"

        binding.activeProjectCard.cardProject.setOnClickListener {
            val bundle = Bundle().apply {
                putString("projectId", project.id)
            }
            findNavController().navigate(R.id.action_mainShell_to_projectDetail, bundle)
        }
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
        SkeletonAnimator.stopPulsing(binding.layoutSkeleton.root)
        super.onDestroyView()
        _binding = null
    }
}
