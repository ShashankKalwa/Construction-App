package com.example.contruction_management_app.feature.audit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.contruction_management_app.R
import com.example.contruction_management_app.databinding.FragmentAuditLogBinding
import com.example.contruction_management_app.domain.model.AuditEntityType
import kotlinx.coroutines.launch

class AuditLogFragment : Fragment() {

    private var _binding: FragmentAuditLogBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuditViewModel by viewModels()
    private var projectId: String = "prj_commercial_tower_b"
    private lateinit var adapter: AuditLogAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuditLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        projectId = arguments?.getString(ARG_PROJECT_ID) ?: "prj_commercial_tower_b"

        setupRecyclerView()
        setupFilters()
        observeState()

        viewModel.loadAuditLogs(projectId)
    }

    private fun setupRecyclerView() {
        adapter = AuditLogAdapter()
        binding.rvAuditLogs.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAuditLogs.adapter = adapter

        binding.swipeRefreshAudit.setOnRefreshListener {
            viewModel.loadAuditLogs(projectId)
        }
    }

    private fun setupFilters() {
        binding.chipGroupAuditFilters.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val typeFilter = when (checkedIds.first()) {
                R.id.chipStockAudit -> AuditEntityType.STOCK
                R.id.chipBillAudit -> AuditEntityType.CONTRACTOR_BILL
                R.id.chipDocAudit -> AuditEntityType.DOCUMENT
                R.id.chipBoqAudit -> AuditEntityType.BOQ
                else -> null
            }
            viewModel.setTypeFilter(typeFilter)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is AuditUiState.Loading -> {
                            binding.swipeRefreshAudit.isRefreshing = true
                        }
                        is AuditUiState.Success -> {
                            binding.swipeRefreshAudit.isRefreshing = false
                            adapter.submitList(state.filteredLogs)

                            state.summary?.let { summary ->
                                binding.tvSummaryTotalEvents.text = summary.totalEvents.toString()
                                binding.tvSummaryApprovals.text = summary.approvalsCount.toString()
                                val stockAndBills = summary.stockIssuesCount + summary.paymentsCount
                                binding.tvSummaryStockPayments.text = stockAndBills.toString()
                            }
                        }
                        is AuditUiState.Error -> {
                            binding.swipeRefreshAudit.isRefreshing = false
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
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
        const val ARG_PROJECT_ID = "arg_project_id"

        fun newInstance(projectId: String): AuditLogFragment {
            return AuditLogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PROJECT_ID, projectId)
                }
            }
        }
    }
}
