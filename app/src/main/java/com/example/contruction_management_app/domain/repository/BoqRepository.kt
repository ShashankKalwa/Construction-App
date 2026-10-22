package com.example.contruction_management_app.domain.repository

import com.example.contruction_management_app.core.network.*
import com.example.contruction_management_app.domain.model.Boq
import com.example.contruction_management_app.domain.model.BoqItem
import com.example.contruction_management_app.domain.model.BoqStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class BoqRepository(private val apiService: CmsApiService) {

    private val localBoqCache = ConcurrentHashMap<String, MutableList<Boq>>()

    init {
        seedInitialDemoBoq("prj_commercial_tower_b")
    }

    private fun seedInitialDemoBoq(projectId: String) {
        val boqId = "boq_tower_b_v1"
        val items = mutableListOf(
            BoqItem(
                id = "item_1",
                boqId = boqId,
                itemCode = "01.01",
                description = "Earthwork in excavation in foundation trenches",
                category = "EARTHWORK",
                unit = "M3",
                estimatedQuantity = BigDecimal("1500.00"),
                unitRate = BigDecimal("350.00"),
                totalAmount = BigDecimal("525000.00"),
                orderIndex = 1
            ),
            BoqItem(
                id = "item_2",
                boqId = boqId,
                itemCode = "02.01",
                description = "Reinforced Cement Concrete M25 in columns & beams",
                category = "CONCRETE",
                unit = "M3",
                estimatedQuantity = BigDecimal("850.00"),
                unitRate = BigDecimal("6500.00"),
                totalAmount = BigDecimal("5525000.00"),
                orderIndex = 2
            ),
            BoqItem(
                id = "item_3",
                boqId = boqId,
                itemCode = "03.01",
                description = "Thermo-mechanically treated (TMT) Fe500D rebar reinforcement",
                category = "STEEL",
                unit = "KG",
                estimatedQuantity = BigDecimal("68000.00"),
                unitRate = BigDecimal("72.00"),
                totalAmount = BigDecimal("4896000.00"),
                orderIndex = 3
            ),
            BoqItem(
                id = "item_4",
                boqId = boqId,
                itemCode = "04.01",
                description = "Brickwork with common burnt clay bricks in cement mortar 1:6",
                category = "MASONRY",
                unit = "M3",
                estimatedQuantity = BigDecimal("420.00"),
                unitRate = BigDecimal("4200.00"),
                totalAmount = BigDecimal("1764000.00"),
                orderIndex = 4
            )
        )
        val initialBoq = Boq(
            id = boqId,
            projectId = projectId,
            title = "Commercial Tower B - Core & Shell Initial BOQ",
            version = 1,
            status = BoqStatus.DRAFT,
            totalAmount = items.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.totalAmount) },
            createdBy = "Project Manager",
            approvedBy = null,
            items = items,
            createdAt = "2026-03-01T10:00:00Z"
        )
        localBoqCache[projectId] = mutableListOf(initialBoq)
    }

    suspend fun getBoqs(projectId: String): Result<List<Boq>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProjectBoqs(projectId)
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                val mapped = response.body()!!.map { mapBoqDto(it) }
                localBoqCache[projectId] = mapped.toMutableList()
                Result.success(mapped)
            } else {
                Result.success(localBoqCache[projectId] ?: emptyList())
            }
        } catch (_: Exception) {
            Result.success(localBoqCache[projectId] ?: emptyList())
        }
    }

    suspend fun getBoqById(id: String): Result<Boq> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getBoqById(id)
            if (response.isSuccessful && response.body() != null) {
                val boq = mapBoqDto(response.body()!!)
                // update local cache
                localBoqCache[boq.projectId]?.let { list ->
                    val idx = list.indexOfFirst { it.id == id }
                    if (idx >= 0) list[idx] = boq else list.add(boq)
                }
                Result.success(boq)
            } else {
                findInLocalCache(id)?.let { Result.success(it) }
                    ?: Result.failure(Exception("BOQ not found"))
            }
        } catch (_: Exception) {
            findInLocalCache(id)?.let { Result.success(it) }
                ?: Result.failure(Exception("BOQ not found"))
        }
    }

    suspend fun createBoq(projectId: String, title: String, version: Int = 1): Result<Boq> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.createBoq(projectId, CreateBoqRequest(title, version))
                if (response.isSuccessful && response.body() != null) {
                    val created = mapBoqDto(response.body()!!)
                    localBoqCache.getOrPut(projectId) { mutableListOf() }.add(created)
                    Result.success(created)
                } else {
                    val local = Boq(
                        id = "boq_" + UUID.randomUUID().toString().take(8),
                        projectId = projectId,
                        title = title,
                        version = version,
                        status = BoqStatus.DRAFT,
                        totalAmount = BigDecimal.ZERO,
                        createdBy = "Local User",
                        approvedBy = null,
                        items = emptyList(),
                        createdAt = "Just now"
                    )
                    localBoqCache.getOrPut(projectId) { mutableListOf() }.add(local)
                    Result.success(local)
                }
            } catch (_: Exception) {
                val local = Boq(
                    id = "boq_" + UUID.randomUUID().toString().take(8),
                    projectId = projectId,
                    title = title,
                    version = version,
                    status = BoqStatus.DRAFT,
                    totalAmount = BigDecimal.ZERO,
                    createdBy = "Local User",
                    approvedBy = null,
                    items = emptyList(),
                    createdAt = "Just now"
                )
                localBoqCache.getOrPut(projectId) { mutableListOf() }.add(local)
                Result.success(local)
            }
        }

    suspend fun addBoqItem(
        boqId: String,
        itemCode: String,
        description: String,
        category: String,
        unit: String,
        estimatedQuantity: BigDecimal,
        unitRate: BigDecimal,
        calculationId: String? = null,
        orderIndex: Int = 0
    ): Result<BoqItem> = withContext(Dispatchers.IO) {
        val total = estimatedQuantity.multiply(unitRate)
        try {
            val response = apiService.addBoqItem(
                boqId,
                CreateBoqItemRequest(
                    itemCode = itemCode,
                    description = description,
                    category = category,
                    unit = unit,
                    estimatedQuantity = estimatedQuantity,
                    unitRate = unitRate,
                    calculationId = calculationId,
                    orderIndex = orderIndex
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val item = mapBoqItemDto(response.body()!!)
                updateLocalBoqWithNewItem(boqId, item)
                Result.success(item)
            } else {
                val item = BoqItem(
                    id = "item_" + UUID.randomUUID().toString().take(8),
                    boqId = boqId,
                    itemCode = itemCode,
                    description = description,
                    category = category,
                    unit = unit,
                    estimatedQuantity = estimatedQuantity,
                    unitRate = unitRate,
                    totalAmount = total,
                    calculationId = calculationId,
                    orderIndex = orderIndex
                )
                updateLocalBoqWithNewItem(boqId, item)
                Result.success(item)
            }
        } catch (_: Exception) {
            val item = BoqItem(
                id = "item_" + UUID.randomUUID().toString().take(8),
                boqId = boqId,
                itemCode = itemCode,
                description = description,
                category = category,
                unit = unit,
                estimatedQuantity = estimatedQuantity,
                unitRate = unitRate,
                totalAmount = total,
                calculationId = calculationId,
                orderIndex = orderIndex
            )
            updateLocalBoqWithNewItem(boqId, item)
            Result.success(item)
        }
    }

    suspend fun submitBoq(boqId: String): Result<Boq> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.submitBoq(boqId)
            if (response.isSuccessful && response.body() != null) {
                val updated = mapBoqDto(response.body()!!)
                updateCachedBoq(updated)
                Result.success(updated)
            } else {
                updateLocalStatus(boqId, BoqStatus.SUBMITTED, null)
            }
        } catch (_: Exception) {
            updateLocalStatus(boqId, BoqStatus.SUBMITTED, null)
        }
    }

    suspend fun decideBoq(boqId: String, status: BoqStatus, reason: String? = null): Result<Boq> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.decideBoq(
                    boqId,
                    BoqDecisionRequest(status.name, reason)
                )
                if (response.isSuccessful && response.body() != null) {
                    val updated = mapBoqDto(response.body()!!)
                    updateCachedBoq(updated)
                    Result.success(updated)
                } else {
                    updateLocalStatus(boqId, status, "Approver")
                }
            } catch (_: Exception) {
                updateLocalStatus(boqId, status, "Approver")
            }
        }

    private fun updateLocalStatus(boqId: String, status: BoqStatus, approver: String?): Result<Boq> {
        val existing = findInLocalCache(boqId) ?: return Result.failure(Exception("BOQ not found"))
        val updated = existing.copy(
            status = status,
            approvedBy = approver ?: existing.approvedBy
        )
        updateCachedBoq(updated)
        return Result.success(updated)
    }

    private fun updateLocalBoqWithNewItem(boqId: String, newItem: BoqItem) {
        val existing = findInLocalCache(boqId) ?: return
        val newItems = existing.items + newItem
        val newTotal = newItems.fold(BigDecimal.ZERO) { acc, it -> acc.add(it.totalAmount) }
        val updated = existing.copy(items = newItems, totalAmount = newTotal)
        updateCachedBoq(updated)
    }

    private fun findInLocalCache(boqId: String): Boq? {
        for ((_, list) in localBoqCache) {
            val item = list.firstOrNull { it.id == boqId }
            if (item != null) return item
        }
        return null
    }

    private fun updateCachedBoq(boq: Boq) {
        val list = localBoqCache[boq.projectId] ?: mutableListOf()
        val idx = list.indexOfFirst { it.id == boq.id }
        if (idx >= 0) {
            list[idx] = boq
        } else {
            list.add(boq)
        }
        localBoqCache[boq.projectId] = list
    }

    private fun mapBoqDto(dto: BoqNetworkDto): Boq {
        val status = try {
            BoqStatus.valueOf(dto.status.uppercase())
        } catch (_: Exception) {
            BoqStatus.DRAFT
        }
        return Boq(
            id = dto.id,
            projectId = dto.projectId,
            title = dto.title,
            version = dto.version,
            status = status,
            totalAmount = dto.totalAmount,
            createdBy = dto.createdBy,
            approvedBy = dto.approvedBy,
            items = dto.items.map { mapBoqItemDto(it) },
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt
        )
    }

    private fun mapBoqItemDto(dto: BoqItemNetworkDto): BoqItem {
        return BoqItem(
            id = dto.id,
            boqId = dto.boqId,
            itemCode = dto.itemCode,
            description = dto.description,
            category = dto.category,
            unit = dto.unit,
            estimatedQuantity = dto.estimatedQuantity,
            unitRate = dto.unitRate,
            totalAmount = dto.totalAmount,
            calculationId = dto.calculationId,
            orderIndex = dto.orderIndex
        )
    }
}
