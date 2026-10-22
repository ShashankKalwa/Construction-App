package com.example.contruction_management_app.feature.measurement

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.contruction_management_app.databinding.DialogAddMeasurementBinding
import java.math.BigDecimal
import java.math.RoundingMode

class AddMeasurementDialog(
    private val onMeasurementAdded: (
        description: String,
        nos: BigDecimal,
        length: BigDecimal,
        width: BigDecimal,
        depth: BigDecimal,
        deduction: BigDecimal,
        unit: String,
        location: String?
    ) -> Unit
) : DialogFragment() {

    private var _binding: DialogAddMeasurementBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddMeasurementBinding.inflate(LayoutInflater.from(requireContext()))

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateLiveNet()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        binding.etMbNos.addTextChangedListener(watcher)
        binding.etMbLength.addTextChangedListener(watcher)
        binding.etMbWidth.addTextChangedListener(watcher)
        binding.etMbDepth.addTextChangedListener(watcher)
        binding.etMbDeduction.addTextChangedListener(watcher)

        updateLiveNet()

        binding.btnCancelMb.setOnClickListener { dismiss() }
        binding.btnSaveMb.setOnClickListener {
            val desc = binding.etMbDescription.text?.toString()?.trim().orEmpty()
            val nos = binding.etMbNos.text?.toString()?.trim()?.toBigDecimalOrNull() ?: BigDecimal.ONE
            val length = binding.etMbLength.text?.toString()?.trim()?.toBigDecimalOrNull() ?: BigDecimal.ZERO
            val width = binding.etMbWidth.text?.toString()?.trim()?.toBigDecimalOrNull() ?: BigDecimal.ZERO
            val depth = binding.etMbDepth.text?.toString()?.trim()?.toBigDecimalOrNull() ?: BigDecimal.ZERO
            val ded = binding.etMbDeduction.text?.toString()?.trim()?.toBigDecimalOrNull() ?: BigDecimal.ZERO
            val unit = binding.etMbUnit.text?.toString()?.trim().orEmpty().ifBlank { "M3" }
            val location = binding.etMbLocation.text?.toString()?.trim()

            if (desc.isNotBlank() && length > BigDecimal.ZERO && width > BigDecimal.ZERO && depth > BigDecimal.ZERO) {
                onMeasurementAdded(desc, nos, length, width, depth, ded, unit, location)
                dismiss()
            } else {
                if (desc.isBlank()) binding.tilMbDescription.error = "Description required"
            }
        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

    private fun updateLiveNet() {
        val nos = binding.etMbNos.text?.toString()?.trim()?.toBigDecimalOrNull() ?: BigDecimal.ONE
        val length = binding.etMbLength.text?.toString()?.trim()?.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val width = binding.etMbWidth.text?.toString()?.trim()?.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val depth = binding.etMbDepth.text?.toString()?.trim()?.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val ded = binding.etMbDeduction.text?.toString()?.trim()?.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val unit = binding.etMbUnit.text?.toString()?.trim().orEmpty().ifBlank { "M3" }

        val gross = nos.multiply(length).multiply(width).multiply(depth)
        val net = gross.subtract(ded).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)
        binding.tvLiveNetQuantity.text = "${net.toPlainString()} $unit"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AddMeasurementDialog"
    }
}
