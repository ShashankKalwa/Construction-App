package com.example.contruction_management_app.feature.siteops

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.contruction_management_app.databinding.DialogApproveDsrBinding
import com.example.contruction_management_app.domain.model.DailySiteReport
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ApproveDsrDialog(
    private val report: DailySiteReport,
    private val onActionConfirmed: (action: String, notes: String?) -> Unit
) : DialogFragment() {

    private var _binding: DialogApproveDsrBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogApproveDsrBinding.inflate(LayoutInflater.from(requireContext()))

        binding.tvApprovalTitle.text = "Review DSR: ${report.reportDate}"
        binding.tvReportSummaryPreview.text = "${report.totalHeadcount} workers on site.\n${report.workSummary}"

        binding.btnApprove.setOnClickListener {
            val notes = binding.etApprovalNotes.text?.toString()?.trim()
            onActionConfirmed("APPROVE", notes)
            dismiss()
        }

        binding.btnReject.setOnClickListener {
            val notes = binding.etApprovalNotes.text?.toString()?.trim()
            onActionConfirmed("REJECT", notes)
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
        const val TAG = "ApproveDsrDialog"
    }
}
