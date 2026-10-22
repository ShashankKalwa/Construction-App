package com.example.contruction_management_app.feature.boq

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.contruction_management_app.databinding.DialogCreateBoqBinding

class CreateBoqDialog(
    private val onBoqCreated: (title: String, version: Int) -> Unit
) : DialogFragment() {

    private var _binding: DialogCreateBoqBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogCreateBoqBinding.inflate(LayoutInflater.from(requireContext()))

        binding.btnCancelBoq.setOnClickListener { dismiss() }
        binding.btnSaveBoq.setOnClickListener {
            val title = binding.etBoqTitle.text?.toString()?.trim()
            val versionStr = binding.etBoqVersion.text?.toString()?.trim()
            val version = versionStr?.toIntOrNull() ?: 1

            if (!title.isNullOrBlank()) {
                onBoqCreated(title, version)
                dismiss()
            } else {
                binding.tilBoqTitle.error = "Title is required"
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
        const val TAG = "CreateBoqDialog"
    }
}
