package com.example.contruction_management_app.feature.siteops

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.contruction_management_app.databinding.DialogCreateSiteIssueBinding
import com.example.contruction_management_app.domain.model.IssueSeverity

class CreateSiteIssueDialog(
    private val onIssueCreated: (
        title: String,
        description: String?,
        severity: IssueSeverity,
        assignedTo: String?
    ) -> Unit
) : DialogFragment() {

    private var _binding: DialogCreateSiteIssueBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogCreateSiteIssueBinding.inflate(LayoutInflater.from(requireContext()))

        binding.btnCancelIssue.setOnClickListener { dismiss() }
        binding.btnSaveIssue.setOnClickListener {
            val title = binding.etIssueTitle.text?.toString()?.trim().orEmpty()
            val desc = binding.etIssueDescription.text?.toString()?.trim()
            val sevStr = binding.etIssueSeverity.text?.toString()?.trim().orEmpty().uppercase()
            val severity = try {
                IssueSeverity.valueOf(sevStr)
            } catch (_: Exception) {
                IssueSeverity.MEDIUM
            }
            val assigned = binding.etIssueAssignedTo.text?.toString()?.trim()

            if (title.isNotBlank()) {
                onIssueCreated(title, desc, severity, assigned)
                dismiss()
            } else {
                binding.tilIssueTitle.error = "Title is required"
            }
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
        const val TAG = "CreateSiteIssueDialog"
    }
}
