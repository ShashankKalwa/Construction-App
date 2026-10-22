package com.example.contruction_management_app.feature.documents

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.contruction_management_app.R
import com.example.contruction_management_app.databinding.FragmentDocumentsBinding
import com.example.contruction_management_app.domain.model.Document
import com.example.contruction_management_app.domain.model.DocumentType
import kotlinx.coroutines.launch

class DocumentListFragment : Fragment() {

    private var _binding: FragmentDocumentsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DocumentViewModel by viewModels()
    private var projectId: String = "prj_commercial_tower_b"
    private lateinit var adapter: DocumentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDocumentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        projectId = arguments?.getString(ARG_PROJECT_ID) ?: "prj_commercial_tower_b"

        setupRecyclerView()
        setupFilters()
        setupSearch()
        setupFab()
        observeState()

        viewModel.loadDocuments(projectId)
    }

    private fun setupRecyclerView() {
        adapter = DocumentAdapter(
            onHistoryClick = { doc ->
                DocumentHistoryDialog(doc).show(parentFragmentManager, DocumentHistoryDialog.TAG)
            },
            onAddRevisionClick = { doc ->
                showAddRevisionDialog(doc)
            }
        )
        binding.rvDocuments.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDocuments.adapter = adapter

        binding.swipeRefreshDocuments.setOnRefreshListener {
            viewModel.loadDocuments(projectId)
        }
    }

    private fun setupFilters() {
        binding.chipGroupDocTypes.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val typeFilter = when (checkedIds.first()) {
                R.id.chipArchDocs -> DocumentType.DRAWING_ARCHITECTURAL
                R.id.chipStructDocs -> DocumentType.DRAWING_STRUCTURAL
                R.id.chipContractDocs -> DocumentType.CONTRACT
                R.id.chipPermitDocs -> DocumentType.PERMIT
                else -> null
            }
            viewModel.setTypeFilter(typeFilter)
        }
    }

    private fun setupSearch() {
        binding.etSearchDocuments.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFab() {
        binding.fabUploadDocument.setOnClickListener {
            UploadDocumentDialog { title, type, category, fileName, size, url, notes ->
                viewModel.uploadDocument(title, type, category, fileName, size, url, notes) { success ->
                    if (success) {
                        Toast.makeText(requireContext(), "Document uploaded successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }.show(parentFragmentManager, UploadDocumentDialog.TAG)
        }
    }

    private fun showAddRevisionDialog(doc: Document) {
        val nextVerNumber = bumpVersion(doc.latestVersion)
        val fileName = "${doc.title.lowercase().replace(" ", "_")}_$nextVerNumber.pdf"
        viewModel.addRevision(
            documentId = doc.id,
            versionNumber = nextVerNumber,
            fileName = fileName,
            fileSizeBytes = (10_000_000L..25_000_000L).random(),
            fileUrl = "https://storage.cms.com/docs/$fileName",
            changeNotes = "Revision $nextVerNumber uploaded by site engineer."
        ) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Revision $nextVerNumber uploaded", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bumpVersion(ver: String): String {
        return try {
            val cleaned = ver.removePrefix("v")
            val parts = cleaned.split(".")
            if (parts.size == 2) {
                val major = parts[0].toInt()
                val minor = parts[1].toInt() + 1
                "v$major.$minor"
            } else {
                "v2.0"
            }
        } catch (_: Exception) {
            "v2.0"
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is DocumentUiState.Loading -> {
                            binding.swipeRefreshDocuments.isRefreshing = true
                        }
                        is DocumentUiState.Success -> {
                            binding.swipeRefreshDocuments.isRefreshing = false
                            adapter.submitList(state.filteredDocuments)
                        }
                        is DocumentUiState.Error -> {
                            binding.swipeRefreshDocuments.isRefreshing = false
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_PROJECT_ID = "arg_project_id"

        fun newInstance(projectId: String): DocumentListFragment {
            return DocumentListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PROJECT_ID, projectId)
                }
            }
        }
    }
}
