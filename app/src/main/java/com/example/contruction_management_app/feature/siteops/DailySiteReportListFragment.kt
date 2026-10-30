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
    private lateinit var dsrAdapter: DailyReportAdapter
    private lateinit var progressAdapter: WorkProgressAdapter

    private var isDsrMode: Boolean = true

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

        setupRecyclerViews()
        setupListeners()
        observeState()

        siteOpsViewModel.loadDailyReports(projectId)
        siteOpsViewModel.loadWorkProgress(projectId)
    }

    private fun setupRecyclerViews() {
        dsrAdapter = DailyReportAdapter(
            onReportClicked = { report ->
                val dialog = ApproveDsrDialog(report) { action, notes ->
                    siteOpsViewModel.approveDailyReport(report.id, action, notes)
                }
                dialog.show(childFragmentManager, ApproveDsrDialog.TAG)
            },
            onApproveClicked = { report ->
                val dialog = ApproveDsrDialog(report) { action, notes ->
                    siteOpsViewModel.approveDailyReport(report.id, action, notes)
                }
                dialog.show(childFragmentManager, ApproveDsrDialog.TAG)
            }
        )
        binding.rvDailyReports.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDailyReports.adapter = dsrAdapter

        progressAdapter = WorkProgressAdapter(
            onVerifyClicked = { progress ->
                val dialog = VerifyProgressDialog(progress) { action, qty, notes ->
                    siteOpsViewModel.verifyWorkProgress(progress.id, action, qty, notes)
                }
                dialog.show(childFragmentManager, VerifyProgressDialog.TAG)
            }
        )
        binding.rvWorkProgress.layoutManager = LinearLayoutManager(requireContext())
        binding.rvWorkProgress.adapter = progressAdapter
    }

    private fun setupListeners() {
        binding.swipeRefreshDsr.setColorSchemeResources(R.color.cms_accent)
        binding.swipeRefreshDsr.setOnRefreshListener {
            if (isDsrMode) {
                siteOpsViewModel.loadDailyReports(projectId)
            } else {
                siteOpsViewModel.loadWorkProgress(projectId)
            }
        }

        binding.chipGroupDsrMode.setOnCheckedStateChangeListener { _, checkedIds ->
            isDsrMode = checkedIds.contains(R.id.chipDsrDiary)
            updateModeUi()
        }

        binding.btnNewReport.setOnClickListener {
            if (isDsrMode) {
                val dialog = CreateDsrDialog { date, weather, summary, tomorrow, labor ->
                    siteOpsViewModel.createDailyReport(date, weather, summary, tomorrow, labor)
                }
                dialog.show(childFragmentManager, CreateDsrDialog.TAG)
            } else {
                val dialog = RecordProgressDialog { itemName, unit, planned, reported ->
                    siteOpsViewModel.recordWorkProgress(
                        taskId = null,
                        reportId = null,
                        itemName = itemName,
                        unit = unit,
                        plannedQuantity = planned,
                        reportedQuantity = reported
                    )
                }
                dialog.show(childFragmentManager, RecordProgressDialog.TAG)
            }
        }

        binding.viewError.btnRetry.setOnClickListener {
            if (isDsrMode) {
                siteOpsViewModel.loadDailyReports(projectId)
            } else {
                siteOpsViewModel.loadWorkProgress(projectId)
            }
        }
    }

    private fun updateModeUi() {
        if (isDsrMode) {
            binding.tvSectionTitle.text = "Daily Site Reports (DSR)"
            binding.tvSectionSubtitle.text = "Weather, work log, labor roster & review approval"
            binding.btnNewReport.text = "+ Log DSR"
            binding.rvDailyReports.visibility = View.VISIBLE
            binding.rvWorkProgress.visibility = View.GONE
        } else {
            binding.tvSectionTitle.text = "Work Progress (Reported vs Verified)"
            binding.tvSectionSubtitle.text = "Physical quantities tracking with dual-control verification"
            binding.btnNewReport.text = "+ Record Progress"
            binding.rvDailyReports.visibility = View.GONE
            binding.rvWorkProgress.visibility = View.VISIBLE
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    siteOpsViewModel.dsrState.collect { state ->
                        binding.swipeRefreshDsr.isRefreshing = false
                        if (isDsrMode) {
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
                                        dsrAdapter.submitList(state.reports)
                                    }
                                }
                                is DsrUiState.Error -> {
                                    binding.viewLoading.root.visibility = View.GONE
                                    binding.viewEmpty.root.visibility = View.GONE
                                    binding.viewError.root.visibility = View.VISIBLE
                                    binding.viewError.tvErrorMessage.text = state.message
                                }
                            }
                        } else if (state is DsrUiState.Success) {
                            dsrAdapter.submitList(state.reports)
                        }
                    }
                }

                launch {
                    siteOpsViewModel.workProgressState.collect { state ->
                        binding.swipeRefreshDsr.isRefreshing = false
                        if (!isDsrMode) {
                            when (state) {
                                is WorkProgressUiState.Loading -> {
                                    binding.viewLoading.root.visibility = View.VISIBLE
                                    binding.viewError.root.visibility = View.GONE
                                    binding.viewEmpty.root.visibility = View.GONE
                                }
                                is WorkProgressUiState.Success -> {
                                    binding.viewLoading.root.visibility = View.GONE
                                    binding.viewError.root.visibility = View.GONE
                                    if (state.list.isEmpty()) {
                                        binding.viewEmpty.root.visibility = View.VISIBLE
                                        binding.viewEmpty.tvEmptyTitle.text = "No Work Progress recorded"
                                        binding.viewEmpty.tvEmptySubtitle.text = "Log executed physical quantities for engineering verification"
                                    } else {
                                        binding.viewEmpty.root.visibility = View.GONE
                                        progressAdapter.submitList(state.list)
                                    }
                                }
                                is WorkProgressUiState.Error -> {
                                    binding.viewLoading.root.visibility = View.GONE
                                    binding.viewEmpty.root.visibility = View.GONE
                                    binding.viewError.root.visibility = View.VISIBLE
                                    binding.viewError.tvErrorMessage.text = state.message
                                }
                            }
                        } else if (state is WorkProgressUiState.Success) {
                            progressAdapter.submitList(state.list)
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
