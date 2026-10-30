package com.example.contruction_management_app.feature.siteops

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.example.contruction_management_app.databinding.DialogVerifyProgressBinding
import com.example.contruction_management_app.domain.model.WorkProgress
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.math.BigDecimal

class VerifyProgressDialog(
    private val progress: WorkProgress,
    private val onVerified: (action: String, verifiedQty: BigDecimal, notes: String?) -> Unit
) : DialogFragment() {

    private var _binding: DialogVerifyProgressBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogVerifyProgressBinding.inflate(LayoutInflater.from(requireContext()))

        binding.tvItemSummary.text = "${progress.itemName}\nPlanned: ${progress.plannedQuantity.stripTrailingZeros().toPlainString()} ${progress.unit} | Reported: ${progress.reportedQuantity.stripTrailingZeros().toPlainString()} ${progress.unit} (by ${progress.reportedBy})"

        // Default to reported quantity
        binding.etVerifiedQty.setText(progress.reportedQuantity.stripTrailingZeros().toPlainString())

        binding.btnConfirmVerify.setOnClickListener {
            val qtyStr = binding.etVerifiedQty.text?.toString()?.trim()
            val qty = qtyStr?.toBigDecimalOrNull() ?: progress.reportedQuantity
            val notes = binding.etVerificationNotes.text?.toString()?.trim()
            onVerified("VERIFY", qty, notes)
            dismiss()
        }

        binding.btnRejectProgress.setOnClickListener {
            val notes = binding.etVerificationNotes.text?.toString()?.trim()
            onVerified("REJECT", BigDecimal.ZERO, notes)
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
        const val TAG = "VerifyProgressDialog"
    }
}
