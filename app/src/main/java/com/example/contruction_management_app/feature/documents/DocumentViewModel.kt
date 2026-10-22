package com.example.contruction_management_app.feature.documents

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.contruction_management_app.core.network.NetworkClient
import com.example.contruction_management_app.domain.model.Document
import com.example.contruction_management_app.domain.model.DocumentType
import com.example.contruction_management_app.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DocumentUiState {
    object Loading : DocumentUiState
    data class Success(
        val allDocuments: List<Document>,
        val filteredDocuments: List<Document>,
        val selectedType: DocumentType? = null
    ) : DocumentUiState
    data class Error(val message: String) : DocumentUiState
}

class DocumentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DocumentRepository(NetworkClient.getApiService(application))

    private val _uiState = MutableStateFlow<DocumentUiState>(DocumentUiState.Loading)
    val uiState: StateFlow<DocumentUiState> = _uiState.asStateFlow()

    private var currentProjectId: String = "prj_commercial_tower_b"
    private var allDocumentsList = listOf<Document>()
    private var currentTypeFilter: DocumentType? = null
    private var searchQuery: String = ""

    fun loadDocuments(projectId: String) {
        currentProjectId = projectId
        viewModelScope.launch {
            _uiState.value = DocumentUiState.Loading
            val result = repository.getDocuments(projectId)
            result.fold(
                onSuccess = { list ->
                    allDocumentsList = list
                    applyFilter()
                },
                onFailure = {
                    _uiState.value = DocumentUiState.Error(it.message ?: "Failed to load documents")
                }
            )
        }
    }

    fun setTypeFilter(type: DocumentType?) {
        currentTypeFilter = type
        applyFilter()
    }

    fun setSearchQuery(query: String) {
        searchQuery = query.trim()
        applyFilter()
    }

    fun uploadDocument(
        title: String,
        type: DocumentType,
        category: String?,
        fileName: String,
        fileSizeBytes: Long,
        fileUrl: String,
        changeNotes: String?,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.createDocument(
                currentProjectId,
                title,
                type,
                category,
                fileName,
                fileSizeBytes,
                fileUrl,
                changeNotes
            )
            result.fold(
                onSuccess = { newDoc ->
                    allDocumentsList = listOf(newDoc) + allDocumentsList
                    applyFilter()
                    onComplete(true)
                },
                onFailure = { onComplete(false) }
            )
        }
    }

    fun addRevision(
        documentId: String,
        versionNumber: String,
        fileName: String,
        fileSizeBytes: Long,
        fileUrl: String,
        changeNotes: String?,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.addVersion(
                documentId,
                currentProjectId,
                versionNumber,
                fileName,
                fileSizeBytes,
                fileUrl,
                changeNotes
            )
            result.fold(
                onSuccess = {
                    loadDocuments(currentProjectId)
                    onComplete(true)
                },
                onFailure = { onComplete(false) }
            )
        }
    }

    private fun applyFilter() {
        val filtered = allDocumentsList.filter { doc ->
            val matchesType = currentTypeFilter == null || doc.documentType == currentTypeFilter
            val matchesSearch = searchQuery.isEmpty() ||
                doc.title.contains(searchQuery, ignoreCase = true) ||
                (doc.category?.contains(searchQuery, ignoreCase = true) == true)
            matchesType && matchesSearch
        }
        _uiState.value = DocumentUiState.Success(
            allDocuments = allDocumentsList,
            filteredDocuments = filtered,
            selectedType = currentTypeFilter
        )
    }
}
