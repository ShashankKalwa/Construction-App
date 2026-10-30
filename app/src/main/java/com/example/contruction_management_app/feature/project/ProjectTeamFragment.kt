package com.example.contruction_management_app.feature.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.contruction_management_app.databinding.LayoutTabTeamBinding
import com.example.contruction_management_app.domain.model.ProjectMember
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class ProjectTeamFragment : Fragment() {

    private var _binding: LayoutTabTeamBinding? = null
    private val binding get() = _binding!!

    private val projectViewModel: ProjectViewModel by activityViewModels()
    private var projectId: String = ""

    private lateinit var teamAdapter: TeamMemberAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutTabTeamBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        projectId = arguments?.getString(ARG_PROJECT_ID) ?: ""

        setupRecyclerView()
        setupListeners()
        observeData()

        if (projectId.isNotEmpty()) {
            projectViewModel.loadMembers(projectId)
        }
    }

    private fun setupRecyclerView() {
        teamAdapter = TeamMemberAdapter { member ->
            showMemberOptionsDialog(member)
        }
        binding.rvTeamMembers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = teamAdapter
        }
    }

    private fun setupListeners() {
        binding.btnAddMember.setOnClickListener {
            val dialog = AddMemberBottomSheetDialog(projectId, projectViewModel) {
                projectViewModel.loadMembers(projectId)
            }
            dialog.show(parentFragmentManager, AddMemberBottomSheetDialog.TAG)
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    projectViewModel.detailState.collect { state ->
                        if (state is ProjectDetailUiState.Success) {
                            if (projectId.isEmpty()) {
                                projectId = state.project.id
                                projectViewModel.loadMembers(projectId)
                            }
                            if (state.project.members.isNotEmpty() && teamAdapter.currentList.isEmpty()) {
                                teamAdapter.submitList(state.project.members)
                                binding.tvEmptyTeam.visibility = View.GONE
                            }
                        }
                    }
                }

                launch {
                    projectViewModel.membersState.collect { state ->
                        when (state) {
                            is ProjectMembersUiState.Loading -> {
                                // Keep current list
                            }
                            is ProjectMembersUiState.Success -> {
                                val list = state.members
                                teamAdapter.submitList(list)
                                binding.tvEmptyTeam.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                            }
                            is ProjectMembersUiState.Error -> {
                                if (teamAdapter.currentList.isEmpty()) {
                                    binding.tvEmptyTeam.visibility = View.VISIBLE
                                    binding.tvEmptyTeam.text = state.message
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showMemberOptionsDialog(member: ProjectMember) {
        val isActive = member.status.equals("ACTIVE", ignoreCase = true)
        val actionText = if (isActive) "Deactivate Member" else "Member already inactive"

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(member.user.fullName)
            .setMessage("Role: ${(member.assignedRole ?: "MEMBER").replace("_", " ")}\nStatus: ${member.status}")
            .setPositiveButton(actionText) { _, _ ->
                if (isActive) {
                    projectViewModel.removeMember(
                        projectId = projectId,
                        userId = member.user.id,
                        onSuccess = {
                            Toast.makeText(requireContext(), "${member.user.fullName} deactivated", Toast.LENGTH_SHORT).show()
                        },
                        onError = { err ->
                            Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PROJECT_ID = "projectId"

        fun newInstance(projectId: String = ""): ProjectTeamFragment {
            return ProjectTeamFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PROJECT_ID, projectId)
                }
            }
        }
    }
}
