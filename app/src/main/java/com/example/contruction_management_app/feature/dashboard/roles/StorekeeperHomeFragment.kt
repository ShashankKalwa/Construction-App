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
import com.example.contruction_management_app.databinding.FragmentHomeStorekeeperBinding
import com.example.contruction_management_app.domain.model.Project
import com.example.contruction_management_app.feature.auth.AuthViewModel
import com.example.contruction_management_app.feature.notifications.NotificationBottomSheetDialog
import com.example.contruction_management_app.feature.project.ProjectViewModel
import com.example.contruction_management_app.feature.shell.WorkspaceSwitcherBottomSheetDialog
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class StorekeeperHomeFragment : Fragment() {

    private var _binding: FragmentHomeStorekeeperBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()
    private val projectViewModel: ProjectViewModel by activityViewModels()

    private var projectsList: List<Project> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeStorekeeperBinding.inflate(inflater, container, false)
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
            binding.tvStorekeeperName.text = user?.fullName ?: "Ramesh Yadav"
            binding.tvStorekeeperOrgName.text = user?.organizationName ?: "Apex Infra Ltd"
        }

        binding.swipeRefreshStorekeeper.setOnRefreshListener {
            projectViewModel.loadProjects()
            binding.swipeRefreshStorekeeper.isRefreshing = false
        }

        binding.btnStorekeeperNotifications.setOnClickListener {
            NotificationBottomSheetDialog.newInstance()
                .show(parentFragmentManager, NotificationBottomSheetDialog.TAG)
        }

        binding.btnStorekeeperLogout.setOnClickListener {
            authViewModel.logout()
        }

        binding.cardStorekeeperProjectSwitcher.setOnClickListener {
            WorkspaceSwitcherBottomSheetDialog.newInstance(projectsList) { chosen ->
                binding.tvStorekeeperActiveProject.text = "${chosen.code} • ${chosen.name}"
            }.show(parentFragmentManager, WorkspaceSwitcherBottomSheetDialog.TAG)
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
                                binding.tvStorekeeperActiveProject.text = "${current.code} • ${current.name}"
                            }
                        }
                    }
                }
                launch {
                    session.activeProjectIdFlow.collect { activeId ->
                        val current = projectsList.find { it.id == activeId }
                        if (current != null) {
                            binding.tvStorekeeperActiveProject.text = "${current.code} • ${current.name}"
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
