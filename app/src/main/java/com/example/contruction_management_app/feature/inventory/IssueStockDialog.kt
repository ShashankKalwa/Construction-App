package com.example.contruction_management_app.feature.inventory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.contruction_management_app.databinding.DialogIssueStockBinding
import com.example.contruction_management_app.domain.model.Material
import com.example.contruction_management_app.domain.model.MaterialRequest
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class IssueStockDialog(
    private val request: MaterialRequest,
    private val material: Material?,
    private val onConfirmIssue: (MaterialRequest, String?) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogIssueStockBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogIssueStockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvIssueMaterialName.text = request.materialName
        binding.tvIssueQuantity.text = "Quantity to Issue: ${request.quantity} ${request.materialUnit}"

        if (material != null) {
            binding.tvCurrentAvailableStock.text =
                "Current Warehouse Stock: ${material.currentStock} ${material.unit}"
            if (material.currentStock < request.quantity) {
                binding.tvCurrentAvailableStock.setTextColor(
                    resources.getColor(com.example.contruction_management_app.R.color.cms_error, null)
                )
                binding.btnConfirmIssue.isEnabled = false
                binding.tilIssueNotes.error = "Insufficient warehouse stock to fulfill request"
            }
        } else {
            binding.tvCurrentAvailableStock.text = "Stock check pending verification"
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnConfirmIssue.setOnClickListener {
            val notes = binding.etIssueNotes.text?.toString()?.trim()?.ifBlank { null }
            onConfirmIssue(request, notes)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "IssueStockDialog"
    }
}

