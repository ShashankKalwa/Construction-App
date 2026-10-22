package com.example.contruction_management_app.feature.inventory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.contruction_management_app.databinding.DialogAdjustStockBinding
import com.example.contruction_management_app.domain.model.Material
import com.example.contruction_management_app.domain.model.MovementType
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.math.BigDecimal

class AdjustStockDialog(
    private val material: Material,
    private val defaultIssue: Boolean = true,
    private val onConfirm: (MovementType, BigDecimal, String?, String?) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogAdjustStockBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAdjustStockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvAdjustTitle.text = if (defaultIssue) "Issue Material to Site" else "Stock Adjustment"
        binding.tvMaterialInfo.text = "${material.name} (Current: ${material.currentStock} ${material.unit})"

        if (defaultIssue) {
            binding.rbIssue.isChecked = true
        } else {
            binding.rbReceive.isChecked = true
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnConfirm.setOnClickListener {
            val qtyStr = binding.etQuantity.text?.toString()?.trim()
            if (qtyStr.isNullOrBlank()) {
                binding.tilQuantity.error = "Quantity is required"
                return@setOnClickListener
            }
            val qty = try {
                BigDecimal(qtyStr)
            } catch (_: Exception) {
                binding.tilQuantity.error = "Invalid quantity"
                return@setOnClickListener
            }

            if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                binding.tilQuantity.error = "Quantity must be greater than zero"
                return@setOnClickListener
            }

            val movementType = if (binding.rbIssue.isChecked) {
                if (material.currentStock.compareTo(qty) < 0) {
                    binding.tilQuantity.error = "Insufficient stock. Available: ${material.currentStock} ${material.unit}"
                    return@setOnClickListener
                }
                MovementType.OUTWARD_ISSUE
            } else {
                MovementType.INWARD_GRN
            }

            val ref = binding.etReference.text?.toString()?.trim()?.ifBlank { null }
            val notes = binding.etNotes.text?.toString()?.trim()?.ifBlank { null }

            onConfirm(movementType, qty, ref, notes)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AdjustStockDialog"
    }
}
