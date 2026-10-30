package com.example.contruction_management_app.feature.inventory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.contruction_management_app.databinding.DialogReturnStockBinding
import com.example.contruction_management_app.domain.model.Material
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.math.BigDecimal

class ReturnStockDialog(
    private val material: Material,
    private val onConfirmReturn: (BigDecimal, String?, String?) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogReturnStockBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogReturnStockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvMaterialInfo.text = "${material.name} (Current: ${material.currentStock} ${material.unit})"

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnConfirmReturn.setOnClickListener {
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
                binding.tilQuantity.error = "Return quantity must be greater than zero"
                return@setOnClickListener
            }

            val ref = binding.etReference.text?.toString()?.trim()?.ifBlank { null }
            val notes = binding.etNotes.text?.toString()?.trim()?.ifBlank { null }

            onConfirmReturn(qty, ref, notes)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ReturnStockDialog"
    }
}

