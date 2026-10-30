package com.example.contruction_management_app.feature.finance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.contruction_management_app.databinding.DialogRecordGrnBinding
import com.example.contruction_management_app.domain.model.PurchaseOrder
import com.example.contruction_management_app.domain.model.PurchaseOrderItem
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordGrnDialog(
    private val po: PurchaseOrder,
    private val onConfirmGrn: (poId: String, materialId: String, challanNumber: String, receivedQty: BigDecimal, receivedDate: String, remarks: String?) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogRecordGrnBinding? = null
    private val binding get() = _binding!!

    private var selectedItem: PurchaseOrderItem? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogRecordGrnBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvPoDetails.text = "${po.poNumber} • ${po.vendorName ?: "Vendor"}"

        val items = po.items
        val itemLabels = items.map { "${it.materialName} (${it.orderedQuantity} ${it.unit})" }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, itemLabels)
        binding.actvMaterialItem.setAdapter(adapter)

        if (items.isNotEmpty()) {
            binding.actvMaterialItem.setText(itemLabels.first(), false)
            selectedItem = items.first()
            val remaining = selectedItem!!.orderedQuantity.subtract(selectedItem!!.receivedQuantity)
            if (remaining > BigDecimal.ZERO) {
                binding.etReceivedQty.setText(remaining.toPlainString())
            }
        }

        binding.actvMaterialItem.setOnItemClickListener { _, _, position, _ ->
            selectedItem = items[position]
            val remaining = selectedItem!!.orderedQuantity.subtract(selectedItem!!.receivedQuantity)
            if (remaining > BigDecimal.ZERO) {
                binding.etReceivedQty.setText(remaining.toPlainString())
            }
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnConfirmGrn.setOnClickListener {
            val item = selectedItem
            if (item == null) {
                binding.tilMaterialItem.error = "Select a material item from the PO"
                return@setOnClickListener
            }

            val challan = binding.etChallanNumber.text?.toString()?.trim()
            if (challan.isNullOrBlank()) {
                binding.tilChallanNumber.error = "Challan / Invoice number is required"
                return@setOnClickListener
            }

            val qtyStr = binding.etReceivedQty.text?.toString()?.trim()
            if (qtyStr.isNullOrBlank()) {
                binding.tilReceivedQty.error = "Received quantity is required"
                return@setOnClickListener
            }
            val qty = try {
                BigDecimal(qtyStr)
            } catch (_: Exception) {
                binding.tilReceivedQty.error = "Invalid quantity"
                return@setOnClickListener
            }

            if (qty <= BigDecimal.ZERO) {
                binding.tilReceivedQty.error = "Received quantity must be greater than zero"
                return@setOnClickListener
            }

            val remarks = binding.etQualityRemarks.text?.toString()?.trim()?.ifBlank { null }
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            onConfirmGrn(po.id, item.materialId, challan, qty, dateStr, remarks)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "RecordGrnDialog"
    }
}

