package com.example.contruction_management_app.feature.task

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
import com.example.contruction_management_app.R
import com.example.contruction_management_app.core.auth.SessionDataStore
import com.example.contruction_management_app.databinding.FragmentTaskListBinding
import com.example.contruction_management_app.domain.model.Phase
import com.example.contruction_management_app.domain.model.Task
import com.example.contruction_management_app.domain.model.TaskStatus
import kotlinx.coroutines.launch

class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private val taskViewModel: TaskViewModel by activityViewModels()

    private lateinit var taskAdapter: TaskAdapter
    private var currentProjectId: String = "prj_commercial_tower_b"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val passedId = arguments?.getString("projectId")
        if (!passedId.isNullOrBlank()) {
            currentProjectId = passedId
        }

        setupRecyclerView()
        setupListeners()
        observeData()

        taskViewModel.loadPhases(currentProjectId)
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter { task ->
            val dialog = TaskUpdateBottomSheetDialog(task, taskViewModel) {
                taskViewModel.loadPhases(currentProjectId)
            }
            dialog.show(parentFragmentManager, TaskUpdateBottomSheetDialog.TAG)
        }
        binding.rvPhasesAndTasks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
        }
    }

    private fun setupListeners() {
        binding.swipeRefreshTasks.setOnRefreshListener {
            taskViewModel.loadPhases(currentProjectId)
        }

        binding.chipGroupTaskStatus.setOnCheckedStateChangeListener { _, checkedIds ->
            val status = when (checkedIds.firstOrNull()) {
                R.id.chipTaskInProgress -> TaskStatus.IN_PROGRESS
                R.id.chipTaskBlocked -> TaskStatus.BLOCKED
                R.id.chipTaskCompleted -> TaskStatus.COMPLETED
                else -> null
            }
            taskViewModel.filterByStatus(status)
        }

        binding.viewError.btnRetry.setOnClickListener {
            taskViewModel.loadPhases(currentProjectId)
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                taskViewModel.uiState.collect { state ->
                    when (state) {
                        is PhasesUiState.Loading -> {
                            binding.viewLoading.root.visibility = View.VISIBLE
                            binding.viewError.root.visibility = View.GONE
                            binding.viewEmpty.root.visibility = View.GONE
                            binding.swipeRefreshTasks.isRefreshing = true
                        }
                        is PhasesUiState.Success -> {
                            binding.viewLoading.root.visibility = View.GONE
                            binding.viewError.root.visibility = View.GONE
                            binding.swipeRefreshTasks.isRefreshing = false

                            val items = flattenPhases(state.filteredPhases)
                            taskAdapter.submitList(items)

                            if (items.isEmpty()) {
                                binding.viewEmpty.root.visibility = View.VISIBLE
                                binding.viewEmpty.tvEmptyTitle.text = "No Tasks Found"
                                binding.viewEmpty.tvEmptySubtitle.text = "There are no tasks matching the selected filter"
                            } else {
                                binding.viewEmpty.root.visibility = View.GONE
                            }
                        }
                        is PhasesUiState.Error -> {
                            binding.viewLoading.root.visibility = View.GONE
                            binding.swipeRefreshTasks.isRefreshing = false
                            binding.viewError.root.visibility = View.VISIBLE
                            binding.viewError.tvErrorMessage.text = state.message
                        }
                    }
                }
            }
        }
    }

    private fun flattenPhases(phases: List<Phase>): List<TaskListItem> {
        val list = mutableListOf<TaskListItem>()
        for (phase in phases) {
            list.add(TaskListItem.PhaseHeader(phase))
            for (task in phase.tasks) {
                list.add(TaskListItem.TaskRow(task))
            }
        }
        return list
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(projectId: String): TaskListFragment {
            return TaskListFragment().apply {
                arguments = Bundle().apply {
                    putString("projectId", projectId)
                }
            }
        }
    }
}
