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
import com.example.contruction_management_app.databinding.FragmentInspectionListBinding
import com.example.contruction_management_app.domain.model.InspectionType
import kotlinx.coroutines.launch

class InspectionListFragment : Fragment() {

    private var _binding: FragmentInspectionListBinding? = null
    private val binding get() = _binding!!

    private val siteOpsViewModel: SiteOperationsViewModel by activityViewModels()
    private var projectId: String = "prj_commercial_tower_b"
    private lateinit var adapter: InspectionAdapter

    private var filterType: InspectionType? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInspectionListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        projectId = arguments?.getString(ARG_PROJECT_ID) ?: "prj_commercial_tower_b"

        setupRecyclerView()
        setupListeners()
        observeState()

        siteOpsViewModel.loadInspections(projectId)
    }

    private fun setupRecyclerView() {
        adapter = InspectionAdapter()
        binding.rvInspections.layoutManager = LinearLayoutManager(requireContext())
        binding.rvInspections.adapter = adapter
    }

    private fun setupListeners() {
        binding.swipeRefreshInspections.setColorSchemeResources(R.color.cms_accent)
        binding.swipeRefreshInspections.setOnRefreshListener {
            siteOpsViewModel.loadInspections(projectId)
        }

        binding.chipGroupInspectionFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            filterType = when {
                checkedIds.contains(R.id.chipFilterQuality) -> InspectionType.QUALITY
                checkedIds.contains(R.id.chipFilterSafety) -> InspectionType.SAFETY
                checkedIds.contains(R.id.chipFilterStructural) -> InspectionType.STRUCTURAL
                else -> null
            }
            applyFilter()
        }

        binding.btnNewInspection.setOnClickListener {
            val dialog = CreateInspectionDialog { title, type, status, checklistJson, location, notes ->
                siteOpsViewModel.createInspection(
                    title = title,
                    inspectionType = type,
                    status = status,
                    checklistData = checklistJson,
                    inspectionDate = null,
                    location = location,
                    notes = notes
                )
            }
            dialog.show(childFragmentManager, CreateInspectionDialog.TAG)
        }

        binding.viewError.btnRetry.setOnClickListener {
            siteOpsViewModel.loadInspections(projectId)
        }
    }

    private fun applyFilter() {
        val state = siteOpsViewModel.inspectionsState.value
        if (state is InspectionsUiState.Success) {
            val filtered = if (filterType != null) {
                state.inspections.filter { it.inspectionType == filterType }
            } else {
                state.inspections
            }
            adapter.submitList(filtered)
            binding.viewEmpty.root.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                siteOpsViewModel.inspectionsState.collect { state ->
                    binding.swipeRefreshInspections.isRefreshing = false
                    when (state) {
                        is InspectionsUiState.Loading -> {
                            binding.viewLoading.root.visibility = View.VISIBLE
                            binding.viewError.root.visibility = View.GONE
                            binding.viewEmpty.root.visibility = View.GONE
                        }
                        is InspectionsUiState.Success -> {
                            binding.viewLoading.root.visibility = View.GONE
                            binding.viewError.root.visibility = View.GONE
                            val filtered = if (filterType != null) {
                                state.inspections.filter { it.inspectionType == filterType }
                            } else {
                                state.inspections
                            }
                            if (filtered.isEmpty()) {
                                binding.viewEmpty.root.visibility = View.VISIBLE
                                binding.viewEmpty.tvEmptyTitle.text = "No site inspections logged"
                                binding.viewEmpty.tvEmptySubtitle.text = "Perform digital quality or safety checklist inspections"
                            } else {
                                binding.viewEmpty.root.visibility = View.GONE
                                adapter.submitList(filtered)
                            }
                        }
                        is InspectionsUiState.Error -> {
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

        fun newInstance(projectId: String): InspectionListFragment {
            val fragment = InspectionListFragment()
            val args = Bundle()
            args.putString(ARG_PROJECT_ID, projectId)
            fragment.arguments = args
            return fragment
        }
    }
}
