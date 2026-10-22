package com.example.contruction_management_app.feature.inventory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.contruction_management_app.databinding.DialogRequestMaterialBinding
import com.example.contruction_management_app.domain.model.Material
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.math.BigDecimal

class RequestMaterialDialog(
    private val materials: List<Material>,
    private val onSubmit: (materialId: String, quantity: BigDecimal, requiredByDate: String?, phaseOrTaskDesc: String?) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogRequestMaterialBinding? = null
    private val binding get() = _binding!!

    private var selectedMaterialId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogRequestMaterialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val materialNames = materials.map { "${it.name} (${it.code})" }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, materialNames)
        binding.actvMaterial.setAdapter(adapter)

        if (materials.isNotEmpty()) {
            binding.actvMaterial.setText(materialNames.first(), false)
            selectedMaterialId = materials.first().id
        }

        binding.actvMaterial.setOnItemClickListener { _, _, position, _ ->
            selectedMaterialId = materials[position].id
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSubmitRequest.setOnClickListener {
            val matId = selectedMaterialId
            if (matId.isNullOrBlank()) {
                binding.tilMaterial.error = "Select a material"
                return@setOnClickListener
            }

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

            val reqDate = binding.etRequiredDate.text?.toString()?.trim()?.ifBlank { null }
            val desc = binding.etPhaseTask.text?.toString()?.trim()?.ifBlank { null }

            onSubmit(matId, qty, reqDate, desc)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "RequestMaterialDialog"
    }
}
