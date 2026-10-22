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
import com.example.contruction_management_app.databinding.FragmentDailyReportListBinding
import kotlinx.coroutines.launch

class DailySiteReportListFragment : Fragment() {

    private var _binding: FragmentDailyReportListBinding? = null
    private val binding get() = _binding!!

    private val siteOpsViewModel: SiteOperationsViewModel by activityViewModels()
    private var projectId: String = "prj_commercial_tower_b"
    private lateinit var adapter: DailyReportAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDailyReportListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        projectId = arguments?.getString(ARG_PROJECT_ID) ?: "prj_commercial_tower_b"

        setupRecyclerView()
        setupListeners()
        observeState()

        siteOpsViewModel.loadDailyReports(projectId)
    }

    private fun setupRecyclerView() {
        adapter = DailyReportAdapter()
        binding.rvDailyReports.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDailyReports.adapter = adapter
    }

    private fun setupListeners() {
        binding.swipeRefreshDsr.setColorSchemeResources(R.color.cms_accent)
        binding.swipeRefreshDsr.setOnRefreshListener {
            siteOpsViewModel.loadDailyReports(projectId)
        }

        binding.btnNewReport.setOnClickListener {
            val dialog = CreateDsrDialog { date, weather, summary, tomorrow, labor ->
                siteOpsViewModel.createDailyReport(date, weather, summary, tomorrow, labor)
            }
            dialog.show(childFragmentManager, CreateDsrDialog.TAG)
        }

        binding.viewError.btnRetry.setOnClickListener {
            siteOpsViewModel.loadDailyReports(projectId)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                siteOpsViewModel.dsrState.collect { state ->
                    binding.swipeRefreshDsr.isRefreshing = false
                    when (state) {
                        is DsrUiState.Loading -> {
                            binding.viewLoading.root.visibility = View.VISIBLE
                            binding.viewError.root.visibility = View.GONE
                            binding.viewEmpty.root.visibility = View.GONE
                        }
                        is DsrUiState.Success -> {
                            binding.viewLoading.root.visibility = View.GONE
                            binding.viewError.root.visibility = View.GONE
                            if (state.reports.isEmpty()) {
                                binding.viewEmpty.root.visibility = View.VISIBLE
                                binding.viewEmpty.tvEmptyTitle.text = "No Daily Site Reports logged"
                                binding.viewEmpty.tvEmptySubtitle.text = "Log today's work execution, weather, and labor headcount"
                            } else {
                                binding.viewEmpty.root.visibility = View.GONE
                                adapter.submitList(state.reports)
                            }
                        }
                        is DsrUiState.Error -> {
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

        fun newInstance(projectId: String): DailySiteReportListFragment {
            val fragment = DailySiteReportListFragment()
            val args = Bundle()
            args.putString(ARG_PROJECT_ID, projectId)
            fragment.arguments = args
            return fragment
        }
    }
}
