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
import com.example.contruction_management_app.databinding.LayoutTabOverviewBinding
import com.example.contruction_management_app.domain.model.Milestone
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class ProjectOverviewTabFragment : Fragment() {

    private var _binding: LayoutTabOverviewBinding? = null
    private val binding get() = _binding!!

    private val projectViewModel: ProjectViewModel by activityViewModels()
    private var projectId: String = ""

    private lateinit var milestoneAdapter: MilestoneAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutTabOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        projectId = arguments?.getString(ARG_PROJECT_ID) ?: ""

        setupMilestonesRecyclerView()
        setupListeners()
        observeData()

        if (projectId.isNotEmpty()) {
            projectViewModel.loadMilestones(projectId)
        }
    }

    private fun setupMilestonesRecyclerView() {
        milestoneAdapter = MilestoneAdapter { milestone ->
            showMilestoneStatusDialog(milestone)
        }
        binding.rvMilestones.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = milestoneAdapter
        }
    }

    private fun setupListeners() {
        binding.btnAddMilestone.setOnClickListener {
            val dialog = AddMilestoneBottomSheetDialog(projectId, projectViewModel) {
                projectViewModel.loadMilestones(projectId)
            }
            dialog.show(parentFragmentManager, AddMilestoneBottomSheetDialog.TAG)
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    projectViewModel.detailState.collect { state ->
                        if (state is ProjectDetailUiState.Success) {
                            val p = state.project
                            if (projectId.isEmpty()) {
                                projectId = p.id
                                projectViewModel.loadMilestones(projectId)
                            }
                            binding.tvOverviewDescription.text = "Project code ${p.code} at ${p.location} with ${p.phasesCount} active phases and ${p.tasksCount} scheduled tasks."
                            binding.tvOverviewClient.text = p.clientName
                            binding.tvOverviewType.text = p.projectType
                            binding.tvOverviewStartDate.text = p.startDate ?: "Not specified"
                            binding.tvOverviewEndDate.text = p.endDate ?: "Not specified"
                        }
                    }
                }

                launch {
                    projectViewModel.milestonesState.collect { state ->
                        when (state) {
                            is MilestonesUiState.Loading -> {
                                // Keep current list while loading
                            }
                            is MilestonesUiState.Success -> {
                                val list = state.milestones
                                milestoneAdapter.submitList(list)
                                updateMilestonesSummary(list)
                            }
                            is MilestonesUiState.Error -> {
                                binding.tvEmptyMilestones.visibility = View.VISIBLE
                                binding.tvEmptyMilestones.text = state.message
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateMilestonesSummary(milestones: List<Milestone>) {
        if (milestones.isEmpty()) {
            binding.tvEmptyMilestones.visibility = View.VISIBLE
            binding.tvMilestoneSummary.text = "No milestones scheduled"
            binding.tvMilestonesPct.text = "0%"
            binding.progressMilestones.progress = 0
            return
        }

        binding.tvEmptyMilestones.visibility = View.GONE
        val reachedCount = milestones.count { it.status.equals("REACHED", ignoreCase = true) || it.status.equals("COMPLETED", ignoreCase = true) }
        val pct = ((reachedCount.toDouble() / milestones.size) * 100).toInt()

        binding.tvMilestoneSummary.text = "$reachedCount of ${milestones.size} Milestones Reached"
        binding.tvMilestonesPct.text = "$pct%"
        binding.progressMilestones.progress = pct
    }

    private fun showMilestoneStatusDialog(milestone: Milestone) {
        val statuses = arrayOf("REACHED", "PENDING", "DELAYED")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Update Milestone Status")
            .setItems(statuses) { _, which ->
                val newStatus = statuses[which]
                projectViewModel.updateMilestoneStatus(
                    milestoneId = milestone.id,
                    projectId = projectId,
                    status = newStatus,
                    onSuccess = {
                        Toast.makeText(requireContext(), "Milestone marked as $newStatus", Toast.LENGTH_SHORT).show()
                    },
                    onError = { err ->
                        Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show()
                    }
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PROJECT_ID = "projectId"

        fun newInstance(projectId: String = ""): ProjectOverviewTabFragment {
            return ProjectOverviewTabFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PROJECT_ID, projectId)
                }
            }
        }
    }
}
