package com.example.contruction_management_app.feature.siteops

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.contruction_management_app.databinding.DialogUpdateIssueStatusBinding
import com.example.contruction_management_app.domain.model.IssueStatus
import com.example.contruction_management_app.domain.model.SiteIssue

class UpdateIssueStatusDialog(
    private val issue: SiteIssue,
    private val onStatusUpdated: (status: IssueStatus, notes: String?) -> Unit
) : DialogFragment() {

    private var _binding: DialogUpdateIssueStatusBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogUpdateIssueStatusBinding.inflate(LayoutInflater.from(requireContext()))

        binding.tvUpdateIssueTitle.text = issue.title
        binding.etNewStatus.setText(
            if (issue.status == IssueStatus.OPEN) "IN_PROGRESS" else "RESOLVED"
        )

        binding.btnCancelUpdateStatus.setOnClickListener { dismiss() }
        binding.btnSaveUpdateStatus.setOnClickListener {
            val statusStr = binding.etNewStatus.text?.toString()?.trim().orEmpty().uppercase()
            val status = try {
                IssueStatus.valueOf(statusStr)
            } catch (_: Exception) {
                IssueStatus.RESOLVED
            }
            val notes = binding.etResolutionNotes.text?.toString()?.trim()

            onStatusUpdated(status, notes)
            dismiss()
        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "UpdateIssueStatusDialog"
    }
}
