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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.contruction_management_app.core.auth.SessionDataStore
import com.example.contruction_management_app.databinding.FragmentHomeCompanyAdminBinding
import com.example.contruction_management_app.domain.model.Project
import com.example.contruction_management_app.feature.auth.AuthViewModel
import com.example.contruction_management_app.feature.notifications.NotificationBottomSheetDialog
import com.example.contruction_management_app.feature.project.CreateProjectBottomSheetDialog
import com.example.contruction_management_app.feature.project.ProjectViewModel
import com.example.contruction_management_app.feature.shell.WorkspaceProjectAdapter
import com.example.contruction_management_app.feature.shell.WorkspaceSwitcherBottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class CompanyAdminHomeFragment : Fragment() {

    private var _binding: FragmentHomeCompanyAdminBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()
    private val projectViewModel: ProjectViewModel by activityViewModels()

    private var projectsList: List<Project> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeCompanyAdminBinding.inflate(inflater, container, false)
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
            binding.tvAdminName.text = user?.fullName ?: "Rajesh Sharma"
            binding.tvAdminOrgName.text = user?.organizationName ?: "Chaitanya InfraTech"
        }

        binding.swipeRefreshCompanyAdmin.setOnRefreshListener {
            projectViewModel.loadProjects()
            binding.swipeRefreshCompanyAdmin.isRefreshing = false
        }

        binding.btnAdminNotifications.setOnClickListener {
            NotificationBottomSheetDialog.newInstance()
                .show(parentFragmentManager, NotificationBottomSheetDialog.TAG)
        }

        binding.btnAdminLogout.setOnClickListener {
            authViewModel.logout()
        }

        binding.cardAdminWorkspaceSwitcher.setOnClickListener {
            WorkspaceSwitcherBottomSheetDialog.newInstance(projectsList) { chosen ->
                binding.tvAdminSelectedWorkspace.text = "${chosen.code} • ${chosen.name}"
            }.show(parentFragmentManager, WorkspaceSwitcherBottomSheetDialog.TAG)
        }

        binding.cardAdminCreateProject.setOnClickListener {
            val dialog = CreateProjectBottomSheetDialog(projectViewModel) {
                projectViewModel.loadProjects()
            }
            dialog.show(parentFragmentManager, CreateProjectBottomSheetDialog.TAG)
        }

        binding.cardAdminInviteStaff.setOnClickListener {
            Snackbar.make(binding.root, "Invitation link generated. Send invitation via staff directory.", Snackbar.LENGTH_SHORT).show()
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
                            binding.tvAdminActiveSites.text = state.projects.count { it.status.equals("Active", ignoreCase = true) }.toString()

                            val activeId = session.activeProjectIdFlow.firstOrNull()
                            val activeProject = state.projects.find { it.id == activeId } ?: state.projects.firstOrNull()
                            if (activeProject != null) {
                                binding.tvAdminSelectedWorkspace.text = "${activeProject.code} • ${activeProject.name}"
                            }

                            val adapter = WorkspaceProjectAdapter(activeProject?.id) { selected ->
                                lifecycleScope.launch {
                                    session.setActiveProjectId(selected.id)
                                    binding.tvAdminSelectedWorkspace.text = "${selected.code} • ${selected.name}"
                                }
                            }
                            binding.rvAdminProjects.layoutManager = LinearLayoutManager(requireContext())
                            binding.rvAdminProjects.adapter = adapter
                            adapter.submitList(state.projects)
                        }
                    }
                }
                launch {
                    session.activeProjectIdFlow.collect { activeId ->
                        val current = projectsList.find { it.id == activeId }
                        if (current != null) {
                            binding.tvAdminSelectedWorkspace.text = "${current.code} • ${current.name}"
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
