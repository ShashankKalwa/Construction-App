package com.example.contruction_management_app.feature.project

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.contruction_management_app.R
import com.example.contruction_management_app.databinding.FragmentProjectListBinding
import kotlinx.coroutines.launch

class ProjectListFragment : Fragment() {

    private var _binding: FragmentProjectListBinding? = null
    private val binding get() = _binding!!

    private val projectViewModel: ProjectViewModel by activityViewModels()

    private lateinit var projectAdapter: ProjectAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProjectListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        observeData()
    }

    private fun setupRecyclerView() {
        projectAdapter = ProjectAdapter { project ->
            val bundle = Bundle().apply {
                putString("projectId", project.id)
            }
            findNavController().navigate(R.id.action_mainShell_to_projectDetail, bundle)
        }
        binding.rvProjects.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = projectAdapter
        }
    }

    private fun setupListeners() {
        binding.swipeRefreshProjects.setOnRefreshListener {
            projectViewModel.loadProjects()
        }

        binding.btnCreateProjectTop.setOnClickListener {
            val dialog = CreateProjectBottomSheetDialog(projectViewModel) {
                projectViewModel.loadProjects()
            }
            dialog.show(parentFragmentManager, CreateProjectBottomSheetDialog.TAG)
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                projectViewModel.setSearchQuery(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.chipGroupStatus.setOnCheckedStateChangeListener { _, checkedIds ->
            val filter = when (checkedIds.firstOrNull()) {
                R.id.chipFilterPlanning -> "PLANNING"
                R.id.chipFilterInProgress -> "IN_PROGRESS"
                R.id.chipFilterCompleted -> "COMPLETED"
                else -> "ALL"
            }
            projectViewModel.setStatusFilter(filter)
        }

        binding.viewError.btnRetry.setOnClickListener {
            projectViewModel.loadProjects()
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                projectViewModel.listState.collect { state ->
                    when (state) {
                        is ProjectListUiState.Loading -> {
                            binding.viewLoading.root.visibility = View.VISIBLE
                            binding.viewError.root.visibility = View.GONE
                            binding.viewEmpty.root.visibility = View.GONE
                            binding.swipeRefreshProjects.isRefreshing = true
                        }
                        is ProjectListUiState.Success -> {
                            binding.viewLoading.root.visibility = View.GONE
                            binding.viewError.root.visibility = View.GONE
                            binding.swipeRefreshProjects.isRefreshing = false

                            binding.tvProjectCount.text = "Projects (${state.filteredProjects.size})"
                            projectAdapter.submitList(state.filteredProjects)

                            if (state.filteredProjects.isEmpty()) {
                                binding.viewEmpty.root.visibility = View.VISIBLE
                                binding.viewEmpty.tvEmptyTitle.text = "No Projects Found"
                                binding.viewEmpty.tvEmptySubtitle.text = "Try adjusting your search query or filter"
                            } else {
                                binding.viewEmpty.root.visibility = View.GONE
                            }
                        }
                        is ProjectListUiState.Error -> {
                            binding.viewLoading.root.visibility = View.GONE
                            binding.swipeRefreshProjects.isRefreshing = false
                            binding.viewError.root.visibility = View.VISIBLE
                            binding.viewError.tvErrorMessage.text = state.message
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
