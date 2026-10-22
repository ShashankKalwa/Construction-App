package com.example.contruction_management_app.feature.siteops

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
import com.example.contruction_management_app.databinding.FragmentSiteIssueListBinding
import com.example.contruction_management_app.domain.model.IssueStatus
import kotlinx.coroutines.launch

class SiteIssueListFragment : Fragment() {

    private var _binding: FragmentSiteIssueListBinding? = null
    private val binding get() = _binding!!

    private val siteOpsViewModel: SiteOperationsViewModel by activityViewModels()
    private var projectId: String = "prj_commercial_tower_b"
    private lateinit var adapter: SiteIssueAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSiteIssueListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        projectId = arguments?.getString(ARG_PROJECT_ID) ?: "prj_commercial_tower_b"

        setupRecyclerView()
        setupListeners()
        observeState()

        siteOpsViewModel.loadIssues(projectId)
    }

    private fun setupRecyclerView() {
        adapter = SiteIssueAdapter { issue ->
            val dialog = UpdateIssueStatusDialog(issue) { newStatus, notes ->
                siteOpsViewModel.updateIssueStatus(issue.id, newStatus, notes)
            }
            dialog.show(childFragmentManager, UpdateIssueStatusDialog.TAG)
        }
        binding.rvSiteIssues.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSiteIssues.adapter = adapter
    }

    private fun setupListeners() {
        binding.swipeRefreshIssues.setColorSchemeResources(R.color.cms_accent)
        binding.swipeRefreshIssues.setOnRefreshListener {
            val status = getSelectedFilterStatus()
            siteOpsViewModel.loadIssues(projectId, status)
        }

        binding.btnNewIssue.setOnClickListener {
            val dialog = CreateSiteIssueDialog { title, desc, sev, assigned ->
                siteOpsViewModel.createIssue(title, desc, sev, assigned)
            }
            dialog.show(childFragmentManager, CreateSiteIssueDialog.TAG)
        }

        binding.chipGroupIssueStatus.setOnCheckedStateChangeListener { _, checkedIds ->
            val status = getSelectedFilterStatus()
            siteOpsViewModel.loadIssues(projectId, status)
        }

        binding.viewError.btnRetry.setOnClickListener {
            val status = getSelectedFilterStatus()
            siteOpsViewModel.loadIssues(projectId, status)
        }
    }

    private fun getSelectedFilterStatus(): IssueStatus? {
        return when (binding.chipGroupIssueStatus.checkedChipId) {
            R.id.chipFilterOpen -> IssueStatus.OPEN
            R.id.chipFilterInProgress -> IssueStatus.IN_PROGRESS
            R.id.chipFilterResolved -> IssueStatus.RESOLVED
            else -> null
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                siteOpsViewModel.issuesState.collect { state ->
                    binding.swipeRefreshIssues.isRefreshing = false
                    when (state) {
                        is IssuesUiState.Loading -> {
                            binding.viewLoading.root.visibility = View.VISIBLE
                            binding.viewError.root.visibility = View.GONE
                            binding.viewEmpty.root.visibility = View.GONE
                        }
                        is IssuesUiState.Success -> {
                            binding.viewLoading.root.visibility = View.GONE
                            binding.viewError.root.visibility = View.GONE
                            if (state.issues.isEmpty()) {
                                binding.viewEmpty.root.visibility = View.VISIBLE
                                binding.viewEmpty.tvEmptyTitle.text = "No site issues recorded"
                                binding.viewEmpty.tvEmptySubtitle.text = "Zero snags or defects logged for this project filter"
                            } else {
                                binding.viewEmpty.root.visibility = View.GONE
                                adapter.submitList(state.issues)
                            }
                        }
                        is IssuesUiState.Error -> {
                            binding.viewLoading.root.visibility = View.GONE
                            binding.viewEmpty.root.visibility = View.GONE
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

    companion object {
        private const val ARG_PROJECT_ID = "projectId"

        fun newInstance(projectId: String): SiteIssueListFragment {
            val fragment = SiteIssueListFragment()
            val args = Bundle()
            args.putString(ARG_PROJECT_ID, projectId)
            fragment.arguments = args
            return fragment
        }
    }
}
