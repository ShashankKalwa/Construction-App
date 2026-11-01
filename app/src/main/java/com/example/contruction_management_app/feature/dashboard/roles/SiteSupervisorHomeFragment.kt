package com.example.contruction_management_app.feature.dashboard.roles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.contruction_management_app.core.auth.SessionDataStore
import com.example.contruction_management_app.databinding.FragmentHomeSiteSupervisorBinding
import com.example.contruction_management_app.domain.model.Project
import com.example.contruction_management_app.feature.auth.AuthViewModel
import com.example.contruction_management_app.feature.notifications.NotificationBottomSheetDialog
import com.example.contruction_management_app.feature.project.ProjectViewModel
import com.example.contruction_management_app.feature.shell.WorkspaceSwitcherBottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class SiteSupervisorHomeFragment : Fragment() {

    private var _binding: FragmentHomeSiteSupervisorBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()
    private val projectViewModel: ProjectViewModel by activityViewModels()

    private var projectsList: List<Project> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeSiteSupervisorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeData()
    }

    private fun setupViews() {
        val session = SessionDataStore(requireContext().applicationContext)

        lifecycleScope.launch {
            val user = session.currentUserFlow.firstOrNull()
            binding.tvSupervisorName.text = user?.fullName ?: "Shashank Kumar"
            binding.tvSupervisorOrgName.text = user?.organizationName ?: "Chaitanya InfraTech"
        }

        binding.swipeRefreshSupervisor.setOnRefreshListener {
            projectViewModel.loadProjects()
            binding.swipeRefreshSupervisor.isRefreshing = false
        }

        binding.btnSupervisorNotifications.setOnClickListener {
            NotificationBottomSheetDialog.newInstance()
                .show(parentFragmentManager, NotificationBottomSheetDialog.TAG)
        }

        binding.btnSupervisorLogout.setOnClickListener {
            authViewModel.logout()
        }

        binding.cardSupervisorProjectSwitcher.setOnClickListener {
            WorkspaceSwitcherBottomSheetDialog.newInstance(projectsList) { chosen ->
                binding.tvSupervisorActiveProject.text = "${chosen.code} • ${chosen.name}"
            }.show(parentFragmentManager, WorkspaceSwitcherBottomSheetDialog.TAG)
        }

        binding.btnSubmitDsr.setOnClickListener {
            Snackbar.make(binding.root, "Opening Daily Site Report field entry...", Snackbar.LENGTH_SHORT).show()
        }

        binding.cardSupervisorRequestMaterial.setOnClickListener {
            Snackbar.make(binding.root, "Opening Material Indent Requisition...", Snackbar.LENGTH_SHORT).show()
        }

        binding.cardSupervisorRecordExpense.setOnClickListener {
            Snackbar.make(binding.root, "Opening Site Expense voucher...", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun observeData() {
        val session = SessionDataStore(requireContext().applicationContext)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    projectViewModel.listState.collect { state ->
                        if (state is com.example.contruction_management_app.feature.project.ProjectListUiState.Success) {
                            projectsList = state.projects
                            val activeId = session.activeProjectIdFlow.firstOrNull()
                            val current = state.projects.find { it.id == activeId } ?: state.projects.firstOrNull()
                            if (current != null) {
                                binding.tvSupervisorActiveProject.text = "${current.code} • ${current.name}"
                            }
                        }
                    }
                }
                launch {
                    session.activeProjectIdFlow.collect { activeId ->
                        val current = projectsList.find { it.id == activeId }
                        if (current != null) {
                            binding.tvSupervisorActiveProject.text = "${current.code} • ${current.name}"
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
}
