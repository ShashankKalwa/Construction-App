package com.example.contruction_management_app.feature.documents

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.contruction_management_app.databinding.DialogDocumentHistoryBinding
import com.example.contruction_management_app.domain.model.Document

class DocumentHistoryDialog(
    private val document: Document
) : DialogFragment() {

    private var _binding: DialogDocumentHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDocumentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvHistoryDocTitle.text = document.title
        val adapter = DocumentVersionAdapter()
        binding.rvDocumentVersions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDocumentVersions.adapter = adapter
        adapter.submitList(document.versions)

        binding.btnCloseHistory.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DocumentHistoryDialog"
    }
}
