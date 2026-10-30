package com.example.contruction_management_app.feature.finance

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.contruction_management_app.core.network.CreatePurchaseOrderItemRequest
import com.example.contruction_management_app.databinding.DialogCreatePurchaseOrderBinding
import com.example.contruction_management_app.domain.model.Vendor
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

class CreatePurchaseOrderDialog(
    private val vendors: List<Vendor>,
    private val onSubmit: (
        vendorId: String,
        poNumber: String?,
        deliveryDate: String?,
        paymentTerms: String?,
        notes: String?,
        items: List<CreatePurchaseOrderItemRequest>
    ) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogCreatePurchaseOrderBinding? = null
    private val binding get() = _binding!!

    private var selectedVendorId: String? = null

    private val inrFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCreatePurchaseOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Vendor dropdown
        val vendorNames = vendors.map { "${it.name} (${it.category ?: "VENDOR"})" }
        val vendorAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, vendorNames)
        binding.actvVendor.setAdapter(vendorAdapter)

        if (vendors.isNotEmpty()) {
            binding.actvVendor.setText(vendorNames.first(), false)
            selectedVendorId = vendors.first().id
        }

        binding.actvVendor.setOnItemClickListener { _, _, position, _ ->
            if (position in vendors.indices) {
                selectedVendorId = vendors[position].id
            }
        }

        // Expected delivery date default (today + 7 days)
        val defaultDeliveryDate = LocalDate.now().plusDays(7).toString()
        binding.etDeliveryDate.setText(defaultDeliveryDate)

        // Text watchers for real-time cost calculation
        val calculationWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                updateFinancialPreview()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        binding.etQuantity.addTextChangedListener(calculationWatcher)
        binding.etUnitPrice.addTextChangedListener(calculationWatcher)

        updateFinancialPreview()

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSubmitPo.setOnClickListener {
            val vendorId = selectedVendorId ?: vendors.firstOrNull()?.id
            if (vendorId.isNullOrBlank()) {
                binding.tilVendor.error = "Please select a vendor"
                return@setOnClickListener
            }

            val desc = binding.etItemDescription.text?.toString()?.trim()
            if (desc.isNullOrBlank()) {
                binding.tilItemDescription.error = "Item description is required"
                return@setOnClickListener
            }

            val qtyStr = binding.etQuantity.text?.toString()?.trim()
            val qty = try {
                if (qtyStr.isNullOrBlank()) null else BigDecimal(qtyStr)
            } catch (_: Exception) {
                null
            }
            if (qty == null || qty <= BigDecimal.ZERO) {
                binding.tilQuantity.error = "Quantity must be > 0"
                return@setOnClickListener
            }

            val priceStr = binding.etUnitPrice.text?.toString()?.trim()
            val unitPrice = try {
                if (priceStr.isNullOrBlank()) null else BigDecimal(priceStr)
            } catch (_: Exception) {
                null
            }
            if (unitPrice == null || unitPrice <= BigDecimal.ZERO) {
                binding.tilUnitPrice.error = "Price must be > 0"
                return@setOnClickListener
            }

            val materialId = binding.etMaterialCode.text?.toString()?.trim()?.ifBlank { "mat_cem_opc" } ?: "mat_cem_opc"
            val poNumber = binding.etPoNumber.text?.toString()?.trim()?.ifBlank { null }
            val deliveryDate = binding.etDeliveryDate.text?.toString()?.trim()?.ifBlank { defaultDeliveryDate }

            val item = CreatePurchaseOrderItemRequest(
                materialId = materialId,
                orderedQuantity = qty,
                unitPrice = unitPrice
            )

            onSubmit(
                vendorId,
                poNumber,
                deliveryDate,
                "NET_30",
                null,
                listOf(item)
            )
            dismiss()
        }
    }

    private fun updateFinancialPreview() {
        val qty = binding.etQuantity.text?.toString()?.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val price = binding.etUnitPrice.text?.toString()?.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val total = qty.multiply(price).setScale(2, RoundingMode.HALF_UP)

        binding.tvPreviewSubtotal.text = inrFormat.format(total)
        binding.tvPreviewTotal.text = inrFormat.format(total)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CreatePurchaseOrderDialog"
    }
}
