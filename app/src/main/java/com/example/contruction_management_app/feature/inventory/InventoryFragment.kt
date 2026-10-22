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
import kotlinx.coroutines.launch

class InventoryFragment : Fragment() {

    private var _binding: FragmentInventoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InventoryViewModel by viewModels()
    private var projectId: String = "prj_commercial_tower_b"
    private lateinit var adapter: MaterialAdapter
    private var currentMaterials = listOf<Material>()

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
        setupCategoryChips()
        setupRecyclerView()
        setupListeners()
        observeState()

        viewModel.loadMaterials(projectId)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
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

    private fun setupRecyclerView() {
        adapter = MaterialAdapter(
            onIssueClick = { material ->
                showAdjustDialog(material, defaultIssue = true)
            },
            onAdjustClick = { material ->
                showAdjustDialog(material, defaultIssue = false)
            }
        )
        binding.rvMaterials.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMaterials.adapter = adapter
    }

    private fun showAdjustDialog(material: Material, defaultIssue: Boolean) {
        val dialog = AdjustStockDialog(material, defaultIssue) { movementType, quantity, ref, notes ->
            viewModel.adjustStock(material.id, movementType, quantity, ref, notes)
            val actionName = if (defaultIssue) "Issued" else "Adjusted"
            Toast.makeText(requireContext(), "$actionName $quantity ${material.unit} of ${material.name}", Toast.LENGTH_SHORT).show()
        }
        dialog.show(childFragmentManager, AdjustStockDialog.TAG)
    }

    private fun setupListeners() {
        binding.fabRequestMaterial.setOnClickListener {
            if (currentMaterials.isEmpty()) {
                Toast.makeText(requireContext(), "No materials available to request", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val dialog = RequestMaterialDialog(currentMaterials) { matId, qty, reqDate, desc ->
                viewModel.createMaterialRequest(matId, qty, reqDate, desc)
                Toast.makeText(requireContext(), "Material request submitted", Toast.LENGTH_SHORT).show()
            }
            dialog.show(childFragmentManager, RequestMaterialDialog.TAG)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is InventoryUiState.Loading -> {
                            binding.loadingState.root.visibility = View.VISIBLE
                            binding.emptyState.root.visibility = View.GONE
                        }
                        is InventoryUiState.Success -> {
                            binding.loadingState.root.visibility = View.GONE
                            currentMaterials = state.allMaterials
                            if (state.filteredMaterials.isEmpty()) {
                                binding.emptyState.root.visibility = View.VISIBLE
                                binding.emptyState.tvEmptyTitle.text = "No materials found"
                                binding.emptyState.tvEmptySubtitle.text = "Add materials or change category filter"
                            } else {
                                binding.emptyState.root.visibility = View.GONE
                                adapter.submitList(state.filteredMaterials)
                            }
                        }
                        is InventoryUiState.Error -> {
                            binding.loadingState.root.visibility = View.GONE
                            binding.emptyState.root.visibility = View.VISIBLE
                            binding.emptyState.tvEmptyTitle.text = "Error loading inventory"
                            binding.emptyState.tvEmptySubtitle.text = state.message
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

        fun newInstance(projectId: String): InventoryFragment {
            val frag = InventoryFragment()
            val args = Bundle()
            args.putString(ARG_PROJECT_ID, projectId)
            frag.arguments = args
            return frag
        }
    }
}
