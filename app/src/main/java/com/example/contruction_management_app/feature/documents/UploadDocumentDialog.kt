package com.example.contruction_management_app.feature.documents

import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.contruction_management_app.R
import com.example.contruction_management_app.databinding.DialogUploadDocumentBinding
import com.example.contruction_management_app.domain.model.DocumentType

class UploadDocumentDialog(
    private val onUploadSubmitted: (title: String, type: DocumentType, category: String?, fileName: String, fileSizeBytes: Long, fileUrl: String, notes: String?) -> Unit
) : DialogFragment() {

    private var _binding: DialogUploadDocumentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        _binding = DialogUploadDocumentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val docTypes = DocumentType.values()
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            docTypes.map { it.displayName }
        )
        binding.spnDocType.adapter = adapter

        binding.btnCancelUpload.setOnClickListener { dismiss() }

        binding.btnSubmitUpload.setOnClickListener {
            val title = binding.etDocTitle.text?.toString()?.trim().orEmpty()
            val fileName = binding.etDocFileName.text?.toString()?.trim().orEmpty()
            val notes = binding.etDocChangeNotes.text?.toString()?.trim()

            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a document title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (fileName.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a file name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedType = docTypes[binding.spnDocType.selectedItemPosition]
            val simulatedSize = (5_000_000L..25_000_000L).random()
            val simulatedUrl = "https://storage.cms.com/docs/$fileName"

            onUploadSubmitted(title, selectedType, selectedType.displayName, fileName, simulatedSize, simulatedUrl, notes)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "UploadDocumentDialog"
    }
}
