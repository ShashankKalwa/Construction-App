package com.example.contruction_management_app.feature.finance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.contruction_management_app.databinding.DialogRejectExpenseBinding
import com.example.contruction_management_app.domain.model.Expense
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RejectExpenseDialog(
    private val expense: Expense,
    private val onConfirmReject: (reason: String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogRejectExpenseBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogRejectExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvRejectSubtitle.text = "Rejecting: \"${expense.title}\" (${expense.category.name})"

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnConfirmReject.setOnClickListener {
            val reason = binding.etRejectionReason.text?.toString()?.trim()
            if (reason.isNullOrBlank()) {
                binding.tilRejectionReason.error = "Reason is required for audit compliance"
                return@setOnClickListener
            }

            onConfirmReject(reason)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "RejectExpenseDialog"
    }
}

