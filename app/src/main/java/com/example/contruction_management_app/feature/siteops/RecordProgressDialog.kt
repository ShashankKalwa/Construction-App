package com.example.contruction_management_app.feature.siteops

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.example.contruction_management_app.databinding.DialogRecordProgressBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.math.BigDecimal

class RecordProgressDialog(
    private val onSave: (itemName: String, unit: String, planned: BigDecimal, reported: BigDecimal) -> Unit
) : DialogFragment() {

    private var _binding: DialogRecordProgressBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogRecordProgressBinding.inflate(LayoutInflater.from(requireContext()))

        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnSaveProgress.setOnClickListener {
            val itemName = binding.etItemName.text?.toString()?.trim().orEmpty()
            val unit = binding.etUnit.text?.toString()?.trim().orEmpty().ifBlank { "unit" }
            val plannedStr = binding.etPlannedQty.text?.toString()?.trim().orEmpty()
            val reportedStr = binding.etReportedQty.text?.toString()?.trim().orEmpty()

            if (itemName.isBlank()) {
                binding.tilItemName.error = "Item name is required"
                return@setOnClickListener
            }
            val planned = plannedStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
            val reported = reportedStr.toBigDecimalOrNull() ?: BigDecimal.ZERO

            onSave(itemName, unit, planned, reported)
            dismiss()
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "RecordProgressDialog"
    }
}
