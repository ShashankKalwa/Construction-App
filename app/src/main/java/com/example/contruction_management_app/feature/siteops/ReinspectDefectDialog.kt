package com.example.contruction_management_app.feature.siteops

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.example.contruction_management_app.databinding.DialogReinspectDefectBinding
import com.example.contruction_management_app.domain.model.SiteDefect
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ReinspectDefectDialog(
    private val defect: SiteDefect,
    private val onReinspectAction: (action: String, notes: String?) -> Unit
) : DialogFragment() {

    private var _binding: DialogReinspectDefectBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogReinspectDefectBinding.inflate(LayoutInflater.from(requireContext()))

        binding.tvDefectTitlePreview.text = "${defect.title}\n${defect.description}"

        binding.btnApproveClose.setOnClickListener {
            val notes = binding.etReinspectionNotes.text?.toString()?.trim()
            onReinspectAction("APPROVE_CLOSE", notes)
            dismiss()
        }

        binding.btnRejectReopen.setOnClickListener {
            val notes = binding.etReinspectionNotes.text?.toString()?.trim()
            onReinspectAction("REJECT_REOPEN", notes)
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
        const val TAG = "ReinspectDefectDialog"
    }
}
