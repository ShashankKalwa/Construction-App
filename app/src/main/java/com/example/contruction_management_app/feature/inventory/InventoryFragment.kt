package com.example.contruction_management_app.feature.inventory

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
import com.example.contruction_management_app.databinding.FragmentInventoryBinding
import com.example.contruction_management_app.domain.model.Material
import com.example.contruction_management_app.domain.model.MaterialCategory
import com.example.contruction_management_app.domain.model.MaterialRequest
import com.example.contruction_management_app.domain.model.MaterialRequestStatus
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import java.math.BigDecimal

class InventoryFragment : Fragment() {

    private var _binding: FragmentInventoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InventoryViewModel by viewModels()
    private var projectId: String = "prj_commercial_tower_b"

    private lateinit var materialAdapter: MaterialAdapter
    private lateinit var requestAdapter: MaterialRequestAdapter
    private lateinit var ledgerAdapter: StockLedgerAdapter

    private var currentMaterials = listOf<Material>()
    private var currentRequests = listOf<MaterialRequest>()
    private var activeTabIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        projectId = arguments?.getString(ARG_PROJECT_ID) ?: "prj_commercial_tower_b"

        setupToolbar()
        setupTabs()
        setupCategoryChips()
        setupRequestFilterChips()
        setupRecyclerViews()
        setupListeners()
        observeViewModel()

        viewModel.loadAll(projectId)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupTabs() {
        binding.tabLayoutInventory.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                activeTabIndex = tab?.position ?: 0
                switchTab(activeTabIndex)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun switchTab(position: Int) {
        binding.layoutStockTab.visibility = if (position == 0) View.VISIBLE else View.GONE
        binding.layoutRequestsTab.visibility = if (position == 1) View.VISIBLE else View.GONE
        binding.layoutLedgerTab.visibility = if (position == 2) View.VISIBLE else View.GONE

        // Show FAB on Stock & Request tabs
        binding.fabRequestMaterial.visibility = if (position == 2) View.GONE else View.VISIBLE

        updateEmptyAndLoadingStates()
    }

    private fun setupCategoryChips() {
        binding.chipGroupCategory.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val category = when (checkedIds.first()) {
                R.id.chipCement -> MaterialCategory.CEMENT
                R.id.chipSteel -> MaterialCategory.STEEL
                R.id.chipAggregate -> MaterialCategory.AGGREGATE
                R.id.chipMasonry -> MaterialCategory.MASONRY
                else -> null
            }
            viewModel.filterCategory(category)
        }
    }

    private fun setupRequestFilterChips() {
        binding.chipGroupRequestStatus.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val status = when (checkedIds.first()) {
                R.id.chipReqPending -> MaterialRequestStatus.PENDING
                R.id.chipReqApproved -> MaterialRequestStatus.APPROVED
                R.id.chipReqIssued -> MaterialRequestStatus.ISSUED
                R.id.chipReqRejected -> MaterialRequestStatus.REJECTED
                else -> null
            }
            viewModel.filterRequests(status)
        }
    }

    private fun setupRecyclerViews() {
        // 1. Materials RecyclerView
        materialAdapter = MaterialAdapter(
            onIssueClick = { material ->
                showAdjustDialog(material, defaultIssue = true)
            },
            onAdjustClick = { material ->
                showAdjustDialog(material, defaultIssue = false)
            },
            onReturnClick = { material ->
                showReturnDialog(material)
            }
        )
        binding.rvMaterials.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMaterials.adapter = materialAdapter

        // 2. Material Requests RecyclerView
        requestAdapter = MaterialRequestAdapter(
            onApproveClick = { request ->
                showApproveRejectDialog(request)
            },
            onRejectClick = { request ->
                showApproveRejectDialog(request)
            },
            onIssueClick = { request ->
                showIssueDialog(request)
            }
        )
        binding.rvMaterialRequests.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMaterialRequests.adapter = requestAdapter

        // 3. Stock Ledger RecyclerView
        ledgerAdapter = StockLedgerAdapter()
        binding.rvStockLedger.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStockLedger.adapter = ledgerAdapter
    }

    private fun setupListeners() {
        binding.fabRequestMaterial.setOnClickListener {
            if (currentMaterials.isEmpty()) {
                Toast.makeText(requireContext(), "No materials available to request", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val dialog = RequestMaterialDialog(currentMaterials) { matId, qty, reqDate, desc ->
                viewModel.createMaterialRequest(matId, qty, reqDate, desc)
            }
            dialog.show(childFragmentManager, RequestMaterialDialog.TAG)
        }
    }

    private fun showAdjustDialog(material: Material, defaultIssue: Boolean) {
        val dialog = AdjustStockDialog(material, defaultIssue) { movementType, quantity, ref, notes ->
            viewModel.adjustStock(material.id, movementType, quantity, ref, notes)
        }
        dialog.show(childFragmentManager, AdjustStockDialog.TAG)
    }

    private fun showReturnDialog(material: Material) {
        val dialog = ReturnStockDialog(material) { qty, ref, notes ->
            viewModel.returnStock(material.id, qty, ref, notes)
        }
        dialog.show(childFragmentManager, ReturnStockDialog.TAG)
    }

    private fun showApproveRejectDialog(request: MaterialRequest) {
        val dialog = ApproveMaterialRequestDialog(
            request = request,
            onApprove = { req ->
                viewModel.approveRequest(req.id)
            },
            onReject = { req, reason ->
                viewModel.rejectRequest(req.id, reason)
            }
        )
        dialog.show(childFragmentManager, ApproveMaterialRequestDialog.TAG)
    }

    private fun showIssueDialog(request: MaterialRequest) {
        val material = currentMaterials.find { it.id == request.materialId }
        val dialog = IssueStockDialog(request, material) { req, notes ->
            viewModel.issueStock(req.id, notes)
        }
        dialog.show(childFragmentManager, IssueStockDialog.TAG)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe Action Messages
                launch {
                    viewModel.actionMessage.collect { message ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                }

                // Observe Low Stock Alerts
                launch {
                    viewModel.lowStockState.collect { state ->
                        when (state) {
                            is LowStockUiState.Success -> {
                                if (state.lowStockMaterials.isNotEmpty()) {
                                    binding.cardLowStockAlert.visibility = View.VISIBLE
                                    val alertSummary = state.lowStockMaterials.joinToString("; ") {
                                        "${it.name} (${it.currentStock} ${it.unit} < ${it.minStockThreshold} ${it.unit})"
                                    }
                                    binding.tvLowStockAlertDetails.text = alertSummary
                                } else {
                                    binding.cardLowStockAlert.visibility = View.GONE
                                }
                            }
                            else -> {
                                binding.cardLowStockAlert.visibility = View.GONE
                            }
                        }
                    }
                }

                // Observe Materials (Tab 0)
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is InventoryUiState.Loading -> {
                                if (activeTabIndex == 0) {
                                    binding.loadingState.root.visibility = View.VISIBLE
                                    binding.emptyState.root.visibility = View.GONE
                                }
                            }
                            is InventoryUiState.Success -> {
                                currentMaterials = state.allMaterials
                                materialAdapter.submitList(state.filteredMaterials)
                                updateEmptyAndLoadingStates()
                            }
                            is InventoryUiState.Error -> {
                                updateEmptyAndLoadingStates(errorMessage = state.message)
                            }
                        }
                    }
                }

                // Observe Requests (Tab 1)
                launch {
                    viewModel.requestsState.collect { state ->
                        when (state) {
                            is MaterialRequestsUiState.Loading -> {
                                if (activeTabIndex == 1) {
                                    binding.loadingState.root.visibility = View.VISIBLE
                                    binding.emptyState.root.visibility = View.GONE
                                }
                            }
                            is MaterialRequestsUiState.Success -> {
                                currentRequests = state.allRequests
                                requestAdapter.submitList(state.filteredRequests)
                                updateEmptyAndLoadingStates()
                            }
                            is MaterialRequestsUiState.Error -> {
                                updateEmptyAndLoadingStates(errorMessage = state.message)
                            }
                        }
                    }
                }

                // Observe Ledger (Tab 2)
                launch {
                    viewModel.ledgerState.collect { state ->
                        when (state) {
                            is StockLedgerUiState.Loading -> {
                                if (activeTabIndex == 2) {
                                    binding.loadingState.root.visibility = View.VISIBLE
                                    binding.emptyState.root.visibility = View.GONE
                                }
                            }
                            is StockLedgerUiState.Success -> {
                                ledgerAdapter.submitList(state.movements)
                                updateEmptyAndLoadingStates()
                            }
                            is StockLedgerUiState.Error -> {
                                updateEmptyAndLoadingStates(errorMessage = state.message)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateEmptyAndLoadingStates(errorMessage: String? = null) {
        binding.loadingState.root.visibility = View.GONE

        if (errorMessage != null) {
            binding.emptyState.root.visibility = View.VISIBLE
            binding.emptyState.tvEmptyTitle.text = "Error loading data"
            binding.emptyState.tvEmptySubtitle.text = errorMessage
            return
        }

        when (activeTabIndex) {
            0 -> {
                val isEmpty = materialAdapter.currentList.isEmpty()
                binding.emptyState.root.visibility = if (isEmpty) View.VISIBLE else View.GONE
                if (isEmpty) {
                    binding.emptyState.tvEmptyTitle.text = "No materials found"
                    binding.emptyState.tvEmptySubtitle.text = "Try clearing category filters or add new inventory"
                }
            }
            1 -> {
                val isEmpty = requestAdapter.currentList.isEmpty()
                binding.emptyState.root.visibility = if (isEmpty) View.VISIBLE else View.GONE
                if (isEmpty) {
                    binding.emptyState.tvEmptyTitle.text = "No material requests"
                    binding.emptyState.tvEmptySubtitle.text = "Submit a material requisition from the button below"
                }
            }
            2 -> {
                val isEmpty = ledgerAdapter.currentList.isEmpty()
                binding.emptyState.root.visibility = if (isEmpty) View.VISIBLE else View.GONE
                if (isEmpty) {
                    binding.emptyState.tvEmptyTitle.text = "No stock movements recorded"
                    binding.emptyState.tvEmptySubtitle.text = "Material receipts, issuances, and returns will appear here"
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

        fun newInstance(projectId: String): InventoryFragment {
            val frag = InventoryFragment()
            val args = Bundle()
            args.putString(ARG_PROJECT_ID, projectId)
            frag.arguments = args
            return frag
        }
    }
}
