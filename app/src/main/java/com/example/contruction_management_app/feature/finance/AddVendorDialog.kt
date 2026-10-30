package com.example.contruction_management_app.feature.finance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.contruction_management_app.databinding.DialogAddVendorBinding
import com.example.contruction_management_app.domain.model.VendorCategory
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddVendorDialog(
    private val onSubmit: (
        name: String,
        category: VendorCategory,
        contactPerson: String?,
        phone: String?,
        email: String?,
        gstin: String?,
        paymentTerms: String?
    ) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogAddVendorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddVendorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categories = VendorCategory.values().map { it.name }
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.actvVendorCategory.setAdapter(categoryAdapter)
        binding.actvVendorCategory.setText(categories.first(), false)

        val paymentTerms = listOf("NET_30", "NET_15", "NET_45", "NET_60", "IMMEDIATE", "ADVANCE")
        val termsAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, paymentTerms)
        binding.actvPaymentTerms.setAdapter(termsAdapter)
        binding.actvPaymentTerms.setText(paymentTerms.first(), false)

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSubmitVendor.setOnClickListener {
            val name = binding.etVendorName.text?.toString()?.trim()
            if (name.isNullOrBlank()) {
                binding.tilVendorName.error = "Vendor name is required"
                return@setOnClickListener
            }

            val categoryStr = binding.actvVendorCategory.text?.toString()?.trim() ?: VendorCategory.MATERIALS.name
            val category = try {
                VendorCategory.valueOf(categoryStr)
            } catch (_: Exception) {
                VendorCategory.MATERIALS
            }

            val contactPerson = binding.etContactPerson.text?.toString()?.trim()?.ifBlank { null }
            val phone = binding.etPhone.text?.toString()?.trim()?.ifBlank { null }
            val email = binding.etEmail.text?.toString()?.trim()?.ifBlank { null }
            val gstin = binding.etGstin.text?.toString()?.trim()?.ifBlank { null }
            val terms = binding.actvPaymentTerms.text?.toString()?.trim()?.ifBlank { null }

            onSubmit(name, category, contactPerson, phone, email, gstin, terms)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AddVendorDialog"
    }
}

