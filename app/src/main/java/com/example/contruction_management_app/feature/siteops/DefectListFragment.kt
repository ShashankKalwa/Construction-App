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
import com.example.contruction_management_app.databinding.FragmentDefectListBinding
import com.example.contruction_management_app.domain.model.DefectStatus
import kotlinx.coroutines.launch

class DefectListFragment : Fragment() {

    private var _binding: FragmentDefectListBinding? = null
    private val binding get() = _binding!!

    private val siteOpsViewModel: SiteOperationsViewModel by activityViewModels()
    private var projectId: String = "prj_commercial_tower_b"
    private lateinit var adapter: DefectAdapter

    private var filterStatus: DefectStatus? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDefectListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        projectId = arguments?.getString(ARG_PROJECT_ID) ?: "prj_commercial_tower_b"

        setupRecyclerView()
        setupListeners()
        observeState()

        siteOpsViewModel.loadDefects(projectId)
    }

    private fun setupRecyclerView() {
        adapter = DefectAdapter(
            onResolveClicked = { defect ->
                siteOpsViewModel.updateDefectStatus(defect.id, DefectStatus.RESOLVED)
            },
            onReinspectClicked = { defect ->
                val dialog = ReinspectDefectDialog(defect) { action, notes ->
                    siteOpsViewModel.reinspectDefect(defect.id, action, notes)
                }
                dialog.show(childFragmentManager, ReinspectDefectDialog.TAG)
            }
        )
        binding.rvDefects.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDefects.adapter = adapter
    }

    private fun setupListeners() {
        binding.swipeRefreshDefects.setColorSchemeResources(R.color.cms_accent)
        binding.swipeRefreshDefects.setOnRefreshListener {
            siteOpsViewModel.loadDefects(projectId, filterStatus)
        }

        binding.chipGroupDefectFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            filterStatus = when {
                checkedIds.contains(R.id.chipFilterOpen) -> DefectStatus.OPEN
                checkedIds.contains(R.id.chipFilterResolved) -> DefectStatus.RESOLVED
                checkedIds.contains(R.id.chipFilterClosed) -> DefectStatus.CLOSED
                else -> null
            }
            siteOpsViewModel.loadDefects(projectId, filterStatus)
        }

        binding.btnNewDefect.setOnClickListener {
            val dialog = CreateDefectDialog { title, desc, loc, sev, assigned, deadline ->
                siteOpsViewModel.createDefect(
                    inspectionId = null,
                    title = title,
                    description = desc,
                    location = loc,
                    severity = sev,
                    assignedTo = assigned,
                    deadline = deadline
                )
            }
            dialog.show(childFragmentManager, CreateDefectDialog.TAG)
        }

        binding.viewError.btnRetry.setOnClickListener {
            siteOpsViewModel.loadDefects(projectId, filterStatus)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                siteOpsViewModel.defectsState.collect { state ->
                    binding.swipeRefreshDefects.isRefreshing = false
                    when (state) {
                        is DefectsUiState.Loading -> {
                            binding.viewLoading.root.visibility = View.VISIBLE
                            binding.viewError.root.visibility = View.GONE
                            binding.viewEmpty.root.visibility = View.GONE
                        }
                        is DefectsUiState.Success -> {
                            binding.viewLoading.root.visibility = View.GONE
                            binding.viewError.root.visibility = View.GONE
                            if (state.defects.isEmpty()) {
                                binding.viewEmpty.root.visibility = View.VISIBLE
                                binding.viewEmpty.tvEmptyTitle.text = "No snags or defects logged"
                                binding.viewEmpty.tvEmptySubtitle.text = "Record site snags for closed-loop rectification and verification"
                            } else {
                                binding.viewEmpty.root.visibility = View.GONE
                                adapter.submitList(state.defects)
                            }
                        }
                        is DefectsUiState.Error -> {
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

        fun newInstance(projectId: String): DefectListFragment {
            val fragment = DefectListFragment()
            val args = Bundle()
            args.putString(ARG_PROJECT_ID, projectId)
            fragment.arguments = args
            return fragment
        }
    }
}
