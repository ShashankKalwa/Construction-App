package com.example.contruction_management_app.domain.repository

import com.example.contruction_management_app.core.network.*
import com.example.contruction_management_app.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class InventoryRepository(private val apiService: CmsApiService) {

    private val localMaterialCache = ConcurrentHashMap<String, MutableList<Material>>()
    private val localRequestCache = ConcurrentHashMap<String, MutableList<MaterialRequest>>()

    init {
        seedInitialDemoInventory("prj_commercial_tower_b")
    }

    private fun seedInitialDemoInventory(projectId: String) {
        val materials = mutableListOf(
            Material(
                id = "mat_cem_opc",
                projectId = projectId,
                code = "MAT-CEM-OPC",
                name = "OPC 53 Grade Cement",
                category = MaterialCategory.CEMENT,
                unit = "Bags",
                currentStock = BigDecimal("450.00"),
                minStockThreshold = BigDecimal("100.00"),
                unitCost = BigDecimal("380.00"),
                isLowStock = false,
                createdAt = "2026-03-01T08:00:00Z"
            ),
            Material(
                id = "mat_steel_16",
                projectId = projectId,
                code = "MAT-STEEL-16",
                name = "16mm TMT Fe550D Rebar",
                category = MaterialCategory.STEEL,
                unit = "MT",
                currentStock = BigDecimal("14.50"),
                minStockThreshold = BigDecimal("5.00"),
                unitCost = BigDecimal("65000.00"),
                isLowStock = false,
                createdAt = "2026-03-01T08:00:00Z"
            ),
            Material(
                id = "mat_sand_m",
                projectId = projectId,
                code = "MAT-SAND-M",
                name = "Manufactured M-Sand",
                category = MaterialCategory.AGGREGATE,
                unit = "Ton",
                currentStock = BigDecimal("220.00"),
                minStockThreshold = BigDecimal("50.00"),
                unitCost = BigDecimal("1250.00"),
                isLowStock = false,
                createdAt = "2026-03-01T08:00:00Z"
            ),
            Material(
                id = "mat_agg_20",
                projectId = projectId,
                code = "MAT-AGG-20",
                name = "20mm Coarse Aggregate",
                category = MaterialCategory.AGGREGATE,
                unit = "Ton",
                currentStock = BigDecimal("310.00"),
                minStockThreshold = BigDecimal("80.00"),
                unitCost = BigDecimal("1100.00"),
                isLowStock = false,
                createdAt = "2026-03-01T08:00:00Z"
            ),
            Material(
                id = "mat_brk_red",
                projectId = projectId,
                code = "MAT-BRK-RED",
                name = "Class 1 Red Clay Bricks",
                category = MaterialCategory.MASONRY,
                unit = "Pcs",
                currentStock = BigDecimal("15000.00"),
                minStockThreshold = BigDecimal("3000.00"),
                unitCost = BigDecimal("9.50"),
                isLowStock = false,
                createdAt = "2026-03-01T08:00:00Z"
            ),
            Material(
                id = "mat_steel_12",
                projectId = projectId,
                code = "MAT-STEEL-12",
                name = "12mm TMT Fe550D Rebar",
                category = MaterialCategory.STEEL,
                unit = "MT",
                currentStock = BigDecimal("3.50"),
                minStockThreshold = BigDecimal("6.00"),
                unitCost = BigDecimal("66000.00"),
                isLowStock = true, // Low stock trigger
                createdAt = "2026-03-01T08:00:00Z"
            )
        )
        localMaterialCache[projectId] = materials

        val requests = mutableListOf(
            MaterialRequest(
                id = "mr_001",
                projectId = projectId,
                materialId = "mat_steel_12",
                materialName = "12mm TMT Fe550D Rebar",
                materialUnit = "MT",
                quantity = BigDecimal("5.00"),
                requiredByDate = "2026-03-25",
                requestedBy = "Site Engineer",
                approvedBy = "Project Manager",
                status = MaterialRequestStatus.APPROVED,
                phaseOrTaskDesc = "Foundation footing rebar cage",
                createdAt = "2026-03-20T10:00:00Z"
            ),
            MaterialRequest(
                id = "mr_002",
                projectId = projectId,
                materialId = "mat_cem_opc",
                materialName = "OPC 53 Grade Cement",
                materialUnit = "Bags",
                quantity = BigDecimal("150.00"),
                requiredByDate = "2026-03-26",
                requestedBy = "Site Engineer",
                approvedBy = null,
                status = MaterialRequestStatus.PENDING,
                phaseOrTaskDesc = "Column M25 casting pour",
                createdAt = "2026-03-21T14:30:00Z"
            )
        )
        localRequestCache[projectId] = requests
    }

    suspend fun getMaterials(projectId: String): Result<List<Material>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMaterials(projectId)
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                val mapped = response.body()!!.map { mapMaterialDto(it) }
                localMaterialCache[projectId] = mapped.toMutableList()
                Result.success(mapped)
            } else {
                Result.success(localMaterialCache[projectId] ?: emptyList())
            }
        } catch (_: Exception) {
            Result.success(localMaterialCache[projectId] ?: emptyList())
        }
    }

    suspend fun createMaterial(
        projectId: String,
        code: String,
        name: String,
        category: MaterialCategory,
        unit: String,
        initialStock: BigDecimal,
        minStockThreshold: BigDecimal,
        unitCost: BigDecimal
    ): Result<Material> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createMaterial(
                projectId,
                CreateMaterialRequest(
                    code = code,
                    name = name,
                    category = category.name,
                    unit = unit,
                    initialStock = initialStock,
                    minStockThreshold = minStockThreshold,
                    unitCost = unitCost
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val item = mapMaterialDto(response.body()!!)
                localMaterialCache.getOrPut(projectId) { mutableListOf() }.add(item)
                Result.success(item)
            } else {
                val isLow = initialStock.compareTo(minStockThreshold) < 0
                val local = Material(
                    id = "mat_" + UUID.randomUUID().toString().take(8),
                    projectId = projectId,
                    code = code,
                    name = name,
                    category = category,
                    unit = unit,
                    currentStock = initialStock,
                    minStockThreshold = minStockThreshold,
                    unitCost = unitCost,
                    isLowStock = isLow,
                    createdAt = "Just now"
                )
                localMaterialCache.getOrPut(projectId) { mutableListOf() }.add(local)
                Result.success(local)
            }
        } catch (_: Exception) {
            val isLow = initialStock.compareTo(minStockThreshold) < 0
            val local = Material(
                id = "mat_" + UUID.randomUUID().toString().take(8),
                projectId = projectId,
                code = code,
                name = name,
                category = category,
                unit = unit,
                currentStock = initialStock,
                minStockThreshold = minStockThreshold,
                unitCost = unitCost,
                isLowStock = isLow,
                createdAt = "Just now"
            )
            localMaterialCache.getOrPut(projectId) { mutableListOf() }.add(local)
            Result.success(local)
        }
    }

    suspend fun adjustStock(
        projectId: String,
        materialId: String,
        movementType: MovementType,
        quantity: BigDecimal,
        referenceId: String? = null,
        notes: String? = null
    ): Result<Material> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.adjustStock(
                projectId,
                materialId,
                StockAdjustRequest(movementType.name, quantity, referenceId, notes)
            )
            if (response.isSuccessful && response.body() != null) {
                val updated = mapMaterialDto(response.body()!!)
                updateCachedMaterial(projectId, updated)
                Result.success(updated)
            } else {
                updateLocalStock(projectId, materialId, movementType, quantity)
            }
        } catch (_: Exception) {
            updateLocalStock(projectId, materialId, movementType, quantity)
        }
    }

    private fun updateLocalStock(
        projectId: String,
        materialId: String,
        movementType: MovementType,
        quantity: BigDecimal
    ): Result<Material> {
        val list = localMaterialCache[projectId] ?: return Result.failure(Exception("Project not found in cache"))
        val idx = list.indexOfFirst { it.id == materialId }
        if (idx < 0) return Result.failure(Exception("Material not found"))
        val existing = list[idx]

        val isDeduction = (movementType == MovementType.OUTWARD_ISSUE)
            || (movementType == MovementType.ADJUSTMENT && quantity.compareTo(BigDecimal.ZERO) < 0)

        val newStock = if (isDeduction) {
            val deductQty = quantity.abs()
            if (existing.currentStock.compareTo(deductQty) < 0) {
                return Result.failure(Exception("Insufficient stock. Available: ${existing.currentStock}"))
            }
            existing.currentStock.subtract(deductQty)
        } else {
            existing.currentStock.add(quantity.abs())
        }

        val updated = existing.copy(
            currentStock = newStock,
            isLowStock = newStock.compareTo(existing.minStockThreshold) < 0
        )
        list[idx] = updated
        return Result.success(updated)
    }

    private fun updateCachedMaterial(projectId: String, material: Material) {
        val list = localMaterialCache[projectId] ?: mutableListOf()
        val idx = list.indexOfFirst { it.id == material.id }
        if (idx >= 0) list[idx] = material else list.add(material)
        localMaterialCache[projectId] = list
    }

    suspend fun getMaterialRequests(projectId: String): Result<List<MaterialRequest>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMaterialRequests(projectId)
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                val mapped = response.body()!!.map { mapRequestDto(it) }
                localRequestCache[projectId] = mapped.toMutableList()
                Result.success(mapped)
            } else {
                Result.success(localRequestCache[projectId] ?: emptyList())
            }
        } catch (_: Exception) {
            Result.success(localRequestCache[projectId] ?: emptyList())
        }
    }

    suspend fun createMaterialRequest(
        projectId: String,
        materialId: String,
        quantity: BigDecimal,
        requiredByDate: String?,
        phaseOrTaskDesc: String?
    ): Result<MaterialRequest> = withContext(Dispatchers.IO) {
        val material = localMaterialCache[projectId]?.firstOrNull { it.id == materialId }
        val matName = material?.name ?: "Material"
        val matUnit = material?.unit ?: "units"

        try {
            val response = apiService.createMaterialRequest(
                projectId,
                CreateMaterialRequestRequest(materialId, quantity, requiredByDate, phaseOrTaskDesc)
            )
            if (response.isSuccessful && response.body() != null) {
                val req = mapRequestDto(response.body()!!)
                localRequestCache.getOrPut(projectId) { mutableListOf() }.add(req)
                Result.success(req)
            } else {
                val local = MaterialRequest(
                    id = "mr_" + UUID.randomUUID().toString().take(8),
                    projectId = projectId,
                    materialId = materialId,
                    materialName = matName,
                    materialUnit = matUnit,
                    quantity = quantity,
                    requiredByDate = requiredByDate,
                    requestedBy = "Site Engineer",
                    approvedBy = null,
                    status = MaterialRequestStatus.PENDING,
                    phaseOrTaskDesc = phaseOrTaskDesc,
                    createdAt = "Just now"
                )
                localRequestCache.getOrPut(projectId) { mutableListOf() }.add(local)
                Result.success(local)
            }
        } catch (_: Exception) {
            val local = MaterialRequest(
                id = "mr_" + UUID.randomUUID().toString().take(8),
                projectId = projectId,
                materialId = materialId,
                materialName = matName,
                materialUnit = matUnit,
                quantity = quantity,
                requiredByDate = requiredByDate,
                requestedBy = "Site Engineer",
                approvedBy = null,
                status = MaterialRequestStatus.PENDING,
                phaseOrTaskDesc = phaseOrTaskDesc,
                createdAt = "Just now"
            )
            localRequestCache.getOrPut(projectId) { mutableListOf() }.add(local)
            Result.success(local)
        }
    }

    suspend fun approveMaterialRequest(projectId: String, requestId: String): Result<MaterialRequest> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.approveMaterialRequest(projectId, requestId)
                if (response.isSuccessful && response.body() != null) {
                    val req = mapRequestDto(response.body()!!)
                    updateCachedRequest(projectId, req)
                    Result.success(req)
                } else {
                    updateLocalRequestStatus(projectId, requestId, MaterialRequestStatus.APPROVED, "Project Manager")
                }
            } catch (_: Exception) {
                updateLocalRequestStatus(projectId, requestId, MaterialRequestStatus.APPROVED, "Project Manager")
            }
        }

    private fun updateLocalRequestStatus(
        projectId: String,
        requestId: String,
        status: MaterialRequestStatus,
        approver: String?
    ): Result<MaterialRequest> {
        val list = localRequestCache[projectId] ?: return Result.failure(Exception("Project not found"))
        val idx = list.indexOfFirst { it.id == requestId }
        if (idx < 0) return Result.failure(Exception("Request not found"))
        val updated = list[idx].copy(status = status, approvedBy = approver)
        list[idx] = updated
        return Result.success(updated)
    }

    private fun updateCachedRequest(projectId: String, req: MaterialRequest) {
        val list = localRequestCache[projectId] ?: mutableListOf()
        val idx = list.indexOfFirst { it.id == req.id }
        if (idx >= 0) list[idx] = req else list.add(req)
        localRequestCache[projectId] = list
    }

    private fun mapMaterialDto(dto: MaterialNetworkDto): Material {
        val cat = try {
            MaterialCategory.valueOf(dto.category.uppercase())
        } catch (_: Exception) {
            MaterialCategory.OTHER
        }
        val isLow = dto.isLowStock || (dto.currentStock.compareTo(dto.minStockThreshold) < 0)
        return Material(
            id = dto.id,
            projectId = dto.projectId,
            code = dto.code,
            name = dto.name,
            category = cat,
            unit = dto.unit,
            currentStock = dto.currentStock,
            minStockThreshold = dto.minStockThreshold,
            unitCost = dto.unitCost,
            isLowStock = isLow,
            createdAt = dto.createdAt
        )
    }

    private fun mapRequestDto(dto: MaterialRequestNetworkDto): MaterialRequest {
        val st = try {
            MaterialRequestStatus.valueOf(dto.status.uppercase())
        } catch (_: Exception) {
            MaterialRequestStatus.PENDING
        }
        return MaterialRequest(
            id = dto.id,
            projectId = dto.projectId,
            materialId = dto.materialId,
            materialName = dto.materialName,
            materialUnit = dto.materialUnit,
            quantity = dto.quantity,
            requiredByDate = dto.requiredByDate,
            requestedBy = dto.requestedBy,
            approvedBy = dto.approvedBy,
            status = st,
            phaseOrTaskDesc = dto.phaseOrTaskDesc,
            createdAt = dto.createdAt
        )
    }
}
