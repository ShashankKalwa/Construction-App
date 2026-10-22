package com.example.contruction_management_app.feature.boq

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.contruction_management_app.databinding.DialogAddBoqItemBinding
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

class AddBoqItemDialog(
    private val onItemAdded: (
        itemCode: String,
        description: String,
        category: String,
        unit: String,
        quantity: BigDecimal,
        rate: BigDecimal
    ) -> Unit
) : DialogFragment() {

    private var _binding: DialogAddBoqItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddBoqItemBinding.inflate(LayoutInflater.from(requireContext()))

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateLiveTotal()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        binding.etQuantity.addTextChangedListener(textWatcher)
        binding.etUnitRate.addTextChangedListener(textWatcher)

        updateLiveTotal()

        binding.btnCancelItem.setOnClickListener { dismiss() }
        binding.btnSaveItem.setOnClickListener {
            val code = binding.etItemCode.text?.toString()?.trim().orEmpty()
            val category = binding.etCategory.text?.toString()?.trim().orEmpty().uppercase()
            val description = binding.etDescription.text?.toString()?.trim().orEmpty()
            val unit = binding.etUnit.text?.toString()?.trim().orEmpty().uppercase()
            val qty = binding.etQuantity.text?.toString()?.trim()?.toBigDecimalOrNull() ?: BigDecimal.ZERO
            val rate = binding.etUnitRate.text?.toString()?.trim()?.toBigDecimalOrNull() ?: BigDecimal.ZERO

            if (code.isNotBlank() && description.isNotBlank() && qty > BigDecimal.ZERO && rate > BigDecimal.ZERO) {
                onItemAdded(code, description, category, unit, qty, rate)
                dismiss()
            } else {
                if (description.isBlank()) binding.tilDescription.error = "Description required"
            }
        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

    private fun updateLiveTotal() {
        val qty = binding.etQuantity.text?.toString()?.trim()?.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val rate = binding.etUnitRate.text?.toString()?.trim()?.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val total = qty.multiply(rate)
        val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))
        binding.tvLiveCalculatedTotal.text = format.format(total)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AddBoqItemDialog"
    }
}
