package com.example.contruction_management_app.feature.boq

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.contruction_management_app.R
import com.example.contruction_management_app.databinding.FragmentBoqDetailBinding
import com.example.contruction_management_app.domain.model.Boq
import com.example.contruction_management_app.domain.model.BoqStatus
import kotlinx.coroutines.launch

class BoqDetailFragment : Fragment() {

    private var _binding: FragmentBoqDetailBinding? = null
    private val binding get() = _binding!!

    private val boqViewModel: BoqViewModel by activityViewModels()
    private var boqId: String = ""
    private lateinit var itemAdapter: BoqItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBoqDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        boqId = arguments?.getString(ARG_BOQ_ID) ?: ""

        setupToolbar()
        setupRecyclerView()
        setupListeners()
        observeDetail()

        boqViewModel.loadBoqDetail(boqId)
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupRecyclerView() {
        itemAdapter = BoqItemAdapter()
        binding.rvBoqItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBoqItems.adapter = itemAdapter
    }

    private fun setupListeners() {
        binding.btnAddItem.setOnClickListener {
            val dialog = AddBoqItemDialog { code, desc, cat, unit, qty, rate ->
                boqViewModel.addBoqItem(boqId, code, desc, cat, unit, qty, rate)
            }
            dialog.show(childFragmentManager, AddBoqItemDialog.TAG)
        }

        binding.btnSubmitBoq.setOnClickListener {
            boqViewModel.submitBoq(boqId)
        }

        binding.btnApproveBoq.setOnClickListener {
            boqViewModel.decideBoq(boqId, BoqStatus.APPROVED)
        }

        binding.btnRejectBoq.setOnClickListener {
            boqViewModel.decideBoq(boqId, BoqStatus.REJECTED, "Rates revision requested by client")
        }
    }

    private fun observeDetail() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                boqViewModel.detailState.collect { state ->
                    when (state) {
                        is BoqDetailUiState.Loading -> {
                            binding.viewLoading.root.visibility = View.VISIBLE
                            binding.viewEmpty.root.visibility = View.GONE
                        }
                        is BoqDetailUiState.Success -> {
                            binding.viewLoading.root.visibility = View.GONE
                            bindBoq(state.boq)
                        }
                        is BoqDetailUiState.Error -> {
                            binding.viewLoading.root.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun bindBoq(boq: Boq) {
        val context = requireContext()
        binding.tvBoqDetailTitle.text = boq.title
        binding.tvBoqDetailSubtitle.text = "Version ${boq.version}.0"
        binding.tvDetailStatusBadge.text = boq.status.name

        val statusColorRes = when (boq.status) {
            BoqStatus.APPROVED -> R.color.cms_success
            BoqStatus.SUBMITTED -> R.color.cms_info
            BoqStatus.DRAFT -> R.color.cms_warning
            BoqStatus.REJECTED -> R.color.cms_error
        }
        binding.tvDetailStatusBadge.setTextColor(ContextCompat.getColor(context, statusColorRes))

        binding.tvDetailTotalAmount.text = boq.formatTotalAmount()
        binding.tvDetailCreatedBy.text = "Created: ${boq.createdBy ?: "Project Lead"}"
        binding.tvDetailApprovedBy.text = "Approver: ${boq.approvedBy ?: "Pending"}"

        // Workflow action button visibility
        when (boq.status) {
            BoqStatus.DRAFT -> {
                binding.btnSubmitBoq.visibility = View.VISIBLE
                binding.btnApproveBoq.visibility = View.GONE
                binding.btnRejectBoq.visibility = View.GONE
                binding.btnAddItem.visibility = View.VISIBLE
            }
            BoqStatus.SUBMITTED -> {
                binding.btnSubmitBoq.visibility = View.GONE
                binding.btnApproveBoq.visibility = View.VISIBLE
                binding.btnRejectBoq.visibility = View.VISIBLE
                binding.btnAddItem.visibility = View.GONE
            }
            BoqStatus.APPROVED, BoqStatus.REJECTED -> {
                binding.btnSubmitBoq.visibility = View.GONE
                binding.btnApproveBoq.visibility = View.GONE
                binding.btnRejectBoq.visibility = View.GONE
                binding.btnAddItem.visibility = View.GONE
            }
        }

        if (boq.items.isEmpty()) {
            binding.viewEmpty.root.visibility = View.VISIBLE
            binding.viewEmpty.tvEmptyTitle.text = "No line items in this BOQ"
            binding.viewEmpty.tvEmptySubtitle.text = "Tap '+ Add Item' to insert scheduled rate items"
        } else {
            binding.viewEmpty.root.visibility = View.GONE
            itemAdapter.submitList(boq.items)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_BOQ_ID = "boqId"

        fun newInstance(boqId: String): BoqDetailFragment {
            val fragment = BoqDetailFragment()
            val args = Bundle()
            args.putString(ARG_BOQ_ID, boqId)
            fragment.arguments = args
            return fragment
        }
    }
}
