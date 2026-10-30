package com.example.contruction_management_app.feature.siteops

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.example.contruction_management_app.R
import com.example.contruction_management_app.databinding.DialogCreateDefectBinding
import com.example.contruction_management_app.domain.model.IssueSeverity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CreateDefectDialog(
    private val onSave: (
        title: String,
        description: String,
        location: String?,
        severity: IssueSeverity,
        assignedTo: String?,
        deadline: String?
    ) -> Unit
) : DialogFragment() {

    private var _binding: DialogCreateDefectBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogCreateDefectBinding.inflate(LayoutInflater.from(requireContext()))

        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnSaveDefect.setOnClickListener {
            val title = binding.etDefectTitle.text?.toString()?.trim().orEmpty()
            val description = binding.etDefectDescription.text?.toString()?.trim().orEmpty()
            if (title.isBlank()) {
                binding.tilDefectTitle.error = "Title is required"
                return@setOnClickListener
            }
            if (description.isBlank()) {
                binding.tilDefectDescription.error = "Description is required"
                return@setOnClickListener
            }

            val severity = when (binding.chipGroupSeverity.checkedChipId) {
                R.id.chipCritical -> IssueSeverity.CRITICAL
                R.id.chipHigh -> IssueSeverity.HIGH
                R.id.chipLow -> IssueSeverity.LOW
                else -> IssueSeverity.MEDIUM
            }

            val location = binding.etLocation.text?.toString()?.trim().orEmpty().ifBlank { null }
            val deadline = binding.etDeadline.text?.toString()?.trim().orEmpty().ifBlank { null }
            val assignedTo = binding.etAssignedTo.text?.toString()?.trim().orEmpty().ifBlank { null }

            onSave(title, description, location, severity, assignedTo, deadline)
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
        const val TAG = "CreateDefectDialog"
    }
}
