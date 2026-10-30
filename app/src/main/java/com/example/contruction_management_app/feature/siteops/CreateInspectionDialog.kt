package com.example.contruction_management_app.feature.siteops

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.example.contruction_management_app.R
import com.example.contruction_management_app.databinding.DialogCreateInspectionBinding
import com.example.contruction_management_app.domain.model.InspectionStatus
import com.example.contruction_management_app.domain.model.InspectionType
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONArray
import org.json.JSONObject

class CreateInspectionDialog(
    private val onSubmitted: (
        title: String,
        type: InspectionType,
        status: InspectionStatus,
        checklistJson: String,
        location: String?,
        notes: String?
    ) -> Unit
) : DialogFragment() {

    private var _binding: DialogCreateInspectionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogCreateInspectionBinding.inflate(LayoutInflater.from(requireContext()))

        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnSubmitInspection.setOnClickListener {
            val title = binding.etInspectionTitle.text?.toString()?.trim().orEmpty()
            if (title.isBlank()) {
                binding.tilInspectionTitle.error = "Title is required"
                return@setOnClickListener
            }

            val type = when (binding.chipGroupInspectionType.checkedChipId) {
                R.id.chipSafety -> InspectionType.SAFETY
                R.id.chipStructural -> InspectionType.STRUCTURAL
                R.id.chipMep -> InspectionType.MEP
                else -> InspectionType.QUALITY
            }

            val check1 = binding.cbChecklist1.isChecked
            val check2 = binding.cbChecklist2.isChecked
            val check3 = binding.cbChecklist3.isChecked

            val checklist = JSONArray().apply {
                put(JSONObject().put("item", binding.cbChecklist1.text.toString()).put("passed", check1))
                put(JSONObject().put("item", binding.cbChecklist2.text.toString()).put("passed", check2))
                put(JSONObject().put("item", binding.cbChecklist3.text.toString()).put("passed", check3))
            }

            val status = if (check1 && check2 && check3) {
                InspectionStatus.PASSED
            } else {
                InspectionStatus.FAILED
            }

            val location = binding.etLocation.text?.toString()?.trim().orEmpty().ifBlank { null }
            val notes = binding.etInspectionNotes.text?.toString()?.trim().orEmpty().ifBlank { null }

            onSubmitted(title, type, status, checklist.toString(), location, notes)
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
        const val TAG = "CreateInspectionDialog"
    }
}
