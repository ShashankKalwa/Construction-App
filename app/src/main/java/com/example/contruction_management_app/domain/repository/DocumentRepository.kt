package com.example.contruction_management_app.domain.repository

import com.example.contruction_management_app.core.network.*
import com.example.contruction_management_app.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class DocumentRepository(private val apiService: CmsApiService) {

    private val localDocumentCache = ConcurrentHashMap<String, MutableList<Document>>()

    init {
        seedInitialDemoDocuments("prj_commercial_tower_b")
    }

    private fun seedInitialDemoDocuments(projectId: String) {
        val docs = mutableListOf(
            Document(
                id = "doc_arch_floor_plan",
                projectId = projectId,
                organizationId = "org_apex_infra",
                title = "Architectural Layout - Typical Floor Plan (L4-L18)",
                documentType = DocumentType.DRAWING_ARCHITECTURAL,
                category = "Drawings",
                latestVersion = "v2.1",
                status = DocumentStatus.APPROVED,
                createdBy = "usr_pm_rahul",
                createdAt = "2026-02-15T10:00:00Z",
                updatedAt = "2026-03-02T14:30:00Z",
                versions = listOf(
                    DocumentVersion(
                        id = "doc_v_arch_2_1",
                        documentId = "doc_arch_floor_plan",
                        versionNumber = "v2.1",
                        fileName = "commercial_tower_b_arch_v2.1.pdf",
                        fileSizeBytes = 17300000L,
                        mimeType = "application/pdf",
                        fileUrl = "https://storage.cms.com/docs/arch_v2_1.pdf",
                        changeNotes = "Minor alignment with facade curtain-wall mullion spacing.",
                        uploadedBy = "usr_eng_priya",
                        uploadedAt = "2026-03-02T14:30:00Z"
                    ),
                    DocumentVersion(
                        id = "doc_v_arch_2_0",
                        documentId = "doc_arch_floor_plan",
                        versionNumber = "v2.0",
                        fileName = "commercial_tower_b_arch_v2.0.pdf",
                        fileSizeBytes = 16980000L,
                        mimeType = "application/pdf",
                        fileUrl = "https://storage.cms.com/docs/arch_v2_0.pdf",
                        changeNotes = "Updated stairwell clearances per revised fire safety code.",
                        uploadedBy = "usr_eng_priya",
                        uploadedAt = "2026-02-24T11:15:00Z"
                    ),
                    DocumentVersion(
                        id = "doc_v_arch_1_0",
                        documentId = "doc_arch_floor_plan",
                        versionNumber = "v1.0",
                        fileName = "commercial_tower_b_arch_v1.0.pdf",
                        fileSizeBytes = 15520000L,
                        mimeType = "application/pdf",
                        fileUrl = "https://storage.cms.com/docs/arch_v1_0.pdf",
                        changeNotes = "Initial approved schematic architectural layout.",
                        uploadedBy = "usr_pm_rahul",
                        uploadedAt = "2026-02-15T10:00:00Z"
                    )
                )
            ),
            Document(
                id = "doc_struct_foundation",
                projectId = projectId,
                organizationId = "org_apex_infra",
                title = "Structural Detailing - Raft & Pile Foundation",
                documentType = DocumentType.DRAWING_STRUCTURAL,
                category = "Structural",
                latestVersion = "v1.2",
                status = DocumentStatus.APPROVED,
                createdBy = "usr_eng_priya",
                createdAt = "2026-01-20T09:00:00Z",
                updatedAt = "2026-02-18T16:45:00Z",
                versions = listOf(
                    DocumentVersion(
                        id = "doc_v_struct_1_2",
                        documentId = "doc_struct_foundation",
                        versionNumber = "v1.2",
                        fileName = "tower_b_foundation_layout_v1.2.pdf",
                        fileSizeBytes = 25160000L,
                        mimeType = "application/pdf",
                        fileUrl = "https://storage.cms.com/docs/struct_v1_2.pdf",
                        changeNotes = "Upgraded bottom mesh reinforcement to 32mm Fe500D bars.",
                        uploadedBy = "usr_eng_priya",
                        uploadedAt = "2026-02-18T16:45:00Z"
                    ),
                    DocumentVersion(
                        id = "doc_v_struct_1_0",
                        documentId = "doc_struct_foundation",
                        versionNumber = "v1.0",
                        fileName = "tower_b_foundation_layout_v1.0.pdf",
                        fileSizeBytes = 23480000L,
                        mimeType = "application/pdf",
                        fileUrl = "https://storage.cms.com/docs/struct_v1_0.pdf",
                        changeNotes = "Initial foundation layout with 1200mm dia bored piles.",
                        uploadedBy = "usr_eng_priya",
                        uploadedAt = "2026-01-20T09:00:00Z"
                    )
                )
            ),
            Document(
                id = "doc_rera_sanction",
                projectId = projectId,
                organizationId = "org_apex_infra",
                title = "RERA Municipal Commencement Certificate",
                documentType = DocumentType.PERMIT,
                category = "Compliance",
                latestVersion = "v1.0",
                status = DocumentStatus.APPROVED,
                createdBy = "usr_pm_rahul",
                createdAt = "2026-01-05T08:00:00Z",
                updatedAt = "2026-01-05T08:00:00Z",
                versions = listOf(
                    DocumentVersion(
                        id = "doc_v_rera_1_0",
                        documentId = "doc_rera_sanction",
                        versionNumber = "v1.0",
                        fileName = "rera_commencement_certificate_signed.pdf",
                        fileSizeBytes = 4710000L,
                        mimeType = "application/pdf",
                        fileUrl = "https://storage.cms.com/docs/rera_sanction.pdf",
                        changeNotes = "Officially stamped and sealed municipal commencement permit.",
                        uploadedBy = "usr_pm_rahul",
                        uploadedAt = "2026-01-05T08:00:00Z"
                    )
                )
            )
        )
        localDocumentCache[projectId] = docs
    }

    suspend fun getDocuments(projectId: String, typeFilter: DocumentType? = null): Result<List<Document>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getDocuments(projectId, typeFilter?.name)
                if (response.isSuccessful && response.body() != null) {
                    val networkDocs = response.body()!!.map { mapNetworkToDomain(it) }
                    localDocumentCache[projectId] = networkDocs.toMutableList()
                    val filtered = if (typeFilter != null) networkDocs.filter { it.documentType == typeFilter } else networkDocs
                    Result.success(filtered)
                } else {
                    val cached = localDocumentCache[projectId] ?: emptyList()
                    val filtered = if (typeFilter != null) cached.filter { it.documentType == typeFilter } else cached
                    Result.success(filtered)
                }
            } catch (e: Exception) {
                val cached = localDocumentCache[projectId] ?: emptyList()
                val filtered = if (typeFilter != null) cached.filter { it.documentType == typeFilter } else cached
                Result.success(filtered)
            }
        }

    suspend fun createDocument(
        projectId: String,
        title: String,
        documentType: DocumentType,
        category: String?,
        fileName: String,
        fileSizeBytes: Long,
        fileUrl: String,
        changeNotes: String?
    ): Result<Document> = withContext(Dispatchers.IO) {
        val request = CreateDocumentRequest(
            title = title,
            documentType = documentType.name,
            category = category,
            versionNumber = "v1.0",
            fileName = fileName,
            fileSizeBytes = fileSizeBytes,
            fileUrl = fileUrl,
            changeNotes = changeNotes
        )
        try {
            val response = apiService.createDocument(projectId, request)
            if (response.isSuccessful && response.body() != null) {
                val doc = mapNetworkToDomain(response.body()!!)
                localDocumentCache.getOrPut(projectId) { mutableListOf() }.add(0, doc)
                Result.success(doc)
            } else {
                createLocalFallback(projectId, title, documentType, category, fileName, fileSizeBytes, fileUrl, changeNotes)
            }
        } catch (e: Exception) {
            createLocalFallback(projectId, title, documentType, category, fileName, fileSizeBytes, fileUrl, changeNotes)
        }
    }

    private fun createLocalFallback(
        projectId: String,
        title: String,
        documentType: DocumentType,
        category: String?,
        fileName: String,
        fileSizeBytes: Long,
        fileUrl: String,
        changeNotes: String?
    ): Result<Document> {
        val docId = UUID.randomUUID().toString()
        val version = DocumentVersion(
            id = UUID.randomUUID().toString(),
            documentId = docId,
            versionNumber = "v1.0",
            fileName = fileName,
            fileSizeBytes = fileSizeBytes,
            mimeType = "application/pdf",
            fileUrl = fileUrl,
            changeNotes = changeNotes ?: "Initial document creation",
            uploadedBy = "usr_current_user",
            uploadedAt = "Just now"
        )
        val doc = Document(
            id = docId,
            projectId = projectId,
            organizationId = "org_local",
            title = title,
            documentType = documentType,
            category = category ?: "General",
            latestVersion = "v1.0",
            status = DocumentStatus.APPROVED,
            createdBy = "usr_current_user",
            createdAt = "Just now",
            updatedAt = "Just now",
            versions = listOf(version)
        )
        localDocumentCache.getOrPut(projectId) { mutableListOf() }.add(0, doc)
        return Result.success(doc)
    }

    suspend fun addVersion(
        documentId: String,
        projectId: String,
        versionNumber: String,
        fileName: String,
        fileSizeBytes: Long,
        fileUrl: String,
        changeNotes: String?
    ): Result<DocumentVersion> = withContext(Dispatchers.IO) {
        val request = CreateDocumentVersionRequest(
            versionNumber = versionNumber,
            fileName = fileName,
            fileSizeBytes = fileSizeBytes,
            fileUrl = fileUrl,
            changeNotes = changeNotes
        )
        try {
            val response = apiService.addDocumentVersion(documentId, request)
            if (response.isSuccessful && response.body() != null) {
                val vNet = response.body()!!
                val newVer = DocumentVersion(
                    id = vNet.id,
                    documentId = vNet.documentId,
                    versionNumber = vNet.versionNumber,
                    fileName = vNet.fileName,
                    fileSizeBytes = vNet.fileSizeBytes,
                    mimeType = vNet.mimeType,
                    fileUrl = vNet.fileUrl,
                    changeNotes = vNet.changeNotes,
                    uploadedBy = vNet.uploadedBy,
                    uploadedAt = vNet.uploadedAt
                )
                updateLocalDocVersions(projectId, documentId, newVer)
                Result.success(newVer)
            } else {
                addLocalVersionFallback(projectId, documentId, versionNumber, fileName, fileSizeBytes, fileUrl, changeNotes)
            }
        } catch (e: Exception) {
            addLocalVersionFallback(projectId, documentId, versionNumber, fileName, fileSizeBytes, fileUrl, changeNotes)
        }
    }

    private fun addLocalVersionFallback(
        projectId: String,
        documentId: String,
        versionNumber: String,
        fileName: String,
        fileSizeBytes: Long,
        fileUrl: String,
        changeNotes: String?
    ): Result<DocumentVersion> {
        val newVer = DocumentVersion(
            id = UUID.randomUUID().toString(),
            documentId = documentId,
            versionNumber = versionNumber,
            fileName = fileName,
            fileSizeBytes = fileSizeBytes,
            mimeType = "application/pdf",
            fileUrl = fileUrl,
            changeNotes = changeNotes ?: "Revision upload",
            uploadedBy = "usr_current_user",
            uploadedAt = "Just now"
        )
        updateLocalDocVersions(projectId, documentId, newVer)
        return Result.success(newVer)
    }

    private fun updateLocalDocVersions(projectId: String, documentId: String, newVer: DocumentVersion) {
        val list = localDocumentCache[projectId] ?: return
        val index = list.indexOfFirst { it.id == documentId }
        if (index != -1) {
            val existing = list[index]
            val updatedVersions = mutableListOf(newVer).apply { addAll(existing.versions) }
            val updatedDoc = existing.copy(
                latestVersion = newVer.versionNumber,
                versions = updatedVersions,
                updatedAt = "Just now"
            )
            list[index] = updatedDoc
        }
    }

    private fun mapNetworkToDomain(dto: DocumentNetworkDto): Document {
        val docType = try {
            DocumentType.valueOf(dto.documentType)
        } catch (e: Exception) {
            DocumentType.DRAWING_ARCHITECTURAL
        }
        val docStatus = try {
            DocumentStatus.valueOf(dto.status)
        } catch (e: Exception) {
            DocumentStatus.APPROVED
        }
        val versions = dto.versions.map { v ->
            DocumentVersion(
                id = v.id,
                documentId = v.documentId,
                versionNumber = v.versionNumber,
                fileName = v.fileName,
                fileSizeBytes = v.fileSizeBytes,
                mimeType = v.mimeType,
                fileUrl = v.fileUrl,
                changeNotes = v.changeNotes,
                uploadedBy = v.uploadedBy,
                uploadedAt = v.uploadedAt
            )
        }
        return Document(
            id = dto.id,
            projectId = dto.projectId,
            organizationId = dto.organizationId,
            title = dto.title,
            documentType = docType,
            category = dto.category,
            latestVersion = dto.latestVersion,
            status = docStatus,
            createdBy = dto.createdBy,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt,
            versions = versions
        )
    }
}
